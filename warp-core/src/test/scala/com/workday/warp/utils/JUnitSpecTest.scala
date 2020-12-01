package com.workday.warp.utils

import com.workday.warp.TestIdImplicits._
import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import org.junit.jupiter.api.TestInfo

/**
  * Created by tomas.mccandless on 5/20/16.
  *
  * // TODO refactor getTestId
  */
class JUnitSpecTest extends WarpJUnitSpec {

  /** Checks that we can read test id from [[TestIdImplicits]]. */
  @UnitTest
  def nameTest(info: TestInfo): Unit = {
    info.id should be ("com.workday.warp.utils.JUnitSpecTest.nameTest")
  }
}
