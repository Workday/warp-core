package com.workday.warp.persistence

import java.sql.Timestamp
import java.time.Instant
import com.workday.warp.TestId
import com.workday.warp.config.CoreWarpProperty._
import com.workday.warp.config.WarpPropertyManager
import com.workday.warp.logger.WarpLogging
import com.workday.warp.persistence.Tables._
import com.workday.warp.persistence.Tables.RowTypeClasses._
import com.workday.warp.persistence.Tables.profile.api._
import com.workday.warp.persistence.TablesLike._
import com.workday.warp.persistence.IdentifierSyntax._
import com.workday.warp.persistence.exception.WarpFieldPersistenceException
import slick.jdbc.TransactionIsolation

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Adds an instance of [[CorePersistenceUtils]] to any Class that mixes in this trait.
  * Mix in this trait in classes or traits that require [[CorePersistenceUtils]]
  *
  * Created by leslie.lam on 2/9/18.
  */
trait CorePersistenceAware extends PersistenceAware with WarpLogging {

  /** [[CorePersistenceUtils]] to allow for db interaction */
  val persistenceUtils: CorePersistenceUtils = new CorePersistenceUtils

  // Initializes the database by initializing the companion object.
  override def initUtils(): Unit = CorePersistenceUtils.version

  protected class CorePersistenceUtils extends AbstractPersistenceUtils with CoreQueries {

    /**
      * Attempts to look up a [[BuildRow]] with the specified parameters. Will be created if it does not exist.
      *
      * @param year year of the build.
      * @param week week of the build.
      * @param buildNumber number of the build.
      * @return a [[BuildRow]] with the specified parameters.
      */
    override def findOrCreateBuild(year: Int, week: Int, buildNumber: Int): BuildRowLike = {
      val find: DBIO[Seq[BuildRowWrapper]] = this.readBuildQuery(year, week, buildNumber).map(_.toSeq)
      val timestamp: Timestamp = Timestamp from Instant.now
      // mysql truncates to second precision anyway, TODO we might be able to modify the write query to return the truncated timestamp
      timestamp.setNanos(0)
      val row: BuildRow = BuildRow(Tables.nullId, year, week, buildNumber, timestamp, timestamp)
      val create: DBIO[BuildRowWrapper] = this.writeBuildQuery(row)
      this.findOrCreate(find, create)
    }


    /**
      * Attempts to look up a [[TestDefinitionRow]] with the given method signature. Will be created if it does not exist.
      * If documentation does not match, then it will be updated
      *
      * @param testId the method signature to look up.
      * @return a [[TestDefinitionRowLike]] with the given method signature.
      */
    override def findOrCreateTestDefinition(testId: TestId, documentation: Option[String] = None): TestDefinitionRowLike = {
      // make sure we have something that fits the schema column size
      val trimmedSignature: String = testId.id take CorePersistenceConstants.SIGNATURE_LENGTH
      val find: Query[TestDefinition, TestDefinitionRow, Seq] = TestDefinition filter { _.methodSignature === trimmedSignature }
      val row: TestDefinitionRow = TestDefinitionRow(
        Tables.nullId,
        trimmedSignature,
        documentation
      )
      val create: DBIO[TestDefinitionRowWrapper] = this.writeTestDefinitionQuery(row)
      val action: DBIO[TestDefinitionRow] = for {
        // get the row by instance id and update it
        rowsAffected <- find map { info => info.documentation } update documentation
        _ <- rowsAffected match {
          // no existing row, insert a new one
          case 0 => create
          // we updated an existing row
          case 1 => DBIO.successful(row)
          case n => DBIO.failed(new RuntimeException(s"expected 0 or 1 TestDefinition row(s) affected, not $n"))
        }
        row <- find.result.head
      } yield row

      this.runWithRetries(action)
    }


    /**
      * Attempts to look up a [[MeasurementNameRow]] with the given name. Will be created if it does not exist.
      *
      * @param name key to look up, or create, a [[MeasurementNameRow]]
      * @return a [[MeasurementNameRowLike]] with the given name
      */
    override def findOrCreateMeasurementName(name: String): MeasurementNameRowLike = {
      // make sure we have something that fits the schema column size
      val trimmedName: String = name take CorePersistenceConstants.DESCRIPTION_LENGTH
      val find: DBIO[Seq[MeasurementNameRowWrapper]] = this.readMeasurementNameQuery(trimmedName).map(_.toSeq)
      val row: MeasurementNameRow = MeasurementNameRow(Tables.nullId, trimmedName)
      val create: DBIO[MeasurementNameRowWrapper] = this.writeMeasurementNameQuery(row)
      this.findOrCreate(find, create)
    }


