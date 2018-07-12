package com.workday.warp

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.common.utils.Implicits._
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Created by tomas.mccandless on 6/8/18.
  */
class TrialResultSpec extends WarpJUnitSpec {

  /**
    * Checks that aux constructors and apply methods give the same result.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def auxConstructors(): Unit = {
    val result: TrialResult[_] = new TrialResult(10 milliseconds, 15 milliseconds)
    val result2: TrialResult[_] = TrialResult(10 milliseconds, 15 milliseconds)

    result should be (result2)
  }
}
