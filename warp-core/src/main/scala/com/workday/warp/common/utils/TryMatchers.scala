package com.workday.warp.common.utils

import org.scalatest.matchers.{MatchResult, Matcher}

import scala.util.Try

/**
  * Scalatest matcher for [[Try]].
  *
  * Created by tomas.mccandless on 7/18/16.
  */
trait TryMatchers {

  /**
    * [[Matcher]] used to check that a [[Try]] succeeded.
    *
    * @tparam _ we don't care about the contained type, only whether the [[Try]] was a [[scala.util.Success]].
    */
  class SuccessMatcher[_] extends Matcher[Try[_]] {
    override def apply(left: Try[_]): MatchResult = MatchResult(left.isSuccess, s"$left did not succeed", s"$left suceeded")
  }


  /**
    * [[Matcher]] used to check that a [[Try]] failed.
    *
    * @tparam _ we don't care about the contained type, only whether the [[Try]] was a [[scala.util.Failure]].
    */
  class FailureMatcher[_] extends Matcher[Try[_]] {
    override def apply(left: Try[_]): MatchResult = MatchResult(left.isFailure, s"$left did not fail", s"$left failed")
  }


  /**
    * [[Matcher]] used to check that a [[Try]] succeeded and contains `held`.
    *
    * @param held should be equivalent to what is contained in the [[Try]].
    * @tparam T type of the [[Try]] we are matching.
    */
  class SuccessMatcherWithHold[T](val held: T) extends Matcher[Try[T]] {
    override def apply(left: Try[T]): MatchResult = {
      MatchResult(left.isSuccess && left.get.equals(held), s"$left did not hold $held", s"$left held $held")
    }
  }


  /**
    * Enables syntax like `Try(1 + 1) should succeed`.
    *
    * TODO feel free to rename this if you come up with a better name. Can't be named `succeed` to avoid clashing with
    * a final field in [[org.scalatest.Assertions]].
    *
    * @return a [[SuccessMatcher]] that verifies success.
    */
  def win: Matcher[Try[_]] = new SuccessMatcher


  /**
    * Enables syntax like `Try(1 + 1) should not die`.
    *
    * @return a [[FailureMatcher]] that verifies failure.
    */
  def die: Matcher[Try[_]] = new FailureMatcher


  /**
    * Enables syntax like `Try(1 + 1) should hold (2)`.
    *
    * @param held should be equivalent to what is contained in the [[Try]].
    * @tparam T type of the [[Try]] we are matching.
    * @return a [[SuccessMatcherWithHold]] that verifies success and equivalence of the wrapped result to `held`.
    */
  def hold[T](held: T): Matcher[Try[T]] = new SuccessMatcherWithHold[T](held)
}


// can be imported or mixed in
object TryMatchers extends TryMatchers
