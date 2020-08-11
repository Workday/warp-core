package com.workday.warp.common.heaphistogram

import com.workday.warp.collectors.abstracts.AbstractMeasurementCollector
import com.workday.warp.persistence.{PersistenceAware, TablesLike}

/**
  * Collects and stores the Heap Histogram before and after measurement
  * @param testId WARP test name
  *
  * Created by vignesh.kalidas on 4/6/18.
  */
class HeapHistogramCollector(testId: String) extends AbstractMeasurementCollector(testId) with HistogramIoLike with PersistenceAware {
  /**
    * Called prior to starting an individual test invocation.
    */
  override def startMeasurement(): Unit = {
    this.collectAndStoreHistogram(testId)
  }

  /**
    * Called after finishing an individual test invocation.
    *
    * Collects and stores histogram regardless of the testExecution passed in
    */
  override def stopMeasurement[T: TablesLike.TestExecutionRowLikeType](maybeTestExecution: Option[T]): Unit = {
    this.collectAndStoreHistogram(testId)
  }
}
