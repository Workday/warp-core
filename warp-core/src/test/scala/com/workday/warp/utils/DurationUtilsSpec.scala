package com.workday.warp.utils

import java.time.Duration

import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import com.workday.warp.utils.Implicits.DecoratedDuration

/**
  * Created by tomas.mccandless on 5/4/16.
  */
class DurationUtilsSpec extends WarpJUnitSpec {

  @UnitTest
  def durationComparison(): Unit = {
    (Duration.ofMillis(5) < Duration.ofMillis(4)) should be (false)
    (Duration.ofMillis(5) < Duration.ofMillis(5)) should be (false)
    (Duration.ofMillis(5) < Duration.ofMillis(6)) should be (true)

    (Duration.ofMillis(5) <= Duration.ofMillis(4)) should be (false)
    (Duration.ofMillis(5) <= Duration.ofMillis(5)) should be (true)
    (Duration.ofMillis(5) <= Duration.ofMillis(6)) should be (true)

    (Duration.ofMillis(5) >= Duration.ofMillis(4)) should be (true)
    (Duration.ofMillis(5) >= Duration.ofMillis(5)) should be (true)
    (Duration.ofMillis(5) >= Duration.ofMillis(6)) should be (false)

    (Duration.ofMillis(5) > Duration.ofMillis(4)) should be (true)
    (Duration.ofMillis(5) > Duration.ofMillis(5)) should be (false)
    (Duration.ofMillis(5) > Duration.ofMillis(6)) should be (false)

    (Duration.ofMillis(5) max Duration.ofMillis(4)) should be (Duration ofMillis 5)
    (Duration.ofMillis(4) max Duration.ofMillis(5)) should be (Duration ofMillis 5)
  }

  @UnitTest
  def durationArithmetic(): Unit = {
    (Duration.ofMillis(5) + Duration.ofMillis(4)) should be (Duration ofMillis 9)
    (Duration.ofMillis(5) - Duration.ofMillis(4)) should be (Duration ofMillis 1)
    (Duration.ofMillis(5) * 4.0) should be (Duration ofMillis 20) // Double
    (Duration.ofMillis(5) * 4) should be (Duration ofMillis 20) // Long
  }
}
