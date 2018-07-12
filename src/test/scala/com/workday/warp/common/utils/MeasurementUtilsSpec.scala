package com.workday.warp.common.utils

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Created by tomas.mccandless on 6/11/18.
  */
class MeasurementUtilsSpec extends WarpJUnitSpec {

  @Test
  @Category(Array(classOf[UnitTest]))
  def heapUsage(): Unit = {
    val heap: String = MeasurementUtils.humanReadableHeapUsage
    heap should fullyMatch regex "\\d+ [KMG]B"
  }
}
