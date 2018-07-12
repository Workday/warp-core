package com.workday.warp.collectors.traits

import java.util.concurrent.atomic.AtomicBoolean

import com.workday.warp.common.CoreWarpProperty._
import com.workday.warp.collectors.abstracts.AbstractMeasurementCollector
import com.workday.warp.persistence.TablesLike.TestExecutionRowLikeType


/**
  * Represents the capability for continuous measurement collection.
  *
  * A separate thread is created to periodically invoke sampleMeasurement().
  *
  * Created by tomas.mccandless on 8/24/15.
  */
trait ContinuousMeasurement extends AbstractMeasurementCollector {

  var measurementInProgress: AtomicBoolean = new AtomicBoolean(false)
  var measurementIntervalMs: Int = WARP_CONTINUOUS_MEASUREMENT_INTERVAL.value.toInt

  // separate thread will sample a measurement at a configurable interval
  val collector: Thread = new Thread {
    override def run(): Unit = {
      while (measurementInProgress.get) {
        collectMeasurement()
        Thread sleep measurementIntervalMs
      }
    }
  }



  /**
   * Starts a new thread collector that will periodically collect measurement samples.
   */
  override final def startMeasurement(): Unit = {
    this.measurementInProgress.set(true)
    this.collector.start()
  }



  /**
    * Marks that measurement is no longer in progress, which causes the collector thread to end.
    *
    * @param maybeTestExecution     Optional field. If the test execution is None the client should
    *                          not attempt to write out to the database.
    */
  override final def stopMeasurement[T: TestExecutionRowLikeType](maybeTestExecution: Option[T]): Unit = {
    this.measurementInProgress.set(false)
  }



  /**
   * Collects a single sample of measurement. Invoked periodically throughout the duration of a warp test.
   * Should be overridden to provide the actual implementation of measurement sampling.
   */
  def collectMeasurement(): Unit
}
