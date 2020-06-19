package com.workday.warp.junit

import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.jupiter.api.TestInfo
import org.pmw.tinylog.Logger

/**
  * Created by tomas.mccandless on 6/17/20.
  */
@MeasuredTest
class MeasurementExtensionSpec extends WarpJUnitSpec {

  @UnitTest
  def foo(): Unit = {
    (1 + 1) should be (2)
  }

  @UnitTest
  def fooWithTestInfo(info: TestInfo): Unit = {
    Logger.info(s"info: ${info.getDisplayName}")
  }
}
