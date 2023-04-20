package com.workday.warp.arbiters

import com.workday.warp.TestId
import com.workday.warp.config.CoreWarpProperty.WARP_ARBITER_FLAPPING
import com.workday.warp.persistence.PersistenceAware
import com.workday.warp.persistence.TablesLike._
import com.workday.warp.persistence.Tables._

/**
  * Represents a requirement imposed on a measured test.
  *
  * Created by tomas.mccandless on 1/25/16.
  */
trait ArbiterLike extends PersistenceAware with CanReadHistory {

  /** Whether this arbiter is enabled. */
  var isEnabled: Boolean = true // scalastyle:ignore

  /**
    * Checks that the measured test passed its performance requirement. If the requirement is failed, constructs an
    * error with a useful message wrapped in an Option.
    *
    * @param ballot box used to register vote result.
    * @param testExecution [[TestExecutionRowLikeType]] we are voting on.
    * @return a wrapped error with a useful message, or None if the measured test passed its requirement.
    */
  def vote[T: TestExecutionRowLikeType](ballot: Ballot, testExecution: T): Option[Throwable]


  final def voteWithFlappingDetection[T: TestExecutionRowLikeType](ballot: Ballot, testExecution: T): Option[Throwable] = {
    // get a vote
    val maybeFailure = this.vote(ballot, testExecution)
    val tagName: String = s"failure-${this.getClass.getCanonicalName}"

    // tag this execution with failure reason
    // "failure-{class}", "message"
    maybeFailure.foreach { f =>
      this.persistenceUtils.recordTestExecutionTag(testExecution.idTestExecution, tagName, f.getMessage)
    }

    // if flapping detection is enabled, check the prior execution to see if it has tag for same failure reason
    val flappingDetectionEnabled: Boolean = isFlappingDetectionEnabled

    // if the last execution has a tag of "failure reason"
    // always need to write "failure-reason-{class}", message
    // if the last execution passed, but this one failed, then write "flapped-{class}" tag
    // only write "flapped-{class}", true, if the last execution doesnt have a failure reason-class

    // if so, fail,
    if (flappingDetectionEnabled) {
      // check the last execution to see if it has a failure tag that matches
      val priorExecutionHasFailureTag: Boolean = priorExecutionFailed(testExecution, tagName)
      if (priorExecutionHasFailureTag) maybeFailure
      // no failure tag on the last execution, (first time failure), don't vote as a failure
      else None
    }
    else {
      maybeFailure
    }
  }


  def isFlappingDetectionEnabled: Boolean = {
    WARP_ARBITER_FLAPPING.value.toBoolean // TODO also consider notification settings table
  }


  /**
    * Whether or not `testExecution` passed its requirement.
    *
    * @param ballot box used to register vote result.
    * @param testExecution [[TestExecutionRowLikeType]] we are voting on.
    * @return true iff the test passed.
    */
  def passed[T: TestExecutionRowLikeType](ballot: Ballot, testExecution: T): Boolean = this.vote(ballot, testExecution).isEmpty


  /**
    * Registers the vote with `ballot` so it can be later analyzed and possibly thrown as an Exception.
    *
    * @param ballot box used to register vote result.
    * @param testExecution [[TestExecutionRowLikeType]] we are voting on.
    */
  def collectVote[T: TestExecutionRowLikeType](ballot: Ballot,
                                               testExecution: T): Unit = ballot.registerVote(this.vote(ballot, testExecution))


  /**
    * Throws an exception iff the measured test did not pass its requirement.
    *
    * @param ballot box used to register vote result.
    * @param testExecution [[TestExecutionRowLikeType]] we are voting on.
    */
  def voteAndThrow[T: TestExecutionRowLikeType](ballot: Ballot,
                                                testExecution: T): Unit = this.maybeThrow(this.vote(ballot, testExecution))


  /** Throws an exception iff the measured test did not pass its requirement. */
  def maybeThrow(maybeError: Option[Throwable]): Unit = {
    maybeError match {
      case Some(error) => throw error
      case None =>
    }
  }


  /**
    * Base failure message. Classes mixing in this trait should append further details.
    *
    * @param testId id of the measured test.
    * @return a generic failure message. Implementing arbiters should append further detail about the failure.
    */
  def failureMessage(testId: TestId): String = s"${testId.id} failed requirement imposed by ${this.getClass.getName}. "
}