    /**
      * Attempts to look up a [[TagNameRow]] with the given name. Will be created if it does not exist.
      *
      * @param name key to look up, or create, a [[TagNameRow]].
      * @return a [[TagNameRowLike]] with the given name.
      */
    override def findOrCreateTagName(name: String, nameType: String = "plain_txt",
                                            isUserGenerated: Boolean = true): TagNameRowLike = {
      // make sure we have something that fits the schema column size
      val trimmedName: String = name take CorePersistenceConstants.DESCRIPTION_LENGTH
      val find: DBIO[Seq[TagNameRowWrapper]] = this.readTagNameQuery(trimmedName).map(_.toSeq)
      val row: TagNameRow = TagNameRow(Tables.nullId, trimmedName, nameType, isUserGenerated)
      val create: DBIO[TagNameRowWrapper] = this.writeTagNameQuery(row)
      this.findOrCreate(find, create)
    }


    /**
      * Creates, inserts, and returns a [[TestExecutionRowLike]]
      *
      * @param testId id of the measured test (usually fully qualified junit method).
      * @param timeStarted time the measured test was started.
      * @param responseTime observed duration of the measured test (seconds).
      * @param maxResponseTime maximum allowable response time set on the measured test (seconds).
      * @param passed a boolean indicating whether the test functionally passed.
      * @param maybeDocs containing documentation for the [[TestExecutionRow]]
      * @return a [[TestExecutionRowLike]] with the given parameters.
      */
    override def createTestExecution(testId: TestId,
                                     timeStarted: Instant,
                                     responseTime: Double,
                                     maxResponseTime: Double,
                                     passed: Boolean = true,
                                     maybeDocs: Option[String] = None): TablesLike.TestExecutionRowLike = {
      if (responseTime == 0.0) {
        throw new IllegalArgumentException("Zero Time recorded for this measurement, check your adapter implementation.")
      }

      val build: BuildNumber = BuildNumber(SILVER_BUILD_NUMBER.value)
      val buildInfo: BuildRowLike = this.findOrCreateBuild(build.major, build.minor, build.patch)
      // TODO update build info with last tested
      val testDefinition: TestDefinitionRowLike = this.findOrCreateTestDefinition(testId, maybeDocs)
      val testExecution: TestExecutionRow = TestExecutionRow(
        Tables.nullId,
        idTestDefinition = testDefinition.idTestDefinition,
        idBuild = buildInfo.idBuild,
        passed = passed,
        responseTime = responseTime,
        responseTimeRequirement = maxResponseTime,
        startTime = Timestamp from timeStarted,
        // TODO consider deleting this field somewhat redundant
        endTime = Timestamp from Instant.now
      )

      this.synchronously(this.writeTestExecutionQuery(testExecution))
    }

    /**
      * Updates each [[TestExecutionRow]] in `testExecutions` to have the new provided threshold.
      *
      * When a threshold is set using the dsl, we don't have access to the threshold until the test has completed and the
      * [[org.scalatest.matchers.Matcher]] is being executed. At that time, we update the existing rows in the db with the
      * user-provided threshold.
      *
      * @param testExecutions collection of [[TestExecutionRow]] to update.
      * @param newThreshold new threshold to set for each row.
      */
    override def updateTestExecutionThresholds[T: TestExecutionRowLikeType](testExecutions: Iterable[T], newThreshold: Double): Unit = {
      testExecutions foreach { testExecution: T => this.synchronously(this.updateTestExecutionThreshold(testExecution, newThreshold)) }
    }


    /**
      * Persists generic measurements in the Measurement table. Looks up the MeasurementName corresponding to
      * `name`, creates a new Measurement with the appropriate fields set.
      *
      * @param idTestExecution id for the [[TestExecutionRow]] associated with this measurement.
      * @param name name to use for this measurement.
      * @param result result of this measurement.
      */
    override def recordMeasurement(idTestExecution: Int, name: String, result: Double): Unit = {
      val nameRow: MeasurementNameRowLike = this.findOrCreateMeasurementName(name)
      val measurementRow: MeasurementRow = MeasurementRow(
        idTestExecution = idTestExecution,
        idMeasurementName = nameRow.idMeasurementName,
        result = result
      )

      this.synchronously(Measurement += measurementRow)
    }


