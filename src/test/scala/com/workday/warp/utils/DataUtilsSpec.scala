package com.workday.warp.utils

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.Test
import org.junit.experimental.categories.Category

import scala.math.abs

/**
  * Created by tomas.mccandless on 6/11/18.
  */
class DataUtilsSpec extends WarpJUnitSpec {

  @Test
  @Category(Array(classOf[UnitTest]))
  def zeroStdDev(): Unit = {
    val standardized: Iterable[Double] = DataUtils.standardize(List(1, 2, 3, 4))
    val diffs: Iterable[Double] = standardized.zip(
      List(-1.161895003862225, -0.3872983346207417, 0.3872983346207417, 1.161895003862225)
    ).map { case (d1, d2) => abs(d1 - d2) }

    val epsilon: Double = 0.0001
    if (diffs.exists(_ > epsilon)) {
      throw new RuntimeException("$diffs exceeded maximum allowed tolerance threshold")
    }

    DataUtils.standardize(List(1, 1, 1, 1)) should be (List(0, 0, 0, 0))
  }
}
