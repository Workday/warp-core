package com.workday.telemetron.junit

import java.time.Duration

import com.workday.telemetron.annotation.Required
import com.workday.telemetron.spec.TelemetronJUnitSpec
import com.workday.warp.common.category.UnitTest
import org.junit.Test
import org.junit.experimental.categories.Category



/**
  * Created by leslie.lam on 12/12/17
  * Based on java class created by tomas.mccandless on 7/11/16.
  */
class TelemetronContextSpec extends TelemetronJUnitSpec(shouldVerifyResponseTime = false) {

  /**
    * Checks that an exception is not thrown when we disable response time requirement checking.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  @Required(maxResponseTime = 1)
  def exceeds(): Unit = {
    this.telemetron.setResponseTime(Duration.ofSeconds(10))
  }
}
