package com.workday.warp.arbiters

import com.workday.warp.utils.StackTraceFilter
import com.workday.warp.TestId

import scala.collection.mutable

/** Container class to hold the results of arbiter votes.
  *
  * Created by tomas.mccandless on 1/27/16.
  */
class Ballot(val testId: TestId) extends StackTraceFilter {

  /** holds any throwables created by arbiters */
  val errors: mutable.ListBuffer[Throwable] = mutable.ListBuffer[Throwable]()


  /** Unboxes `maybeThrowable` and stores the boxed value if it is defined.
    *
    * @param maybeThrowable `Option[Throwable]` to unbox and inspect
    */
  def registerVote(maybeThrowable: Option[Throwable]): Unit = {
    maybeThrowable match {
      case Some(throwable) => this.errors.synchronized { this.errors += throwable }
      case None =>
    }
  }

  /** Throws an exception if any arbiters voted to fail the test.
    *
    * If `errors` is non-empty, creates a new exception containing all the failure messages and throws that exception.
    */
  @throws[RequirementViolationException]("when any performance requirements have been violated")
  def checkAndThrow(): Unit = {
    this.errors.synchronized {
      if (this.errors.nonEmpty) {
        val errorMessages: StringBuilder = new StringBuilder(Ballot.ERROR_MESSAGE_PREFIX)
        this.errors foreach { error: Throwable => errorMessages ++= s"\n    ${error.getMessage}" }
        throw this.filter(new RequirementViolationException(errorMessages.toString))
      }
    }
  }
}


object Ballot {
  val ERROR_MESSAGE_PREFIX: String = "the following performance requirements were not met:"
}
