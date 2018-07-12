package com.workday.warp.persistence

import java.time.LocalDate
import java.util.Date

import com.workday.warp.common.CoreWarpProperty.WARP_DATABASE_URL
import com.workday.warp.persistence.exception.PreExistingTagException
import com.workday.warp.persistence.TablesLike._
import org.pmw.tinylog.Logger

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

/**
  * Adds an abstract definition of AbstractPersistenceUtils to any Class that mixes in this trait.
  * Mix in this trait in abstract classes or traits that require persistenceUtils
  *
  * Created by leslie.lam on 2/9/18.
  */
trait PersistenceAware {

  /** [[AbstractPersistenceUtils]] to allow for db interaction */
  val persistenceUtils: AbstractPersistenceUtils

  // Initializes the database when you mix in PersistenceAware.
  this.initUtils()

  /**
    * Override this to initialize the correct PersistenceUtils companion object.
    * This ensures that a database with the correct schema is initialized.
    */
  def initUtils(): Unit


  /**
    * Utility functions for persisting results.
    * This is a protected trait within [[PersistenceAware]] so it is only accessible by classes that extend [[PersistenceAware]].
    */
  protected trait AbstractPersistenceUtils extends Connection with AbstractQueries with HasProfile {
    import profile.api._

    /**
      * Composes `find` and `create` into a single [[DBIOAction]], and runs that action synchronously. If `find` returns a
      * row, `create` will not be executed.
      *
      * @param find [[DBIO]] used to look up an existing row.
      * @param create [[DBIO]] used to insert a row if one does not exist.
      * @tparam Table type of the table that we are referencing.
      * @tparam Row type of the row that we are referencing.
      * @return preexisting [[Row]], or newly inserted [[Row]].
      */
    def findOrCreate[Table, Row](find: DBIO[Seq[Row]], create: DBIO[Row]): Row = {
      val action: DBIO[Row] = for {
        maybeRow <- find
        _ <- maybeRow.headOption match {
          case Some(row) => DBIO.successful(row)
          case None => create
        }
        // finally read back the row
        result <- find
      } yield result.head

      this.runWithRetries(action)
    }

    /**
      * Attempts to look up a [[BuildRowLike]] with the specified parameters. Will be created if it does not exist.
      *
      * @param major version of the build.
      * @param minor version of the build.
      * @param patch version of the build.
      * @return a [[BuildRowLike]] with the given year, week, and buildNumber.
      */
    def findOrCreateBuild(major: Int, minor: Int, patch: Int): BuildRowLike


    /**
      * Attempts to look up a [[TestDefinitionRowLike]] with the given method signature. Will be created if it does not exist.
      * If documentation does not match, then it will be updated
      *
      * @param methodSignature the method signature to look up.
      * @return a [[TestDefinitionRowLike]] with the given method signature.
      */
    def findOrCreateTestDefinition(methodSignature: String, documentation: Option[String] = None): TestDefinitionRowLike


    /**
      * Attempts to look up a [[MeasurementNameRowLike]] with the given name. Will be created if it does not exist.
      *
      * @param name key to look up, or create, a [[MeasurementNameRowLike]]
      * @return a [[MeasurementNameRowLike]] with the given name
      */
    def findOrCreateMeasurementName(name: String): MeasurementNameRowLike


    /**
      * Attempts to look up a [[TagNameRowLike]] with the given name. Will be created if it does not exist.
      *
      * @param name key to look up, or create, a [[TagNameRowLike]]
      * @return a [[TagNameRowLike]] with the given name
      */
    def findOrCreateTagName(name: String,
                                   nameType: String = "plain_txt",
                                   isUserGenerated: Boolean = true): TagNameRowLike

    /**
      * Creates, inserts, and returns a [[TestExecutionRowLike]].
      *
      * @param testId id of the measured test (usually fully qualified junit method).
      * @param documentation option containing documentation for the [[TestExecutionRowLike]]
      * @param timeStarted time the measured test was started.
      * @param responseTime observed duration of the measured test (seconds).
      * @param maxResponseTime maximum allowable response time set on the measured test (seconds).
      * @return a [[TestExecutionRowLike]] with the given parameters.
      */
    def createTestExecution(testId: String,
                       documentation: Option[String],
                       timeStarted: Date,
                       responseTime: Double,
                       maxResponseTime: Double): TestExecutionRowLike

