package com.workday.warp.collectors

import java.time.Duration

import com.workday.warp.common.CoreWarpProperty._
import com.workday.warp.TrialResult
import com.workday.warp.collectors.abstracts.AbstractMeasurementCollector
import com.workday.warp.common.utils.Implicits._
import com.workday.warp.common.utils.StackTraceFilter
import com.workday.warp.persistence.TablesLike._
import com.workday.warp.persistence.TablesLike.RowTypeClasses._
import com.workday.warp.persistence.Tables._
import com.workday.warp.persistence.CorePersistenceAware
import com.workday.warp.persistence.influxdb.InfluxDBClient

/**
  * Writes test response time thresholds to influxdb, and updates them in MySql.
  *
  * When using the DSL, we don't know the threshold until _after_ measurement has been completed and the test results
  * have been written. This class patches up the existing rows once we know the threshold.
  *
  * Created by tomas.mccandless on 7/12/16.
  *
  * @param testId fully qualified name of the method being measured.
  */
class ResponseTimeCollector(testId: String) extends AbstractMeasurementCollector(testId) {
  /**
    * Called prior to starting an individual test invocation.
    */
  override def startMeasurement(): Unit = {
    if (InfluxDBClient.client.isEmpty) {
      this.isEnabled = false
    }
  }


  /**
    * Called after finishing an individual test invocation. Persists test execution and threshold to influxdb.
    *
    * @param maybeTestExecution Optional field. If the test execution is None the client should
    *                      not attempt to write out to the database.
    */
  override def stopMeasurement[T: TestExecutionRowLikeType](maybeTestExecution: Option[T]): Unit = {
    maybeTestExecution foreach { testExecution => ResponseTimeCollector.persistThresholdInflux(List(testExecution)) }
  }
}


object ResponseTimeCollector extends InfluxDBClient with CorePersistenceAware with StackTraceFilter {

  private val dbName: String = WARP_INFLUXDB_HEAPHISTO_DB.value
  private val seriesName: String = "responseTimes"


  /**
    * Persists response time and threshold of `testExecution` to influxdb.
    *
    * @param testExecutions [[Iterable]] of [[TestExecutionRow]] to persist details of.
    * @param maybeThreshold Optional [[Duration]] to override the `responseTimeRequirement` field of `testExecutions`.
    */
  def persistThresholdInflux(testExecutions: Iterable[TestExecutionRowLike], maybeThreshold: Option[Duration] = None): Unit = {
    this.persistThresholds(this.dbName, this.seriesName, testExecutions, maybeThreshold)
  }


  /**
    * Updates thresholds in MySql, and also persists them in influxdb.
    *
    * @param results [[Iterable]] of [[com.workday.warp.TrialResult]] to persist details of.
    * @param threshold [[Duration]] to override the `responseTimeRequirement` field of `testExecutions`.
    */
  def updateThresholds(results: Iterable[TrialResult[_]], threshold: Duration): Unit = {
    val testExecutions: Iterable[TestExecutionRowLike] = results flatMap { _.maybeTestExecution }
    this.persistenceUtils.updateTestExecutionThresholds(testExecutions, threshold.doubleSeconds)
    this.persistThresholdInflux(testExecutions, Option(threshold))
  }
}
