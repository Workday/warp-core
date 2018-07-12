package com.workday.telemetron.junit

import java.time.Duration
import java.util.concurrent.TimeUnit

import com.workday.telemetron.RequirementViolationException
import com.workday.telemetron.annotation.Required
import com.workday.telemetron.spec.HasTelemetron
import com.workday.warp.common.category.UnitTest
import org.junit.experimental.categories.Category
import org.junit.Test
import org.scalatest.Matchers


/**
  * Tests to verify that RequirementViolationException are thrown when appropriate.
  *
  * Created by leslie.lam on 12/12/17
  * Based on java class created by michael.ottati on 9/11/15.
  */
class RequirementSpec extends HasTelemetron with Matchers {

  @Test
  @Category(Array(classOf[UnitTest]))
  def shouldVerify(): Unit = {
    this.shouldVerifyResponseTime should be (true)
  }

  @Test
  @Category(Array(classOf[UnitTest]))
  @Required(maxResponseTime = 1, timeUnit = TimeUnit.SECONDS)
  def lessThan(): Unit = {}

  @Test
  @Category(Array(classOf[UnitTest]))
  @Required(maxResponseTime = 1040, timeUnit = TimeUnit.MILLISECONDS)
  def equalResponseTimes(): Unit = {
    this.telemetron.setResponseTime(Duration.ofMillis(1040))
  }

  /**
    * Checks that an exception is thrown if we attempt to set response time more than once.
    */
  @Test(expected = classOf[IllegalStateException])
  @Category(Array(classOf[UnitTest]))
  def setDurationTwice(): Unit = {
    this.telemetron.setResponseTime(Duration.ofMillis(1040))
    this.telemetron.setResponseTime(Duration.ofMillis(1050))
  }

  @Test
  @Category(Array(classOf[UnitTest]))
  @Required(maxResponseTime = 1, timeUnit = TimeUnit.SECONDS)
  def testName(): Unit = {
    classOf[RequirementSpec].getName + ".testName" should be (this.telemetron.getTestName)
  }

  @Test
  @Category(Array(classOf[UnitTest]))
  @Required(maxResponseTime = 10, timeUnit = TimeUnit.MILLISECONDS)
  def exceedsThreshold(): Unit = {
    val thrown = this.telemetron.getThrown
    thrown.expect(classOf[RequirementViolationException])
    thrown.expectMessage("Response time requirement exceeded, specified: 0:00:00.010 (10 ms)")
    try {
      Thread.sleep(11)
    }
    catch {
      case _: InterruptedException => Thread.currentThread.interrupt()
    }
  }
}