    /**
      * Convenience function for creating a test execution with no documentation.
      *
      * @param testId id of the measured test (usually fully qualified junit method).
      * @param timeStarted time the measured test was started.
      * @param responseTime observed duration of the measured test (seconds).
      * @param maxResponseTime maximum allowable response time set on the measured test (seconds).
      * @return a [[TestExecutionRowLike]] with the given parameters and no documentation.
      */
    def createTestExecution(testId: String,
                       timeStarted: Date,
                       responseTime: Double,
                       maxResponseTime: Double): TestExecutionRowLike


    /**
      * Updates each [[TestExecutionRowLike]] in `testExecutions` to have the new provided threshold.
      *
      * When a threshold is set using the dsl, we don't have access to the threshold until the test has completed and the
      * [[org.scalatest.matchers.Matcher]] is being executed. At that time, we update the existing rows in the db with the
      * user-provided threshold.
      *
      * @param testExecutions collection of [[TestExecutionRowLike]] to update.
      * @param newThreshold new threshold to set for each row.
      */
    def updateTestExecutionThresholds[T: TestExecutionRowLikeType](testExecutions: Iterable[T], newThreshold: Double): Unit


    /**
      * Persists generic measurements in the Measurement table. Looks up the MeasurementName corresponding to
      * `name`, creates a new Measurement with the appropriate fields set.
      *
      * @param idTestExecution id for the [[TestExecutionLike]] associated with this measurement.
      * @param name name to use for this measurement.
      * @param result result of this measurement.
      */
    def recordMeasurement(idTestExecution: Int, name: String, result: Double): Unit


    /**
      * Persists generic tags in the [[TestExecutionTagLike]] table. Looks up the [[TagNameRowLike]] corresponding to
      * `name`, creates a new tag with the appropriate fields set.
      *
      * Behavior:
      *   If a tag with the given name and value exists in the database:
      *     Log a warning, but is a success.
      *   If a tag with the given name but different value exists in the database:
      *     Throw an error. This is a failure.
      *   If no tag with given name and idTestDefinition exists in the database:
      *     Success.
      *
      * @param idTestExecution id for the [[TestExecutionRowLike]] associated with this tag.
      * @param name name to use for this tag.
      * @param value value of the tag.
      * @return a [[TestExecutionTagRowLike]] with the given parameters.
      */
    @throws[scala.RuntimeException]
    @throws[PreExistingTagException]
    def recordTestExecutionTag(idTestExecution: Int,
                               name: String,
                               value: String,
                               isUserGenerated: Boolean = true): TestExecutionTagRowLike


    /**
      * Persists generic tags in the [[TestExecutionTagLike]] table. Looks up the [[TagNameRowLike]] corresponding to
      * `name`, creates a new tag with the appropriate fields set.
      *
      * @param testExecution [[TestExecutionRowLike]] associated with this tag.
      * @param name name to use for this tag.
      * @param value value of the tag.
      * @return a [[TestExecutionTagRowLike]] with the given parameters.
      */
    def recordTestExecutionTag[T: TestExecutionTagRowLikeType](testExecution: T,
                                                               name: String,
                                                               value: String,
                                                               isUserGenerated: Boolean): TestExecutionTagRowLike


    /**
      * Persists generic tags in the [[TestDefinitionTagLike]] table. Looks up the [[TagNameRowLike]] corresponding to
      * `name`, creates a new tag with the appropriate fields set.
      *
      * Behavior:
      *   If a tag with the given name and value exists in the database:
      *     Log a warning, but is a success.
      *   If a tag with the given name but different value exists in the database:
      *     Throw an error. This is a failure.
      *   If no tag with given name and idTestDefinition exists in the database:
      *     Success.
      *
      * @param idTestDefinition id for the [[TestDefinitionRowLike]] associated with this tag.
      * @param name name to use for this tag.
      * @param value value of the tag.
      * @return a [[TestDefinitionTagRowLike]] with the given parameters.
      */
    @throws[PreExistingTagException]
    def recordTestDefinitionTag(idTestDefinition: Int,
                                name: String,
                                value: String,
                                isUserGenerated: Boolean = true): TestDefinitionTagRowLike


