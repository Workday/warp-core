package com.workday.warp.utils

import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}

/**
  * Created by tomas.mccandless on 6/11/18.
  */
class MeasurementUtilsSpec extends WarpJUnitSpec {

  @UnitTest
  def heapUsage(): Unit = {
    val heap: String = MeasurementUtils.humanReadableHeapUsage
    heap should fullyMatch regex "\\d+ [KMG]B"
  }
}
