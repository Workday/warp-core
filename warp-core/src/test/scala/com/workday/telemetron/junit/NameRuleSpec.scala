package com.workday.telemetron.junit

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Created by leslie.lam on 12/12/17
  * Based on java class created by tomas.mccandless on 5/19/16.
  */
class NameRuleSpec extends WarpJUnitSpec {
  // create a second name rule that isn't applied
  private val unappliedNameRule = new TelemetronNameRule

  /**
    * Checks that we can read the fully qualified name of the executing test.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def testNameRule(): Unit = {
    classOf[NameRuleSpec].getName + ".testNameRule" should be (this.getTestId)
  }

  /**
    * Checks that an exception is thrown when we try to read testName from an unapplied name rule.
    */
  @Test(expected = classOf[IllegalStateException])
  @Category(Array(classOf[UnitTest]))
  def testUnappliedNameRule(): Unit = {
    this.unappliedNameRule.getTestName
  }
}
