package com.workday.warp

import com.workday.warp.utils.Implicits._
import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}

/**
  * Created by tomas.mccandless on 6/8/18.
  */
class TrialResultSpec extends WarpJUnitSpec {

  /**
    * Checks that aux constructors and apply methods give the same result.
    */
  @UnitTest
  def auxConstructors(): Unit = {
    val result: TrialResult[_] = new TrialResult(10 milliseconds, 15 milliseconds)
    val result2: TrialResult[_] = TrialResult(10 milliseconds, 15 milliseconds)

    result should be (result2)
  }
}