    /**
      * Persists generic tags in the [[TestExecutionTag]] table. Looks up the [[TagNameRow]] corresponding to
      * `name`, creates a new tag with the appropriate fields set.
      *
      * Behavior:
      *   If a tag with the given name and value exists in the database:
      *     Log a warning, but is a success.
      *   If no tag with given name and idTestDefinition exists in the database:
      *     Success.
      *
      * @param idTestExecution id for the [[TestExecutionRow]] associated with this tag.
      * @param name name to use for this tag.
      * @param value value of the tag.
      * @return a [[TestExecutionTagRowLike]] with the given parameters.
      */
    @throws[scala.RuntimeException]
    @throws[WarpFieldPersistenceException]
    override def recordTestExecutionTag(idTestExecution: Int,
                                   name: String,
                                   value: String,
                                   isUserGenerated: Boolean = true): TestExecutionTagRowLike = {
      val nameRow: TagNameRowLike = this.findOrCreateTagName(name, isUserGenerated = isUserGenerated)
      val dbAction: DBIO[TestExecutionTagRowWrapper] = this.insertOrUpdateTestExecutionTagValueQuery(
        TestExecutionTagRow(Tables.nullId, idTestExecution, nameRow.idTagName, value)
      )

      this.runWithRetries(dbAction)
    }



    /**
      * Persists generic tags in the [[TestExecutionTag]] table. Looks up the [[TagNameRow]] corresponding to
      * `name`, creates a new tag with the appropriate fields set.
      *
      * @param testExecution [[TestExecutionRow]] associated with this tag.
      * @param name name to use for this tag.
      * @param value value of the tag.
      * @return a [[TestExecutionTagRowLike]] with the given parameters.
      */
    @deprecated("Use recordTestExecutionTag with idTestExecution", since = "9.0.0")
    override def recordTestExecutionTag[T: TestExecutionTagRowLikeType](testExecution: T,
                                                              name: String,
                                                              value: String,
                                                              isUserGenerated: Boolean): TestExecutionTagRowLike = {
      this.recordTestExecutionTag(testExecution.idTestExecution, name, value, isUserGenerated)
    }


    /**
      * Persists generic tags in the [[BuildTag]] table. Looks up the [[TagNameRow]] corresponding to
      * `name`, creates a new tag with the appropriate fields set.
      *
      * @param idBuild id of the [[BuildRowLike]] associated with this tag.
      * @param name name to use for this tag.
      * @param value value of the tag.
      * @param isUserGenerated whether the tag was generated by the user. Most will be false.
      * @return a [[BuildTagRowLike]] with the given parameters.
      */
    override def recordBuildTag(idBuild: Int, name: String, value: String, isUserGenerated: Boolean): BuildTagRowLike = {
      val nameRow: TagNameRowLike = this.findOrCreateTagName(name, isUserGenerated = isUserGenerated)
      val dbAction: DBIO[BuildTagRowWrapper] = this.insertOrUpdateBuildTagValueQuery(
        BuildTagRow(Tables.nullId, idBuild, nameRow.idTagName, value)
      )

      this.runWithRetries(dbAction)
    }


    /**
      * Persists generic tags in the [[BuildMetaTag]] table. Looks up the [[TagNameRow]] corresponding to
      * `name`, creates a new tag with the appropriate fields set.
      *
      * @param idBuildTag id of the [[BuildTagRowLike]] associated with this tag.
      * @param name name to use for this tag.
      * @param value value of the tag.
      * @param isUserGenerated whether the tag was generated by the user. Most will be false.
      * @return a [[BuildMetaTagRowLike]] with the given parameters.
      */
    override def recordBuildMetaTag(idBuildTag: Int, name: String, value: String, isUserGenerated: Boolean): BuildMetaTagRowLike = {
      val nameRow: TagNameRowLike = this.findOrCreateTagName(name, isUserGenerated = isUserGenerated)
      val dbAction: DBIO[BuildMetaTagRowWrapper] = this.insertOrUpdateBuildMetaTagValueQuery(
        BuildMetaTagRow(idBuildTag, nameRow.idTagName, value)
      )

      this.runWithRetries(dbAction)
    }

