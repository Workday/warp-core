package com.workday.telemetron

import com.workday.telemetron.annotation.Schedule
import com.workday.telemetron.junit.{SchedulingRule, TelemetronContext}
import com.workday.warp.common.category.UnitTest
import org.junit.experimental.categories.Category
import org.junit.{AfterClass, Before, Rule, Test}
import org.pmw.tinylog.Logger
import org.scalatest.Matchers

/**
  * Created by leslie.lam on 12/12/17
  * Based on java class created by michael.ottati on 8/22/15.
  */
class RepeatSpec extends Matchers {
  private[this] val _schedulingRule: SchedulingRule = new SchedulingRule(new ResultReporter, new TelemetronContext)

  @Rule
  def schedulingRule: SchedulingRule = _schedulingRule

  @Before
  def incrementInvocations(): Unit = {
    RepeatSpec.invocations += 1
  }

  /**
    * Because this test is using the SchedulingRule and not the TelemetronRule the timesTest below
    * will not actually fail from JUnits perspective. What this testcase is designed to do is to
    * show that invocation stops on failure. This is determined in the @AfterClass method.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  @Schedule(invocations = 4)
  def timesTest(): Unit = {
    Logger.trace(s"Invocations = ${RepeatSpec.invocations}")
    if (RepeatSpec.invocations == RepeatSpec.failureInvocation) {
      fail("Intentional failure to test that invocations get stopped.")
    }
  }
}

object RepeatSpec extends Matchers {
  /**
    * Invocation number on which we will intentionally fail the test
    */
  val failureInvocation: Int = 2

  /**
    * Number of times the test has been invoked
    */
  var invocations: Int = 0

  /**
    * Checks that the test was invoked exactly 3 times.
    */
  @AfterClass
  def verifyInvocationStopsOnTestFailure(): Unit = {
    RepeatSpec.invocations should be (RepeatSpec.failureInvocation)
  }

}
