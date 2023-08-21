package com.workday.warp.arbiters

import java.time.Duration
import java.util.concurrent.TimeUnit

import com.workday.warp.utils.Implicits._
import com.workday.warp.persistence.CorePersistenceAware
import com.workday.warp.persistence.TablesLike.TestExecutionRowLikeType
import com.workday.warp.persistence.Tables._
import com.workday.warp.utils.TimeUtils

/**
  * Simple arbiter based on a provided static threshold.
  *
  * Created by tomas.mccandless on 7/7/16.
  */
class ResponseTimeArbiter extends ArbiterLike with CorePersistenceAware {

  /**
    * Checks that the measured test passed its performance requirement. If the requirement is failed, constructs an
    * error with a useful message wrapped in an Option.
    *
    * Assumes that the threshold has already been set on the corresponding test execution, we don't directly consult
    * any annotations here.
    *
    * @param ballot   box used to register vote result.
    * @param testExecution [[TestExecutionRowLikeType]] we are voting on.
    * @return a wrapped error with a useful message, or None if the measured test passed its requirement.
    */
  override def vote[T: TestExecutionRowLikeType](ballot: Ballot, testExecution: T): Option[Throwable] = {
    val testId: String = this.persistenceUtils.getMethodSignature(testExecution)
    val threshold: Duration = this.getThreshold(testExecution)

    val responseTime: Duration = this.getResponseTime(testExecution)

    if (threshold.isPositive && responseTime > threshold) {
      Option(new RequirementViolationException(
        s"$testId violated response time requirement: expected ${threshold.humanReadable} (${threshold.toMillis} ms)" +
        s", but measured ${responseTime.humanReadable} (${responseTime.toMillis} ms)"
      ))
    }
    else {
      None
    }
  }


  /**
    * Gets response time from the test execution. Subclasses may have a different notion of where to obtain response time.
    *
    * @param testExecution [[TestExecutionRowLikeType]] we are voting on.
    * @return test response time as a [[Duration]].
    */
  def getResponseTime[T: TestExecutionRowLikeType](testExecution: T): Duration = {
    TimeUtils.toNanos(testExecution.responseTime, TimeUnit.SECONDS).nanoseconds
  }


  /**
    * Gets test response time threshold from the test execution. Subclasses may have a different notion of where to obtain
    * a threshold. For example, [[SpikeFilterSettings]].
    *
    * @param testExecution [[TestExecutionRowLikeType]] we are voting on.
    * @return test response time threshold as a [[Duration]].
    */
  def getThreshold[T: TestExecutionRowLikeType](testExecution: T): Duration = {
    testExecution.responseTimeRequirement.seconds
  }
}