    /**
      * Persists generic tags in the [[TestDefinitionTag]] table. Looks up the [[TagNameRow]] corresponding to
      * `name`, creates a new tag with the appropriate fields set.
      *
      * Behavior:
      *   If a tag with the given name and value exists in the database:
      *     Log a warning, but is a success.
      *   If no tag with given name and idTestDefinition exists in the database:
      *     Success.
      *
      * @param idTestDefinition id for the [[TestDefinitionRow]] associated with this tag.
      * @param name name to use for this tag.
      * @param value value of the tag.
      * @return a [[TestDefinitionTagRowLike]] with the given parameters.
      */
    @throws[scala.RuntimeException]
    @throws[WarpFieldPersistenceException]
    override def recordTestDefinitionTag(idTestDefinition: Int,
                                       name: String,
                                       value: String,
                                       isUserGenerated: Boolean = true): TestDefinitionTagRowLike = {
      val nameRow: TagNameRowLike = this.findOrCreateTagName(name, isUserGenerated = isUserGenerated)
      val dbAction: DBIO[TestDefinitionTagRowWrapper] = this.insertOrUpdateTestDefinitionTagValueQuery(
        TestDefinitionTagRow(Tables.nullId, idTestDefinition, nameRow.idTagName, value)
      )

      this.runWithRetries(dbAction)
    }


    /**
      * Persists metatags into the [[TestDefinitionMetaTag]] table. Looks up the [[TagNameRow]] corresponding to
      * `name`, creates a new metatag with the appropriate fields set.
      *
      * Behavior:
      *   If a tag with the given name and value exists in the database:
      *     Log a warning, but is a success.
      *   If no tag with given name and idTestDefinitionTag exists in the database:
      *     Success.
      *
      * @param idTestDefinitionTag id for the [[TestDefinitionTagRow]] associated with this tag
      * @param name name to use for this tag.
      * @param value value of the tag.
      */
    @throws[WarpFieldPersistenceException]
    override def recordTestDefinitionMetaTag(idTestDefinitionTag: Int,
                                             name: String,
                                             value: String,
                                             isUserGenerated: Boolean = true): TestDefinitionMetaTagRowLike = {
      val nameRow: TagNameRowLike = this.findOrCreateTagName(name, isUserGenerated = isUserGenerated)
      val dbAction: DBIO[TestDefinitionMetaTagRowWrapper] = this.insertOrUpdateTestDefinitionMetaTagValueQuery(
        TestDefinitionMetaTagRow(idTestDefinitionTag, nameRow.idTagName, value)
      )

      this.runWithRetries(dbAction)
    }


    /**
      * Persists metatags into the [[TestExecutionMetaTag]] table. Looks up the [[TagNameRow]] corresponding to
      * `name`, creates a new metatags with the appropriate fields et.
      *
      * Behavior:
      *   If a tag with the given name and value exists in the database:
      *     Log a warning, but is a success.
      *   If no tag with given name and idTestDefinitionTag exists in the database:
      *     Success.
      *
      * @param idTestExecutionTag id for the [[TestExecutionTagRow]] associated with this tag
      * @param name name to use for this tag.
      * @param value value of the tag.
      */
    @throws[WarpFieldPersistenceException]
    override def recordTestExecutionMetaTag(idTestExecutionTag: Int,
                                            name: String,
                                            value: String,
                                            isUserGenerated: Boolean = true): TestExecutionMetaTagRowLike = {
      val nameRow: TagNameRowLike = this.findOrCreateTagName(name, isUserGenerated = isUserGenerated)
      val dbAction: DBIO[TestExecutionMetaTagRowWrapper] = this.insertOrUpdateTestExecutionMetaTagValueQuery(
        TestExecutionMetaTagRow(idTestExecutionTag, nameRow.idTagName, value)
      )

      this.runWithRetries(dbAction)
    }


