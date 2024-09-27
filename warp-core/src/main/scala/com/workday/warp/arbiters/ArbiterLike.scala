package com.workday.warp.arbiters

import com.workday.warp.TestId
import com.workday.warp.config.CoreWarpProperty.{WARP_ARBITER_SPIKE_FILTER_ENABLED, WARP_ARBITER_SPIKE_FILTER_ALERT_ON_NTH}
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
    * Wraps a vote with spike filtering.
    *
    * @param ballot box used to register vote result.
    * @param testExecution [[TestExecutionRowLikeType]] we are voting on.
    * @param spikeFilterEnabled whether spike filtering is enabled.
    * @param alertOnNth exceed limit.
    * @return
    */
  final def voteWithSpikeFilter[T: TestExecutionRowLikeType](ballot: Ballot,
                                                             testExecution: T,
                                                             spikeFilterEnabled: Boolean,
                                                             alertOnNth: Int): Option[Throwable] = {
    // get a vote
    val maybeFailure = this.vote(ballot, testExecution)
    val tagName: String = s"failure-${this.getClass.getCanonicalName}"

    // tag this execution with failure reason
    // "failure-{class}", "message"
    maybeFailure.foreach { f =>
      val msg: String = Option(f.getMessage).getOrElse("null").take(255)
      this.persistenceUtils.recordTestExecutionTag(testExecution.idTestExecution, tagName, msg)
    }

    if (spikeFilterEnabled) {
      logger.trace(s"voting, spike filter is enabled")
      // check the last executions to see if they have a failure tag that matches
      val priorExecutionHasFailureTag: Boolean = priorExecutionsFailed(testExecution, tagName, alertOnNth)
      if (priorExecutionHasFailureTag) {
        logger.trace(s"sufficient prior consecutive failures, vote=${maybeFailure.nonEmpty}")
        maybeFailure
      }
      // no failure tag on the last execution, (first time failure), don't vote as a failure
      else {
        logger.trace(s"last executions did not fail, vote=false")
        None
      }
    }
    else {
      logger.trace(s"spike filter is disabled, vote=${maybeFailure.nonEmpty}")
      maybeFailure
    }
  }


  /**
    * Wraps a vote with spike filtering.
    *
    * @param ballot box used to register vote result.
    * @param testExecution [[TestExecutionRowLikeType]] we are voting on.
    * @return a wrapped error with a useful message, or None if the measured test passed its requirement.
    */
  final def voteWithSpikeFilter[T: TestExecutionRowLikeType](ballot: Ballot, testExecution: T): Option[Throwable] = {
    val methodSignature: String = this.persistenceUtils.getMethodSignature(testExecution)
    val (spikeFilterEnabled, alertOnNth) = spikeFilterSettings(methodSignature)
    voteWithSpikeFilter(ballot, testExecution, spikeFilterEnabled, alertOnNth)
  }


  /**
    * Whether spike filtering is enabled (notification settings).
    *
    * Order of precedence:
    * - WarpProperties
    * - DB
    * - defaults to off
    *
    * @return spike filtering settings.
    */
  def spikeFilterSettings(methodSignature: String): (Boolean, Int) = {
    var settings: (Boolean, Int) = this.persistenceUtils.getSpikeFilterSettings(methodSignature)
      .map(setting => (setting.spikeFilterEnabled, setting.alertOnNth))
      .getOrElse((false, 1))
    logger.trace(s"base spike filter settings: $settings")
    // allow individual overrides from properties if they are present
    Option(WARP_ARBITER_SPIKE_FILTER_ENABLED.value).foreach { f =>
      logger.trace(s"spike filter enabled override: $f")
      settings = settings.copy(_1 = f.toBoolean)
    }
    Option(WARP_ARBITER_SPIKE_FILTER_ALERT_ON_NTH.value).foreach { f =>
      logger.trace(s"spike filter alert on nth override: $f")
      settings = settings.copy(_2 = f.toInt)
    }
    settings
  }


  /**
    * Whether or not `testExecution` passed its requirement.
    *
    * @param ballot box used to register vote result.
    * @param testExecution [[TestExecutionRowLikeType]] we are voting on.
    * @return true iff the test passed.
    */
  def passed[T: TestExecutionRowLikeType](ballot: Ballot, testExecution: T): Boolean = {
    this.voteWithSpikeFilter(ballot, testExecution).isEmpty
  }


  /**
    * Registers the vote with `ballot` so it can be later analyzed and possibly thrown as an Exception.
    *
    * @param ballot box used to register vote result.
    * @param testExecution [[TestExecutionRowLikeType]] we are voting on.
    */
  def collectVote[T: TestExecutionRowLikeType](ballot: Ballot,
                                               testExecution: T): Unit = {
    ballot.registerVote(this.voteWithSpikeFilter(ballot, testExecution))
  }


  /**
    * Throws an exception iff the measured test did not pass its requirement.
    *
    * @param ballot box used to register vote result.
    * @param testExecution [[TestExecutionRowLikeType]] we are voting on.
    */
  def voteAndThrow[T: TestExecutionRowLikeType](ballot: Ballot,
                                                testExecution: T): Unit = {
    this.maybeThrow(this.voteWithSpikeFilter(ballot, testExecution))
  }


  /** Throws an exception iff the measured test did not pass its requirement. */
  def maybeThrow(maybeError: Option[Throwable]): Unit = maybeError.foreach(throw _)


  /**
    * Base failure message. Classes mixing in this trait should append further details.
    *
    * @param testId id of the measured test.
    * @return a generic failure message. Implementing arbiters should append further detail about the failure.
    */
  def failureMessage(testId: TestId): String = s"${testId.id} failed requirement imposed by ${this.getClass.getName}. "
}
