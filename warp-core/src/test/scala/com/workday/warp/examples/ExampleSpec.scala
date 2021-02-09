package com.workday.warp.examples

import com.workday.warp.TestIdImplicits._
import com.workday.warp.junit.{Measure, WarpInfo, WarpTest}
import org.junit.Assert
import org.junit.jupiter.api.{Test, TestInfo}
import org.pmw.tinylog.Logger

/**
  * Created by tomas.mccandless on 2/9/21.
  */
class ExampleSpec {

  /** A plain vanilla junit test with no extensions. */
  @Test
  def vanilla(): Unit = {
    Logger.trace("only plain junit infra")
  }


  @Test
  def testId(info: TestInfo): Unit = {
    // TestIdImplicits implicit conversion
    val testId: String = info.id
    Assert.assertTrue("com.workday.warp.examples.ExampleSpec.testId" == testId)
  }


  @Test
  @Measure
  def measuredOnly(): Unit = {
    Logger.trace("we are being measured but not repeated")
  }


  /** A test that will be invoked a total of 6 times, 2 unmeasured warmups and 4 measured trials. */
  @WarpTest(warmups = 1, trials = 2)
  def measured(): Unit = {
    Logger.trace("we are being measured")
  }


  /** Annotated WarpTests can also use the same parameter provider mechanism to pass WarpInfo. */
  @WarpTest
  def measuredWithInfo(info: WarpInfo): Unit = {
    Assert.assertTrue(info.testId == "com.workday.warp.examples.ExampleSpec.measuredWithInfo")
  }
}
