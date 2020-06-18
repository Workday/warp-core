package com.workday.warp.junit

import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.pmw.tinylog.Logger

/**
  * Created by tomas.mccandless on 6/17/20.
  */
@ExtendWith(Array(classOf[MeasurementExtension]))
class MeasurementExtensionSpec {

  @UnitTest
  def foo(): Unit = {
    Logger.info("hekk")
  }

  @UnitTest
  def fooWithTestInfo(info: TestInfo): Unit = {
    Logger.info(s"info: ${info.getDisplayName}")
  }
}
