package com.workday.warp.common.utils

import org.apache.commons.io.FileUtils.byteCountToDisplaySize

/**
  * Utility functions for dealing with byte values.
  *
  * Created by tomas.mccandless on 10/18/16.
  */
object MeasurementUtils {

  /** @return current heap usage as a [[Long]]. */
  def heapUsed: Long = {
    val runtime: Runtime = Runtime.getRuntime
    runtime.totalMemory - runtime.freeMemory
  }


  /** @return human-readable form of current heap usage. */
  def humanReadableHeapUsage: String = byteCountToDisplaySize(this.heapUsed)
}
