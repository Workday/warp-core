package com.workday.warp.utils

import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import com.workday.warp.utils.Implicits.DecoratedTry

import scala.util.Try

/**
  * Created by tomas.mccandless on 10/24/16.
  */
class DecoratedTrySpec extends WarpJUnitSpec {

  /** Checks that we can clean up after a [[Try]]. */
  @UnitTest
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



  /** Checks that we can transform a [[Try]] to an [[Either]]. */
  @UnitTest
  def toEither(): Unit = {
    val aTry: Try[Int] = Try(1 + 1)
    aTry.toEither should be (Right(2))
    val msg: String = "something failed"
    val aFailedTry: Try[_] = Try(throw new RuntimeException(msg))
    aFailedTry.toEither.left.map(_.getMessage) should be (Left(msg))
  }
}
