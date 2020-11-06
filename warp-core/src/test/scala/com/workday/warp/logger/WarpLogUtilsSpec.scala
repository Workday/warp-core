package com.workday.warp.logger

import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import org.pmw.tinylog.Level

/**
  * Created by tomas.mccandless on 6/8/18.
  */
class WarpLogUtilsSpec extends WarpJUnitSpec {

  @UnitTest
  def parseLogLevel(): Unit = {

    WarpLogUtils.parseLevel("NOT_VALID_LEVEL", Option("DEBUG")) should be (Level.DEBUG)
    WarpLogUtils.parseLevel("NOT_VALID_LEVEL", Option("ALSO_NOT_VALID_LEVEL")) should be (Level.INFO)
    WarpLogUtils.parseLevel("NOT_VALID_LEVEL", None) should be (Level.INFO)
  }
}
