package com.workday.warp.common.utils

import java.time.Duration

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.common.utils.Implicits._
import org.junit.Test
import org.junit.experimental.categories.Category
import org.scalatest.Matchers

/**
  * Created by tomas.mccandless on 7/22/16.
  */
class DecoratedIntSpec extends WarpJUnitSpec with Matchers {

  /** Checks usage of `times` */
  @Test
  @Category(Array(classOf[UnitTest]))
  def timesTest(): Unit = {
    5 times { 1 + 1 } should be (List(2, 2, 2, 2, 2))
    50 times { 2 + 2 } should (have length 50 and contain only 4)
  }


  /** Checks usage of [[Duration]] conversions. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def durationTest(): Unit = {
    (1 nano) should be (Duration.ofNanos(1))
    (5 nanos) should be (Duration.ofNanos(5))
    (5 nanoseconds) should be (Duration.ofNanos(5))
    (2000 microseconds) should be (Duration.ofMillis(2))
    (2000 micros) should be (Duration.ofMillis(2))
    (1 micro) should be (Duration.ofNanos(1000))
    (1 milli) should be (Duration.ofMillis(1))
    (5 millis) should be (Duration.ofMillis(5))
    (5 milliseconds) should be (Duration.ofMillis(5))
    (1 second) should be (Duration.ofSeconds(1))
    (5 seconds) should be (Duration.ofSeconds(5))
    (1 minute) should be (Duration.ofMinutes(1))
    (5 minutes) should be (Duration.ofMinutes(5))
    (1 hour) should be (Duration.ofHours(1))
    (5 hours) should be (Duration.ofHours(5))
    (1 day) should be (Duration.ofDays(1))
    (5 days) should be (Duration.ofDays(5))
  }
}
