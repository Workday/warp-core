package com.workday.telemetron.utils

import java.time.temporal.Temporal
import java.time.{Duration, Instant}
import java.util.concurrent.TimeUnit

import com.workday.telemetron.RequirementViolationException
import com.workday.telemetron.annotation.Required
import com.workday.telemetron.utils.TimeUtils.toNanos
import com.workday.warp.common.utils.Implicits._

/**
  * Utility implicits.
  *
  * Created by leslie.lam on 12/15/17.
  */
// TODO should be consolidated with common.utils.Implicits and moved to utils.Implicits
object Implicits {

  /**
    * Decorates [[Required]] with nice failure functions.
    *
    * @param requirement
    */
  implicit class DecoratedRequired(requirement: Required) {

    /**
      * Checks if the time requirement has been violated
      *
      * @param responseTime A [[Duration]] containing the response time of the test.
      * @return Option containing a Throwable if the time requirement is violated.
      */
      // TODO do we need some other context?
    def failedTimeRequirement(responseTime: Duration, verifyResponseTime: Boolean = true): Option[Throwable] = {
      val threshold: Double = this.requirement.maxResponseTime
      val timeUnit: TimeUnit = this.requirement.timeUnit
      val maxResponseTime: Duration = Duration.ofNanos(toNanos(threshold, timeUnit))

      if (verifyResponseTime && maxResponseTime.isPositive && responseTime > maxResponseTime) {
        val error: String = s"Response time requirement exceeded, " +
          s"specified: ${maxResponseTime.humanReadable} (${maxResponseTime.toMillis} ms) " +
          s"observed: ${responseTime.humanReadable} (${responseTime.toMillis} ms)"
        Some(new RequirementViolationException(error))
      }
      else {
        None
      }
    }
  }

  implicit class DecoratedInstant(instant: Instant) {

    /**
      * Subtracts `other` from this [[Instant]].
      *
      * @param other
      * @return
      */
    def -(other: Temporal): Duration = Duration.between(other, this.instant)
  }
}
