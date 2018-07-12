package com.workday.telemetron.utils

import java.time.Duration
import java.util.concurrent.TimeUnit

import com.workday.warp.common.category.UnitTest
import org.junit.Test
import org.junit.experimental.categories.Category
import org.scalatest.Matchers

/**
  * A simple test to verify time formatting.
  *
  * Created by leslie.lam on 12/12/17
  * Based on java class created by michael.ottati on 9/17/15.
  */
class TimeUtilsSpec extends Matchers {

  /**
    * Checks that we can create a human-readable [[Duration]].
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def humanReadableDuration(): Unit = {
    TimeUtils.humanReadableDuration(Duration.ofHours(1).plusMinutes(1).plusSeconds(1).plusMillis(5)) should be ("1:01:01.005")
    TimeUtils.humanReadableDuration(Duration.ofMillis(123)) should be ("0:00:00.123")
    TimeUtils.humanReadableDuration(Duration.ofDays(1)) should be ("24:00:00.000")
  }


  /**
    * Checks that we can convert to nanoseconds.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def convertToNanos(): Unit = {
    TimeUtils.toNanos(20.5, TimeUnit.NANOSECONDS) should be (20L)
    TimeUtils.toNanos(20.5, TimeUnit.MICROSECONDS) should be (20500L)
    TimeUtils.toNanos(1000.5, TimeUnit.MICROSECONDS) should be (1000500L)
    TimeUtils.toNanos(2.5, TimeUnit.MILLISECONDS) should be (2500000L)
    TimeUtils.toNanos(2.3456, TimeUnit.MILLISECONDS) should be (2345600L)
    TimeUtils.toNanos(1, TimeUnit.SECONDS) should be (1000000000L)
    TimeUtils.toNanos(2.5, TimeUnit.SECONDS) should be (2500000000L)
    TimeUtils.toNanos(3.5, TimeUnit.SECONDS) should be (3500000000L)
    TimeUtils.toNanos(1, TimeUnit.MINUTES) should be (60000000000L)
    TimeUtils.toNanos(1.5, TimeUnit.MINUTES) should be (90000000000L)
    TimeUtils.toNanos(1, TimeUnit.HOURS) should be (3600000000000L)
    TimeUtils.toNanos(1.5, TimeUnit.HOURS) should be (5400000000000L)
    TimeUtils.toNanos(12, TimeUnit.HOURS) should be (TimeUtils.toNanos(0.5, TimeUnit.DAYS))
  }


  /**
    * Checks that we can convert milliseconds to seconds.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def millisecondConversion(): Unit = {
    TimeUtils.millisToDoubleSeconds(1000) should be (1.0d)
    TimeUtils.millisToDoubleSeconds(1234) should be (1.234d)
  }


  /**
    * Checks that we can convert nanoseconds to seconds.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def nanosecondConversion(): Unit = {
    TimeUtils.nanosToDoubleSeconds(1000000000) should be (1.0d)
    TimeUtils.nanosToDoubleSeconds(1234000000) should be (1.234d)
  }


  /**
    * Checks that we can create a human-readable version of milliseconds.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def humanReadableMillis(): Unit = {
    TimeUtils.millisToHumanReadable(4000) should be ("0:00:04.000")
  }
}
