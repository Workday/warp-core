package com.workday.warp.arbiters

import com.workday.telemetron.RequirementViolationException
import com.workday.warp.common.CoreWarpProperty._
import com.workday.warp.arbiters.traits.{ArbiterLike, CanReadHistory}
import com.workday.warp.persistence.TablesLike.TestExecutionRowLikeType
import com.workday.warp.persistence.Tables._
import com.workday.warp.utils.{AnnotationReader, Ballot}
import org.pmw.tinylog.Logger

/**
  * Arbiter that checks whether the response time for this test was within an acceptable percentage of the historical
  * arithmetic mean.
  *
  * Created by tomas.mccandless on 5/13/16.
  */
class PercentageDegradationArbiter extends CanReadHistory with ArbiterLike {

  /**
    * Checks that the measured test passed its performance requirement. If the requirement is failed, constructs an
    * error with a useful message wrapped in an Option. The response time of the measured test must not be more than the
    * specified percentage greater than the historical average.
    *
    * @param ballot box used to register vote result.
    * @param testExecution [[TestExecutionRowLikeType]] we are voting on.
    * @return a wrapped error with a useful message, or None if the measured test passed its requirement.
    */
  override def vote[T: TestExecutionRowLikeType](ballot: Ballot, testExecution: T): Option[Throwable] = {
    val minimumHistoricalData: Int = WARP_ARBITER_SLIDING_WINDOW_SIZE.value.toInt
    this.vote(this.responseTimes(ballot.testId, testExecution.idTestExecution), ballot, testExecution, minimumHistoricalData)
  }


  /**
    * Reads the percentage threshold annotated on this test, and constructs a wrapped error if the most recent measured
    * response time is above the specified threshold of the last 30 days arithmetic mean.
    *
    * Returns None if the number of recorded historical measurements is less than `minimumHistoricalData`
    *
    * @param responseTimes historical response times collected for this test.
    * @param minimumHistoricalData minimum amount of historical measurements required to process percentiles.
    * @return a wrapped error with a useful message, or None if the measured test passed its requirement.
    */
  def vote[T: TestExecutionRowLikeType](responseTimes: Iterable[Double],
           ballot: Ballot,
           testExecution: T,
           minimumHistoricalData: Int): Option[Throwable] = {

    // we don't have enough historical data yet
    if (responseTimes.size < minimumHistoricalData) {
      Logger.warn(s"not enough historical measurements for ${ballot.testId}. (found ${responseTimes.size}, we require " +
        s"$minimumHistoricalData.) percentage threshold processing will not continue.")
      None
    }
    else {
      val measuredResponseTime: Double = testExecution.responseTime
      val responseTimesForTraining: Iterable[Double] = responseTimes takeRight minimumHistoricalData
      val mean: Double = responseTimesForTraining.sum / responseTimesForTraining.size
      // compute the percentage amount that measured response time is above the mean
      val percentage: Double = (measuredResponseTime / mean - 1) * 100.0

      val percentageRequirement: Double = AnnotationReader.getPercentageDegradationRequirement(ballot.testId)

      if (percentage <= percentageRequirement) None
      else Option(new RequirementViolationException(
        this.failureMessage(ballot.testId) +
          s"response time ($measuredResponseTime sec) was $percentage% greater than historical average. " +
          s"should have been <= $percentageRequirement%")
      )
    }
  }
}
