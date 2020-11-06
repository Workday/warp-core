package com.workday.warp.config.utils

import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import com.workday.warp.utils.MeasurementUtils

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