    /**
      * Persists generic tags in the [[TestDefinitionTagLike]] table. Looks up the [[TagNameRowLike]] corresponding to
      * `name`, creates a new tag with the appropriate fields set.
      *
      * @param testDefinition [[TestDefinitionRowLike]] associated with this tag.
      * @param name name to use for this tag.
      * @param value value of the tag.
      * @return a [[TestDefinitionTagRowLike]] with the given parameters.
      */
    @throws[PreExistingTagException]
    def recordTestDefinitionTag[T: TestDefinitionRowLikeType](testDefinition: T,
                                                              name: String,
                                                              value: String,
                                                              isUserGenerated: Boolean): Unit = {
      this.recordTestDefinitionTag(implicitly[TestDefinitionRowLikeType[T]].idTestDefinition(testDefinition), name, value, isUserGenerated)
    }


    /**
      * Persists metatags into the [[TestDefinitionMetaTagLike]] table. Looks up the [[TagNameRowLike]] corresponding to
      * `name`, creates a new metatag with the appropriate fields set.
      *
      * Behavior:
      *   If a tag with the given name and value exists in the database:
      *     Log a warning, but is a success.
      *   If a tag with the given name but different value exists in the database:
      *     Throw an error. This is a failure.
      *   If no tag with given name and idTestDefinitionTag exists in the database:
      *     Success.
      *
      * @param idTestDefinitionTag id for the [[TestDefinitionTagRowLike]] associated with this tag
      * @param name name to use for this tag.
      * @param value value of the tag.
      */
    @throws[PreExistingTagException]
    def recordTestDefinitionMetaTag(idTestDefinitionTag: Int, name: String, value: String, isUserGenerated: Boolean = true): Unit


    /**
      * Persists metatags into the [[TestExecutionMetaTagLike]] table. Looks up the [[TagNameRowLike]] corresponding to
      * `name`, creates a new metatags with the appropriate fields et.
      *
      * Behavior:
      *   If a tag with the given name and value exists in the database:
      *     Log a warning, but is a success.
      *   If a tag with the given name but different value exists in the database:
      *     Throw an error. This is a failure.
      *   If no tag with given name and idTestDefinitionTag exists in the database:
      *     Success.
      *
      * @param idTestExecutionTag id for the [[TestExecutionTagRowLike]] associated with this tag
      * @param name name to use for this tag.
      * @param value value of the tag.
      */
    @throws[PreExistingTagException]
    def recordTestExecutionMetaTag(idTestExecutionTag: Int, name: String, value: String, isUserGenerated: Boolean = true): Unit


    /**
      * Gets historical response times (seconds) for running test identified by `identifier`.
      *
      * @param identifier [[IdentifierType]] containing the identifying parameters of the measured test.
      * @return a [[List]] of historical response times.
      */
    def getResponseTimes[I: IdentifierType](identifier: I): List[Double] = {
      this.synchronously(this.responseTimesQuery(identifier)).toList
    }


    /**
      * Gets historical response times (seconds) for running `testId` and `confidenceLevel`. The response time for the
      * [[TestExecutionRowLike]] with `excludeIdTestExecution` will be omitted.
      *
      * @param identifier [[IdentifierType]] containing the identifying parameters of the measured test.
      * @param excludeIdTestExecution idTestExecution to exclude from results.
      * @return a [[List]] of historical response times.
      */
    def getResponseTimes[I: IdentifierType](identifier: I, excludeIdTestExecution: Int): List[Double] = {
      this.synchronously(this.responseTimesQuery(identifier, excludeIdTestExecution)).toList
    }


    /**
      * Gets historical response times (seconds) for running `testId` and `confidenceLevel`. The response time for the
      * [[TestExecutionRowLike]] with `excludeIdTestExecution` and before 'startDateCutoff' will be omitted.
      *
      * @param identifier [[IdentifierType]] containing the identifying parameters of the measured test.
      * @param excludeIdTestExecution idTestExecution to exclude from results.
      * @param startDateLowerBound ignore all results before this date.
      * @return a [[List]] of historical response times.
      */
    def getResponseTimes[I: IdentifierType](identifier: I, excludeIdTestExecution: Int, startDateLowerBound: LocalDate): List[Double] = {
      this.synchronously(this.responseTimesQuery(identifier, excludeIdTestExecution, startDateLowerBound)).toList
    }


    /**
      * Reads the number of times a test has been executed successfully.
      * Note that this number includes those executions which have failed due to exceeding performance
      * performance requirements, but does not include any executions that failed due to other errors.
      *
      * @param identifier [[IdentifierType]] containing the identifying parameters of the measured test.
      * @return the number of times testId has been successfully executed
      */
    def getNumExecutions[I: IdentifierType](identifier: I): Int = this.getResponseTimes(identifier).length


