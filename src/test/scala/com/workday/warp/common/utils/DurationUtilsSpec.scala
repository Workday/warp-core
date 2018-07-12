package com.workday.warp.common.utils

import java.time.Duration

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.common.utils.Implicits.DecoratedDuration
import org.junit.Test
import org.junit.experimental.categories.Category
import org.scalatest.Matchers

/**
  * Created by tomas.mccandless on 5/4/16.
  */
class DurationUtilsSpec extends WarpJUnitSpec with Matchers {

  @Test
  @Category(Array(classOf[UnitTest]))
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

  @Test
  @Category(Array(classOf[UnitTest]))
  def durationArithmetic(): Unit = {
    (Duration.ofMillis(5) + Duration.ofMillis(4)) should be (Duration ofMillis 9)
    (Duration.ofMillis(5) - Duration.ofMillis(4)) should be (Duration ofMillis 1)
    (Duration.ofMillis(5) * 4.0) should be (Duration ofMillis 20) // Double
    (Duration.ofMillis(5) * 4) should be (Duration ofMillis 20) // Long
  }
}
