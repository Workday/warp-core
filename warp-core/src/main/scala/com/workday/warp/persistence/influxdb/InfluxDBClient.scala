package com.workday.warp.persistence.influxdb

import java.time.Duration
import java.util.concurrent.TimeUnit

import com.workday.warp.config.CoreConstants
import com.workday.warp.utils.Implicits._
import com.workday.warp.config.CoreWarpProperty._
import com.workday.warp.exception.WarpConfigurationException
import com.workday.warp.heaphistogram.{HeapHistogram, HeapHistogramEntry}
import com.workday.warp.persistence.CorePersistenceAware
import com.workday.warp.persistence.TablesLike._
import com.workday.warp.persistence.Tables._
import com.workday.warp.utils.StackTraceFilter
import org.influxdb.dto.{BatchPoints, Point, Pong, Query, QueryResult}
import org.influxdb.{InfluxDB, InfluxDBFactory}
import org.pmw.tinylog.Logger

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/**
 * InfluxDB client that handles persistence of heap histograms.
 *
 * Created by tomas.mccandless on 9/21/15.
 */
trait InfluxDBClient extends StackTraceFilter with CorePersistenceAware {

  /**
   * Persists a heap histogram in influxdb.
   *
   * @param histo the histogram to persist.
   * @param dbName the database to use for persistence.
   * @param seriesName the series (also referred to as a measurement) to use for persistence.
   */
  def persistHeapHistogram(histo: HeapHistogram, dbName: String, seriesName: String, warpTestName: String): Try[Unit] = {
    InfluxDBClient.maybeClient match {
      case Left(error) => Failure(new WarpConfigurationException(error))
      case Right(client) =>
        // create the database if necessary
        if (!this.databaseExists(dbName).getOrElse(false)) {
          this.createDatabase(dbName)
        }

        // timestamp to use for all points in this batch
        val timestamp: Long = System.currentTimeMillis

        val points: BatchPoints = InfluxDBClient.batch(dbName)

        // add a point to the batch for each histogram entry
        histo.histogramEntries foreach { histoEntry: HeapHistogramEntry =>
          points.point(Point
            .measurement(seriesName)
            .time(timestamp, TimeUnit.MILLISECONDS)
            // add a tag to ensure points are considered unique by influxdb.
            // without this tag only the last point in the batch will be retained because all points in the batch will be
            // considered identical as they have identical timestamps
            .tag("className", histoEntry.className)
            .tag("warpTestName", warpTestName)
            .addField("numInstances", histoEntry.numInstances)
            .addField("numBytes", histoEntry.numBytes)
            .build
          )
        }
        Try(client.write(points))
    }
  }

  /**
    * Creates [[BatchPoints]] from `testExecutions` to persist to Influx
    *
    * @param dbName name of the database to persist
    * @param seriesName name of series for points
    * @param testExecutions Iterable of [[TestExecutionRowLikeType]] to persist
    * @param threshold Optional override for threshold because it might come from the dsl. If this is [[None]], we'll use
    *                  `responseTimeRequirement` field of `testExecution`.
    * @return [[BatchPoints]] of points to persist to Influx
    */
  def addPoints[T: TestExecutionRowLikeType](dbName: String,
                                        seriesName: String,
                                        testExecutions: Iterable[T],
                                        threshold: Option[Duration] = None): BatchPoints = {
    val points: BatchPoints = InfluxDBClient.batch(dbName)

    testExecutions foreach { testExecution: T =>
      val methodSignature: Try[String] = Try(this.persistenceUtils.getMethodSignature(testExecution))

      points.point(Point
        .measurement(seriesName)
        // default to current time if the testcase doesnt have a start time set
        .time(Try(testExecution.startTime.getTime).getOrElse(System.currentTimeMillis), TimeUnit.MILLISECONDS)
        // default to undefined test id if the testcase doesnt have an associated method signature
        .tag("warpTestName", methodSignature.getOrElse(CoreConstants.UNDEFINED_TEST_ID))
        .tag("build", SILVER_BUILD_NUMBER.value)
        .addField("responseTime", testExecution.responseTime)
        // try to use the duration we're given, otherwise look at the testcase instance
        .addField("threshold", threshold match {
        case Some(duration) => duration.doubleSeconds
        case None => testExecution.responseTimeRequirement
      })
        .build
      )
    }

    points
  }