    /** Computes the median of the given test at the given confidence level.
      *
      * @param identifier [[CoreIdentifier]] containing the fully qualified method signature of the test.
      * @return the median of the response time of the specified test
      */
    override def getMedianResponseTime[I: IdentifierType](identifier: I): Double = {
      val responseTimes: List[Double] = this.getResponseTimes(identifier).sorted
      val numResponseTimes: Int = responseTimes.size

      val median: Double = numResponseTimes % 2 match {
        case 0 => (responseTimes(numResponseTimes / 2) + responseTimes(numResponseTimes / 2 - 1)) / 2
        case 1 => responseTimes(numResponseTimes / 2)
      }

      logger.trace(s"Median of test: ${identifier.methodSignature} is $median")
      median
    }


    /**
      * @param testExecution [[TestExecutionRow]] to get the method signature for.
      * @return method signature of `testExecution`
      */
    override def getMethodSignature[T: TestExecutionRowLikeType](testExecution: T): String =
      this.synchronously(this.readTestExecutionSignatureQuery(testExecution)).head


    /**
      * @param name [[String]] name of TagNameRow
      * @return a [[TagNameRowLike]] with the given name
      */
    override def getTagName(name: String): Option[TagNameRowLike] =
      this.synchronously(this.readTagNameQuery(name))


    /**
      * Gets the entire row of a tag with id `idTagName` set on the [[TestExecutionRowLike]] with id `idTestExecution`.
      *
      * @param idTestExecution id of the [[TestExecutionRow]] to look up tags for.
      * @param idTagName id of the [[TagNameRow]] to look up.
      * @return a [[TestExecutionTagRowLike]] with the id `idTestExecution` and `idTagName`.
      */
    override def getTestExecutionTagsRow(idTestExecution: Int, idTagName: Int): TestExecutionTagRowLike =
      this.synchronously(testExecutionTagsRowQuery(idTestExecution, idTagName)).head


    /**
      * Safe version of reading a TestExecutionTag.
      *
      * @param idTestExecution id of the [[TestExecutionRow]] to look up tags for.
      * @param idTagName id of the [[TagNameRow]] to look up.
      * @return a [[TestExecutionTagRowLike]] with the id `idTestExecution` and `idTagName`.
      */
    override def getTestExecutionTagsRowSafe(idTestExecution: Int, idTagName: Int): Option[TestExecutionTagRowLike] =
      this.synchronously(testExecutionTagsRowQuery(idTestExecution, idTagName))


    /**
      * Reads prior test executions, subject to the provided limit.
      *
      * @param testExecution execution to look up history for.
      * @param limit query row limit.
      * @tparam T
      * @return a collection of prior test executions.
      */
    override def getPriorTestExecutions[T: TestExecutionRowLikeType](testExecution: T, limit: Int): Seq[TestExecutionRowLike] = {
      this.synchronously(getPriorTestExecutionsQuery(testExecution, limit))
    }

    /**
      * Reads spike filter settings for the given test definition.
      *
      * @param methodSignature method signature to look up spike filter settings for.
      * @return spike filter settings for the given test execution.
      */
    override def getSpikeFilterSettings(methodSignature: String): Option[SpikeFilterSettingsRowLike] =
      this.synchronously(getSpikeFilterSettingsQuery(methodSignature))


    /**
      * Writes a collection of spike filter settings.
      *
      * @param settings
      * @tparam T
      * @return number of rows affected.
      */
    override def writeSpikeFilterSettings[T: SpikeFilterSettingsRowLikeType](settings: Seq[T]): Int = {
      this.synchronously(writeSpikeFilterSettingsQuery(settings))
    }
  }
}

// This companion object initializes the schema, inserts seed data, and creates database users.
object CorePersistenceUtils extends Connection with CorePersistenceAware with MigrateSchemaLike with CoreQueries with CanDropCoreSchema {
  // make sure properties get loaded
  val version: String = WarpPropertyManager.version

  this.initSchema()
  this.createDbUsers()

