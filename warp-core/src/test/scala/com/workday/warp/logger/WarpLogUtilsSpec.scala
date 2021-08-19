package com.workday.warp.logger

import ch.qos.logback.classic.Level
import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}

/**
  * Created by tomas.mccandless on 6/8/18.
  */
class WarpLogUtilsSpec extends WarpJUnitSpec with WarpLogging {

  @UnitTest
  def parseLogLevel(): Unit = {
    WarpLogUtils.parseLevel("NOT_VALID_LEVEL", Option("INFO")) should be (Level.INFO)
    WarpLogUtils.parseLevel("NOT_VALID_LEVEL", Option("ALSO_NOT_VALID_LEVEL")) should be (Level.DEBUG)
    WarpLogUtils.parseLevel("NOT_VALID_LEVEL", None) should be (Level.DEBUG)
    WarpLogUtils.parseLevel("ERROR") should be (Level.ERROR)
    WarpLogUtils.parseLevel("NOT_VALID_LEVEL") should be (Level.DEBUG)
  }

  @UnitTest
  def addFileWriter(): Unit = {
    WarpLogUtils.addFileWriter(WriterConfig("build/UnitTest.log", "com.workday.warp.logger", Level.DEBUG))
    logger.debug("foobar")
  }
}
