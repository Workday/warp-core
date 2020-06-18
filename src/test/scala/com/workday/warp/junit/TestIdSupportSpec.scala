package com.workday.warp.junit

import com.workday.warp.common.spec.WarpJUnitSpec

/**
  * Created by tomas.mccandless on 6/17/20.
  */
class TestIdSupportSpec extends WarpJUnitSpec with TestIdSupport {

  @UnitTest
  def testIdFromUniqueId(): Unit = {
    fromUniqueId("[engine:junit-jupiter]/[class:com.workday.warp.junit.MeasurementCallbacksSpec]/[method:foo()]") should
      be ("com.workday.warp.junit.MeasurementCallbacksSpec.foo")
    // repeated invocations will have [<invocation num>] appended
    fromUniqueId("[engine:junit-jupiter]/[class:com.workday.warp.junit.MeasurementCallbacksSpec]/[method:foo()][9]") should
      be ("com.workday.warp.junit.MeasurementCallbacksSpec.foo")
  }
}