  /**
    * Initializes the schema as defined in [[WARP_DATABASE_URL]], if it does not already exist.
    *
    * If the schema was previously dropped, you might need to call [[Connection.refresh()]] after calling this.
    */
  @throws[RuntimeException]("when there is a database error other than relating to preexisting schema")
  def initSchema(): Unit = {
    Tables.profile match {
      // migrate the schema if we're using mysql and the migration property is set
      case _: slick.jdbc.MySQLProfile if WARP_MIGRATE_SCHEMA.value.toBoolean =>
        logger.info(s"migrating mysql schema using flyway")
        this.migrate()
      // if we're using mysql but the migration property is not set, just log a message
      case _: slick.jdbc.MySQLProfile =>
        logger.info(
          s"""
             |mysql is being used, but ${WARP_MIGRATE_SCHEMA.propertyName}=${WARP_MIGRATE_SCHEMA.value}.
             |warp framework will assume the schema exists and has already been brought up to date
          """.stripMargin
        )
      // otherwise just use the generated classes. be aware that views won't exist.
      case _ =>
        logger.info("creating schema from generated slick classes. views will not exist.")
        val action = Tables.schema.create.transactionally.withTransactionIsolation(TransactionIsolation.Serializable)
        this.trySynchronously(action) match {
          case Success(_) =>
            logger.info("initialized schema")
          case Failure(exception) if exception.getMessage matches "Table .* already exists[\\S\\s]*" =>
            logger.trace("schema already exists")
          case Failure(exception) =>
            throw exception
        }
    }
  }


  /** Truncates the schema as defined in [[WARP_DATABASE_URL]], if it exists. */
  @throws[RuntimeException]("when there is a database error")
  def truncateSchema(): Unit = {
    // we need to disable referential integrity checks in order to truncate the tables
    val actions: DBIO[Unit] = DBIO.seq(
      sqlu"#${Tables.disableForeignKeys}",
      Tables.schema.truncate,
      sqlu"#${Tables.enableForeignKeys}"
    )

    this.synchronously(actions.transactionally.withTransactionIsolation(TransactionIsolation.Serializable))
  }


  /**
    * Adds warpReader and vulcan accounts to the specified MySQL database.
    *
    * A good faith attempt to create warpReader and vulcan accounts. These accounts are used by warp and SkyLab
    * reporting mechanisms.
    *
    * Has no effect if we are using a database other than MySQL.
    */
  private def createDbUsers(): Unit = Tables.profile match {
    case _: slick.jdbc.MySQLProfile =>
      val mySqlDbName: String = this.getMySQLDbName.get

      val queries: List[String] = List(
        s"grant select on `$mySqlDbName`.* to 'warpReader'             IDENTIFIED BY 'Readerwarp'"
        , s"grant select on `$mySqlDbName`.* to 'warpReader'@'localhost' IDENTIFIED BY 'Readerwarp'"
        , s"grant create, select, update on `$mySqlDbName`.* to 'vulcan'             IDENTIFIED BY 'Sp0ckDamitJim2016'"
        , s"grant create, select, update on `$mySqlDbName`.* to 'vulcan'@'localhost' IDENTIFIED BY 'Sp0ckDamitJim2016'"
      )

      val actions: List[DBIO[Int]] = queries map { query => sqlu"#$query" }
      this.trySynchronously(DBIO.sequence(actions)) match {
        case Success(_) =>
        case Failure(exception) =>
          // scalastyle:off
          // This failure can usually be fixed by setting up the grants correctly as shown below.
          //
          //  mysql> show grants for 'root'@'node1159.svc.devpg.pdx.wd';
          //  +-------------------------------------------------------------------------------------------------------------------+
          //  | Grants for root@node1159.svc.devpg.pdx.wd                                                                         |
          //  +-------------------------------------------------------------------------------------------------------------------+
          //  | GRANT ALL PRIVILEGES ON *.* TO 'root'@'node1159.svc.devpg.pdx.wd' IDENTIFIED BY PASSWORD 'XXXX' WITH GRANT OPTION |
          //  | GRANT PROXY ON ''@'' TO 'root'@'node1159.svc.devpg.pdx.wd' WITH GRANT OPTION                                      |
          //  +-------------------------------------------------------------------------------------------------------------------+
          //  2 rows in set (0.00 sec)
          // scalastyle:on
          logger.error(exception.getMessage)
      }
    case _ =>
  }
}

// main method, can be invoked to initialize the db schema
object CorePersistenceUtilsMain extends App with CorePersistenceAware

// main method, can be invoked to migrate the schema using flyway.
// only supported for mysql
object MigrateSchema extends App with MigrateSchemaLike with CorePersistenceAware

// main method, can be invoked to delete the schema before or after a build
object DropCoreSchema extends App with CanDropCoreSchema {
  this.dropSchema()
}
