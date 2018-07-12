package com.workday.warp.logger

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.Test
import org.junit.experimental.categories.Category
import org.pmw.tinylog.Level

/**
  * Created by tomas.mccandless on 6/8/18.
  */
class WarpLogUtilsSpec extends WarpJUnitSpec {

  @Test
  @Category(Array(classOf[UnitTest]))
  def parseLogLevel(): Unit = {

    WarpLogUtils.parseLevel("NOT_VALID_LEVEL", "DEBUG") should be (Level.DEBUG)
    WarpLogUtils.parseLevel("NOT_VALID_LEVEL", "ALSO_NOT_VALID_LEVEL") should be (Level.INFO)
  }
}
