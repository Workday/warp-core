package com.workday.warp.common.utils

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.common.utils.Implicits.DecoratedTry
import org.junit.Test
import org.junit.experimental.categories.Category

import scala.util.Try

/**
  * Created by tomas.mccandless on 10/24/16.
  */
class DecoratedTrySpec extends WarpJUnitSpec {

  /** Checks that we can clean up after a [[Try]]. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def lastly(): Unit = {
    var n: Int = 0

    val aTry: Try[Int] = Try(1 + 1)
    // check that lastly returns the original try
    aTry == (aTry andThen { n += 1 }) should be (true)
    n should be (1)

    val aFailedTry: Try[_] = Try(throw new RuntimeException)
    aFailedTry == (aFailedTry andThen { n += 1 }) should be (true)
    n should be (2)
  }
}