  /**
    * Persists response time and threshold for an [[Iterable]] of [[TestExecutionRow]].
    *
    * @param dbName name of the database to write into.
    * @param seriesName name of the series to write into.
    * @param testExecutions [[Iterable]] of [[TestExecutionRow]] to write response time and threshold for.
    * @param threshold Optional override for threshold because it might come from the dsl. If this is [[None]], we'll use
    *                  `responseTimeRequirement` field of `testExecution`.
    */
  def persistThresholds[T: TestExecutionRowLikeType](dbName: String,
                                                seriesName: String,
                                                testExecutions: Iterable[T],
                                                threshold: Option[Duration] = None): Try[Unit] = {

    InfluxDBClient.maybeClient match {
      case Left(error) => Failure(new WarpConfigurationException(error))
      case Right(client) =>
        // create the database if necessary
        if (!this.databaseExists(dbName).getOrElse(false)) {
          this.createDatabase(dbName)
        }

        val points = addPoints(dbName, seriesName, testExecutions, threshold)

        Try(client.write(points))
    }
  }


  /**
    * Persists response time and threshold for a [[TestExecutionRow]].
    *
    * @param dbName name of the database to write into.
    * @param seriesName name of the series to write into.
    * @param testExecution [[TestExecutionRow]] to write response time and threshold for.
    * @param threshold Optional override for threshold because it might come from the dsl. If this is [[None]], we'll use
    *                  `responseTimeRequirement` field of `testExecution`.
    */
  def persistThreshold[T: TestExecutionRowLikeType](dbName: String,
                                               seriesName: String,
                                               testExecution: T,
                                               threshold: Option[Duration] = None): Try[Unit] = {
    this.persistThresholds(dbName, seriesName, List(testExecution), threshold)
  }


  /**
   * @param databaseName name of the database to look up.
   * @return true iff databaseName exists as a database in InfluxDB and we have a successful connection.
   */
  def databaseExists(databaseName: String): Try[Boolean] = {
    val showQuery: Query = new Query("SHOW DATABASES", databaseName)
    InfluxDBClient.maybeClient match {
      case Left(error) => Failure(new WarpConfigurationException(error))
      case Right(client) => Try {
        val results: Seq[QueryResult.Result] = client.query(showQuery).getResults.asScala
        val databaseNames: Seq[String] = for {
          res <- results
          serie <- res.getSeries.asScala
          value <- serie.getValues.asScala
          name <- value.asScala
        } yield name.toString

        databaseNames.exists(_.equals(databaseName))
      }
    }
  }


  /**
    * Deletes the database with the specified name.
    *
    * @param database name of the database to delete.
    * @return
    */
  def dropDatabase(database: String): Try[Unit] = {
    val dropQuery: Query = new Query(s"""DROP DATABASE "$database"""", database)

    InfluxDBClient.maybeClient match {
      case Left(error) => Failure(new WarpConfigurationException(error))
      case Right(client) => Try(client.query(dropQuery))
    }
  }


  /**
    * Creates a database with the specified name.
    *
    * @param database
    * @return
    */
  def createDatabase(database: String): Try[Unit] = {
    val createQuery: Query = new Query(s"""CREATE DATABASE "$database"""", database)

    InfluxDBClient.maybeClient match {
      case Left(error) => Failure(new WarpConfigurationException(error))
      case Right(client) => Try(client.query(createQuery))
    }
  }


  /**
   * @return a Pong object describing the deployed influxdb server
   */
  def ping: Try[Pong] = {
    InfluxDBClient.maybeClient match {
      case Left(error) => Failure(new WarpConfigurationException(error))
      case Right(client) => Try(client.ping)
    }
  }
}


object InfluxDBClient {

  private val retentionPolicy: String = WARP_INFLUXDB_RETENTION_POLICY.value
  private val url: String = WARP_INFLUXDB_URL.value
  private val user: String = WARP_INFLUXDB_USER.value
  private val password: String = WARP_INFLUXDB_PASSWORD.value

  /** [[Either]] containing an error message, or an [[InfluxDB]]. Use this to write datapoints to influxdb. */
  val maybeClient: Either[String, InfluxDB] = this.connect(this.url, this.user, this.password)

  /**
    * Constructs a [[BatchPoints]].
    *
    * @param dbName name of the influxdb database to use.
    * @param retentionPolicy retention policy to use. Default to "default".
    * @return an instance of [[BatchPoints]] ready to accept points.
    */
  def batch(dbName: String, retentionPolicy: String = this.retentionPolicy): BatchPoints = {
    BatchPoints
      .database(dbName)
      .retentionPolicy(retentionPolicy)
      .build
  }


  /** @return an InfluxDB connection based on the values set in WarpProperty. */
  protected[influxdb] def connect(url: String, user: String, password: String): Either[String, InfluxDB] = {
    val influx: InfluxDB = InfluxDBFactory.connect(url, user, password)

    Try(influx.ping) match {
      case Failure(exception) =>
        val error: String =
          s"unable to connect to influxdb at $url using credentials (user = $user, password = $password)"
        Logger.warn(error, exception.getMessage)
        Left(error)
      case Success(_) =>
        Right(influx)
    }
  }
}
