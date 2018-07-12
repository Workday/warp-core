package com.workday.warp.common.utils

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Created by tomas.mccandless on 5/20/16.
  */
class JUnitSpecTest extends WarpJUnitSpec {

  /** Checks that we can read test id from [[com.workday.telemetron.junit.TelemetronNameRule]]. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def nameTest(): Unit = {
    this.getTestId should be ("com.workday.warp.common.utils.JUnitSpecTest.nameTest")
  }
}
