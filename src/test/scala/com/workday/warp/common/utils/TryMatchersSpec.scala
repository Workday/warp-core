package com.workday.warp.common.utils

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.Test
import org.junit.experimental.categories.Category
import org.scalatest.exceptions.TestFailedException

import scala.util.Try

/**
  * Created by tomas.mccandless on 7/18/16.
  */
class TryMatchersSpec extends WarpJUnitSpec {

  /** Checks usage of scalatest [[org.scalatest.matchers.Matcher]] for [[Try]]. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def tryMatchers(): Unit = {
    // check that we can verify success
    Try(1 + 1) should win
    Try(1 + 1) should not (die)

    // check that we can verify failure
    Try(throw new RuntimeException) should die
    Try(throw new RuntimeException) should not (win)

    // check expected exceptions
    intercept[TestFailedException] { Try(1 + 1) should die }
    intercept[TestFailedException] { Try(throw new RuntimeException) should win }

    // check that the thrown TestFailedException contains the message of the underlying exception
    val exception: TestFailedException = intercept[TestFailedException] {
      Try(throw new RuntimeException("we failed")) should win
    }
    exception.getMessage should include ("we failed")

    // check that we can match the wrapped result
    Try(1 + 1) should hold (2)
  }
}
