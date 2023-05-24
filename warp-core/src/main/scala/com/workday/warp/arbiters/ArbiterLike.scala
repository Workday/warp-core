package com.workday.warp.arbiters

import com.workday.warp.TestId
import com.workday.warp.config.CoreWarpProperty.{WARP_ARBITER_FLAPPING, WARP_ARBITER_FLAPPING_NUM_EXCEED}
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


  /**
    *
    * @param ballot box used to register vote result.
    * @param testExecution [[TestExecutionRowLikeType]] we are voting on.
    * @param flappingDetectionEnabled whether flapping detection is enabled.
    * @param numToExceed exceed limit.
    * @return
    */
  final def voteWithFlappingDetection[T: TestExecutionRowLikeType](ballot: Ballot,
                                                                   testExecution: T,
                                                                   flappingDetectionEnabled: Boolean,
                                                                   numToExceed: Int): Option[Throwable] = {
    // get a vote
    val maybeFailure = this.vote(ballot, testExecution)
    val tagName: String = s"failure-${this.getClass.getCanonicalName}"

    // tag this execution with failure reason
    // "failure-{class}", "message"
    maybeFailure.foreach { f =>
      val msg: String = Option(f.getMessage).getOrElse("null")
      this.persistenceUtils.recordTestExecutionTag(testExecution.idTestExecution, tagName, msg)
    }

    if (flappingDetectionEnabled) {
      // check the last executions to see if they have a failure tag that matches
      val priorExecutionHasFailureTag: Boolean = priorExecutionsFailed(testExecution, tagName, numToExceed)
      if (priorExecutionHasFailureTag) maybeFailure
      // no failure tag on the last execution, (first time failure), don't vote as a failure
      else None
    }
    else {
      maybeFailure
    }
  }


  /**
    * Wraps a vote with flapping detection based on notification settings.
    *
    * @param ballot box used to register vote result.
    * @param testExecution [[TestExecutionRowLikeType]] we are voting on.
    * @return a wrapped error with a useful message, or None if the measured test passed its requirement.
    */
  final def voteWithFlappingDetection[T: TestExecutionRowLikeType](ballot: Ballot, testExecution: T): Option[Throwable] = {
    val (flappingEnabled, numToExceed) = isFlappingDetectionEnabled
    voteWithFlappingDetection(ballot, testExecution, flappingEnabled, numToExceed)
  }


  /**
    * Whether flapping detection is enabled (notification settings).
    * TODO should consult notification settings db table in addition to properties.
    *
    * @return notification settings.
    */
  def isFlappingDetectionEnabled: (Boolean, Int) = {
    (WARP_ARBITER_FLAPPING.value.toBoolean, WARP_ARBITER_FLAPPING_NUM_EXCEED.value.toInt)
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
