package com.workday.warp.utils

import com.workday.warp.junit.WarpJUnitSpec
import org.junit.jupiter.api.Test
import com.workday.warp.utils.Implicits.DecoratedInstant

import java.time.Instant

class DecoratedInstantSpec extends WarpJUnitSpec {

  @Test
  def instant(): Unit = {
    (Instant.now() - Instant.now()).toMillis should be < 1000L
  }
}
