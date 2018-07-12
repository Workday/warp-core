package com.workday.warp.arbiters

import java.time.Duration
import java.util.concurrent.TimeUnit

import com.workday.telemetron.RequirementViolationException
import com.workday.telemetron.utils.TimeUtils
import com.workday.warp.arbiters.traits.ArbiterLike
import com.workday.warp.common.utils.Implicits._
import com.workday.warp.persistence.CorePersistenceAware
import com.workday.warp.persistence.TablesLike.TestExecutionRowLikeType
import com.workday.warp.persistence.Tables._
import com.workday.warp.utils.Ballot

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
    * @param ballot   box used to register vote result.
    * @param testExecution [[TestExecutionRowLikeType]] we are voting on.
    * @return a wrapped error with a useful message, or None if the measured test passed its requirement.
    */
  override def vote[T: TestExecutionRowLikeType](ballot: Ballot, testExecution: T): Option[Throwable] = {
    val testId: String = this.persistenceUtils.getMethodSignature(testExecution)
    val threshold: Duration = testExecution.responseTimeRequirement.seconds

    val responseTime: Duration = Duration.ofNanos(TimeUtils.toNanos(testExecution.responseTime, TimeUnit.SECONDS))

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
}