    /**
      * @param identifier [[IdentifierType]] containing the identifying parameters of the measured test.
      * @return the average response time for `testId`.
      */
    def getAverageResponseTime[I: IdentifierType](identifier: I): Double = {
      val responseTimes = this.getResponseTimes(identifier)
      if (responseTimes.nonEmpty) responseTimes.sum / responseTimes.length.toDouble
      else Double.NaN
    }


    /**
      * @param identifier [[IdentifierType]] containing the identifying parameters of the measured test.
      * @return the mode of the response times of the specified test
      */
    def getModeResponseTime[I: IdentifierType](identifier: I): Double = {
      this.getResponseTimes(identifier)
        // group the list of response times into a map from each distinct to a list of the occurrences of that element
        // scala> List(3.14159, 4.5, 4.5).groupBy(identity)
        // res0: Map[Double, List[Double]] = Map(3.14159 -> List(3.14159), 4.5 -> List(4.5, 4.5))
        .groupBy(identity)
        // compute the maximum (k, v) according the size of the values (lists), then return the associated key
        .maxBy(_._2.size)._1
    }

    /** Computes the median of the given test.
      *
      * @param identifier [[IdentifierType]] containing the identifying parameters of the measured test.
      * @return the median of the response time of the specified test
      */
    def getMedianResponseTime[I: IdentifierType](identifier: I): Double


    /**
      * @param testExecution [[TestExecutionRowLike]] to get the method signature for.
      * @return method signature of `testExecution`
      */
    def getMethodSignature[T: TestExecutionRowLikeType](testExecution: T): String


    /**
      * @param name [[String]] name of TagNameRow
      * @return a [[TagNameRowLike]] with the given name
      */
    def getTagName(name: String): TagNameRowLike


    /**
      * Gets the entire row of a tag with id `idTagName` set on the [[TestExecutionRowLike]] with id `idTestExecution`.
      *
      * @param idTestExecution id of the [[TestExecutionRowLike]] to look up tags for.
      * @param idTagName id of the [[TagNameRowLike]] to look up.
      * @return a [[TestExecutionTagRowLike]] with the id `idTestExecution` and `idTagName`.
      */
    def getTestExecutionTagsRow(idTestExecution: Int, idTagName: Int): TestExecutionTagRowLike
  }
}

// trait for invokable objects to migrate the schema using flyway.
// only supported for mysql
trait MigrateSchemaLike extends PersistenceAware {

  /**
    * Simple retry logic for a call-by-name function.
    *
    * Defaults to 8 retry attempts.
    *
    * @param f function to retry.
    * @param retries number of retries to attempt.
    * @tparam U return type of `f`.
    * @return result of evaluating `f`.
    */
  @throws[RuntimeException]("when f fails and there are no retries remaining.")
  @tailrec private final def retry[U](f: => U, retries: Int = 8): U = {
    Try(f) match {
      case Success(result) => result
      case Failure(exception) if retries < 1 => throw exception
      case Failure(_) => retry(f, retries - 1)
    }
  }

  /**
    * Applies migrations to bring our schema up to date.
    *
    * Recursively scans `locations` to look for unapplied migration scripts.
    * If `locations` is empty, we'll allow flyway to use its default locations.
    *
    * The type of each entry is determined by its prefix:
    *   - no prefix (or "classpath:" prefix) indicates a location on the classpath
    *   - a "filesystem:" prefix points to a directory on the file system
    *
    * @param locations locations we'll recursively scan for migration scripts.
    */
  def migrate(locations: Seq[String] = Seq.empty): Unit = {
    this.persistenceUtils.maybeFlyway() match {
      case Some(flyway) =>
        if (locations.nonEmpty) {
          flyway.setLocations(locations: _*)
        }
        this.retry({
          val before: Long = System.currentTimeMillis()
          flyway.migrate()
          flyway.validate()
          val after: Long = System.currentTimeMillis()
          Logger.debug(s"migrated schema in ${after - before}ms")
        })
      case None =>
        val error: String = s"schema migration is only supported for mysql. check the value of ${WARP_DATABASE_URL.propertyName}"
        throw new RuntimeException(error)
    }
  }
}


