package com.workday.telemetron.junit

import java.time.Duration

import com.workday.telemetron.annotation.Required
import com.workday.telemetron.spec.TelemetronJUnitSpec
import com.workday.warp.common.category.UnitTest
import org.junit.Test
import org.junit.experimental.categories.Category
import org.pmw.tinylog.Logger
import org.scalatest.Matchers


/**
  * Tests various aspects of RuleChains
  *
  * Created by leslie.lam on 12/12/17
  * Based on java class created by michael.ottati on 8/20/15.
  */
class RepeatTimeRuleChainSpec extends TelemetronJUnitSpec with Matchers {

  /**
    * Verify that test runs in under 1 second.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  @Required(maxResponseTime = 1)
  def subSecondDuration(): Unit = {
    Logger.debug("single test")
    this.telemetron.getResponseTime.getSeconds should be < 1L
  }

  /**
    * Verify that user can set/read a duration longer that what actually occurred.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def setTwoHourDuration(): Unit = {
    this.telemetron.setResponseTime(Duration.ofHours(2))
    this.telemetron.getResponseTime.toHours should be (2L)
    this.telemetron.getElapsedTime.getSeconds should be < 1L
  }
}
