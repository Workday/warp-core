package com.workday.warp.common.utils

import java.time.Duration
import java.util.concurrent.TimeUnit

import com.workday.telemetron.utils.TimeUtils
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.experimental.categories.Category
import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.utils.Implicits.DecoratedDouble
import org.junit.Test

class DecoratedDoubleSpec extends WarpJUnitSpec {
  @Test
  @Category(Array(classOf[UnitTest]))
  def decoratedDoubleSpec(): Unit = {
    val timeUnit: Double = 10000.0

    val expectedNanoDuration: Duration = Duration ofNanos TimeUtils.toNanos(timeUnit, TimeUnit.NANOSECONDS)
    timeUnit.nanoseconds should be (expectedNanoDuration)
    timeUnit.nanos should be (expectedNanoDuration)
    timeUnit.nano should be (expectedNanoDuration)

    val expectedMicroDuration: Duration = Duration ofNanos TimeUtils.toNanos(timeUnit, TimeUnit.MICROSECONDS)
    timeUnit.microseconds should be (expectedMicroDuration)
    timeUnit.micros should be (expectedMicroDuration)
    timeUnit.micro should be (expectedMicroDuration)

    val expectedMilliDuration: Duration = Duration ofNanos TimeUtils.toNanos(timeUnit, TimeUnit.MILLISECONDS)
    timeUnit.milliseconds should be (expectedMilliDuration)
    timeUnit.millis should be (expectedMilliDuration)
    timeUnit.milli should be (expectedMilliDuration)

    val expectedSecondDuration: Duration = Duration ofNanos TimeUtils.toNanos(timeUnit, TimeUnit.SECONDS)
    timeUnit.seconds should be (expectedSecondDuration)
    timeUnit.second should be (expectedSecondDuration)

    val expectedMinuteDuration: Duration = Duration ofNanos TimeUtils.toNanos(timeUnit, TimeUnit.MINUTES)
    timeUnit.minutes should be (expectedMinuteDuration)
    timeUnit.minute should be (expectedMinuteDuration)

    val expectedHourDuration: Duration = Duration ofNanos TimeUtils.toNanos(timeUnit, TimeUnit.HOURS)
    timeUnit.hours should be (expectedHourDuration)
    timeUnit.hour should be (expectedHourDuration)

    val expectedDayDuration: Duration = Duration ofNanos TimeUtils.toNanos(timeUnit, TimeUnit.DAYS)
    timeUnit.days should be (expectedDayDuration)
    timeUnit.day should be (expectedDayDuration)
  }
}
