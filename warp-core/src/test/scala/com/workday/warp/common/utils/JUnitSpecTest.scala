package com.workday.warp.common.utils

import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.junit.UnitTest
import com.workday.warp.junit.TestIdConverters._
import org.junit.jupiter.api.TestInfo

/**
  * Created by tomas.mccandless on 5/20/16.
  *
  * // TODO refactor getTestId
  */
class JUnitSpecTest extends WarpJUnitSpec {

  /** Checks that we can read test id from [[com.workday.warp.junit.TestIdConverters]]. */
  @UnitTest
  def nameTest(info: TestInfo): Unit = {
    info.testId should be ("com.workday.warp.common.utils.JUnitSpecTest.nameTest")
  }
}
