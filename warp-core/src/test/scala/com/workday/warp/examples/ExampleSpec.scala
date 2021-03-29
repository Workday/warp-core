package com.workday.warp.examples

import com.workday.warp.dsl._
import com.workday.warp.utils.Implicits._
import com.workday.warp.TestIdImplicits._
import com.workday.warp.junit.{Measure, WarpInfo, WarpJUnitSpec, WarpTest}
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.{Test, TestInfo}
import org.pmw.tinylog.Logger
import scala.language.postfixOps


/**
  * Created by tomas.mccandless on 2/9/21.
  */
class ExampleSpec extends WarpJUnitSpec {

  /** A plain vanilla junit test with no extensions. */
  @Test
  def vanilla(): Unit = {
    Logger.trace("only plain junit infra")
  }


  @WarpTest
  def testId(info: TestInfo): Unit = {
    // TestIdImplicits implicit conversion
    val testId: String = info.id
    Assertions.assertTrue("com.workday.warp.examples.ExampleSpec.testId" == testId)
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
    Assertions.assertTrue(info.testId == "com.workday.warp.examples.ExampleSpec.measuredWithInfo")
  }


  @Test
  def dslCollectors(testInfo: TestInfo): Unit = {
    using testId testInfo warmups 2 trials 2 measuring {
      1 + 1
    } should not exceed (5 seconds)
  }
}
