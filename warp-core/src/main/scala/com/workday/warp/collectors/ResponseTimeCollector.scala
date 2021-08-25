package com.workday.warp.collectors

import com.workday.warp.config.CoreWarpProperty._
import com.workday.warp.inject.WarpGuicer
import com.workday.warp.persistence.TablesLike._
import com.workday.warp.persistence.influxdb.InfluxDBClient

/**
  * Writes test response time thresholds to influxdb, and updates them in MySql.
  *
  * When using the DSL, we don't know the threshold until _after_ measurement has been completed and the test results
  * have been written. This class patches up the existing rows once we know the threshold.
  *
  * Created by tomas.mccandless on 7/12/16.
  */
class ResponseTimeCollector extends AbstractMeasurementCollector {

  private val dbName: String = WARP_INFLUXDB_HEAPHISTO_DB.value
  private val seriesName: String = "responseTimes"

  /**
    * Called prior to starting an individual test invocation.
    */
  override def startMeasurement(): Unit = {
    if (InfluxDBClient.maybeClient.isLeft) {
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
    val influx: InfluxDBClient = WarpGuicer.getInfluxDb
    maybeTestExecution foreach { testExecution => influx.persistThresholds(this.dbName, this.seriesName, Seq(testExecution)) }
  }
}
