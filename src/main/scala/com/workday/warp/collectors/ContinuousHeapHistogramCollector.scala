package com.workday.warp.collectors

import com.workday.warp.collectors.traits.ContinuousMeasurement
import com.workday.warp.common.heaphistogram.HeapHistogramCollector

/**
  * Continuously collects the heap histogram. Mixes in ContinuousMeasurement so it's invoked periodically
  * @param testId WARP test name
  *
  * Created by vignesh.kalidas on 4/6/18.
  */
class ContinuousHeapHistogramCollector(testId: String)
    extends HeapHistogramCollector(testId) with ContinuousMeasurement {
  /**
    * Collects a single sample of measurement. Invoked periodically throughout the duration of a warp test.
    * Should be overridden to provide the actual implementation of measurement sampling.
    */
  override def collectMeasurement(): Unit = {
    this.collectAndStoreHistogram(testId)
  }
}
