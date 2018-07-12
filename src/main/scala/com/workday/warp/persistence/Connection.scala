package com.workday.warp.persistence

import java.net.URI

import com.workday.warp.common.CoreWarpProperty._
import com.workday.warp.common.exception.WarpConfigurationException
import com.workday.warp.persistence.exception.PreExistingTagException
import Tables.profile.api._
import Tables.profile.backend.DatabaseDef
import com.typesafe.config.ConfigFactory
import com.workday.warp.utils.SynchronousExecutor
import org.flywaydb.core.Flyway
import slick.dbio.DBIOAction
import org.pmw.tinylog.Logger
import slick.jdbc.{JdbcDataSource, TransactionIsolation}
import slick.util.ClassLoaderUtil

import scala.annotation.tailrec
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

/**
  * Created by tomas.mccandless on 10/6/16.
  */
trait Connection {

  /** @return name of the mysql database we will write to. */
  @throws[WarpConfigurationException]("when an invalid url is set as the value of javax.persistence.jdbc.url")
  private[persistence] def getMySQLDbName: Option[String] = {
    Option(Connection.url) flatMap { jdbcUrl: String =>
      if (jdbcUrl.startsWith("jdbc:h2")) {
        None
      }
      else if (jdbcUrl.startsWith("jdbc:mysql")) {
        // remove 'jdbc:', then remove leading '/'
        Option(URI.create(jdbcUrl drop 5).getPath.tail)
      }
      else {
        val propertyName: String = WARP_DATABASE_URL.propertyName
        val message: String = s"invalid value for $propertyName: $jdbcUrl (must start with 'jdbc:mysql' or 'jdbc:h2')"
        throw new WarpConfigurationException(message)
      }
    }
  }


  /**
    * Creates a [[Flyway]] instance based on warp properties
    *
    * Will be [[None]] iff we are not using mysql.
    *
    * @return an [[Option]] containing a [[Flyway]] instance for database schema migration.
    */
  def maybeFlyway(): Option[Flyway] = {
    this.getMySQLDbName map { _ =>
      val flyway: Flyway = new Flyway
      // initialize the metadata table if we don't have it already
      flyway.setBaselineOnMigrate(true)
      flyway.setDataSource(Connection.url, Connection.user, Connection.password)
      flyway
    }
  }


  /**
    * Executes `action` with transaction isolation level serializable. Retries will be attempted.
    *
    * @param action [[DBIO]] to execute.
    * @param retries number of retries to attempt. Defaults to 8.
    * @tparam Row return type of `action`.
    * @return the result of executing `action`.
    */
  @tailrec final def runWithRetries[Row](action: DBIO[Row], retries: Int = CorePersistenceConstants.RETRIES): Row = {
    this.trySynchronously(action.transactionally.withTransactionIsolation(TransactionIsolation.Serializable)) match {
      case Success(row) => row
        // TODO consider matching on just a rollback exception
      case Failure(exception: PreExistingTagException) => throw exception
      case Failure(exception) =>
        if (retries < 0) throw exception
        else {
          Logger.warn(s"error in database operation: ${exception.getMessage}\n going to retry.")
          // back off a bit before retrying
          Thread.sleep(50)
          this.runWithRetries(action, retries - 1)
        }
    }
  }


  /**
    * Executes `action` and returns the result.
    *
    * @param action the [[DBIOAction]] to execute.
    * @tparam Row return type of `action`.
    * @return the result of executing `action`.
    */
  @throws[RuntimeException]("when there is a database error")
  def synchronously[Row](action: DBIO[Row]): Row = this.trySynchronously(action).get


  /**
    * Executes `action` and returns the result.
    *
    * @param action the [[DBIOAction]] to execute.
    * @tparam Row return type of `action`.
    * @return the result of executing `action`.
    */
  @throws[RuntimeException]("when there is a database error")
  def synchronously[Row](action: DBIO[Seq[Row]]): Seq[Row] = this.trySynchronously(action).get

  /**
    * Converts `query` to a [[DBIOAction]] and executes it, unboxing the result.
    *
    * @param query [[Query]] that will be executed.
    * @tparam Table table that is referenced by `query`.
    * @tparam Row return type of `query`.
    * @return the result of executing `query`.
    */
  // TODO does this need to be more generic than seq?
  @throws[RuntimeException]("when there is a database error")
  def synchronously[Table, Row](query: Query[Table, Row, Seq]): Seq[Row] = this.trySynchronously(query).get


  /**
    * Converts `query` to a [[DBIOAction]] and executes it, returning the result wrapped in a [[Try]]
    *
    * @param query [[Query]] that will be executed.
    * @tparam Table table that is referenced by `query`.
    * @tparam Row return type of `query`.
    * @return [[Try]] containing the result of executing `query`.
    */
  def trySynchronously[Table, Row](query: Query[Table, Row, Seq]): Try[Seq[Row]] = this.trySynchronously(query.result)


  /**
    * Executes `action` and returns the result wrapped in a [[Try]]
    *
    * @param action the [[DBIO]] to execute.
    * @tparam Row return type of `action`.
    * @return [[Try]] containing the result of executing `action`.
    */
  def trySynchronously[Row](action: DBIO[Row]): Try[Row] = Try(Await.result(Connection.db.run(action), Duration.Inf))
}

object Connection {

  val driver: String = WARP_DATABASE_DRIVER.value
  val url: String = WARP_DATABASE_URL.value
  val user: String = WARP_DATABASE_USER.value
  val password: String = WARP_DATABASE_PASSWORD.value

  // scalastyle:off two.spaces
  val config: String =
    s"""
      |db {
      |  connectionTimeout = 30000
      |  driver = "$driver"
      |  url = "$url"
      |  user = "$user"
      |  password = "$password"
      |}
    """.stripMargin
  // scalastyle:on two.spaces

  var db: DatabaseDef = this.connect

  /** @return a synchronous database connection. */
  def connect: DatabaseDef = {
    val name: String = "db"
    val source = JdbcDataSource.forConfig(ConfigFactory.parseString(this.config).getConfig(name),
                                            None.orNull,
                                            name,
                                            ClassLoaderUtil.defaultClassLoader)
    val executor = new SynchronousExecutor

    Database.forSource(source, executor)
  }

  /** Shutdown and recreate our database connection. */
  def refresh(): Unit = {
      Await.result(this.db.shutdown, Duration.Inf)
      this.db = this.connect
  }
}
