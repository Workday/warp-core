package com.workday.warp.arbiters

import com.workday.telemetron.RequirementViolationException
import com.workday.warp.common.CoreWarpProperty._
import com.workday.warp.arbiters.traits.{ArbiterLike, CanReadHistory}
import com.workday.warp.persistence.TablesLike.TestExecutionRowLikeType
import com.workday.warp.persistence.Tables._
import com.workday.warp.utils.{AnnotationReader, Ballot}
import org.apache.commons.math3.distribution.NormalDistribution
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation
import org.pmw.tinylog.Logger

/**
  * Arbiter that checks whether the response time for this test was within an acceptable percentile.
  *
  * Constructs a [[NormalDistribution]] using the historical arithmetic mean and standard deviation, and evaluates
  * the cumulative probability of the measured response time.
  *
  * Created by tomas.mccandless on 1/25/16.
  */
class ZScoreArbiter extends CanReadHistory with ArbiterLike {

  /**
    * Reads historical response times for this test.
    *
    * @return a wrapped error with a useful message, or None if the measured test passed its requirement.
    */
  override def vote[T: TestExecutionRowLikeType](ballot: Ballot, testExecution: T): Option[Throwable] = {
    this.vote(this.responseTimes(ballot.testId, testExecution.idTestExecution), ballot, testExecution, WARP_ARBITER_MINIMUM_N.value.toInt)
  }



  /**
    * Reads the percentile threshold annotated on this test, computes the percentile of the historical response times for
    * this test and constructs a wrapped error if the most recent measured response time is above the specified threshold.
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
        s"$minimumHistoricalData.) percentile threshold processing will not continue.")
      None
    }
    else {
      val measuredResponseTime: Double = testExecution.responseTime
      val mean: Double = responseTimes.sum / responseTimes.size
      // make sure standard deviation is strictly positive
      val stdDev: Double = math.max((new StandardDeviation).evaluate(responseTimes.toArray, mean), Double.MinPositiveValue)
      // convert cdf value to a percentile
      val percentile: Double = 100 * new NormalDistribution(mean, stdDev).cumulativeProbability(measuredResponseTime)

      val percentileRequirement: Double = AnnotationReader.getZScoreRequirement(ballot.testId)
      // check that the percentile according to cumulative distribution function is less than the requirement
      if (percentile <= percentileRequirement) None
      else Option(new RequirementViolationException(
        s"${ballot.testId} failed requirement imposed by ${this.getClass.getName}. expected response time (measured " +
        s"$measuredResponseTime sec) percentile <= $percentileRequirement, but was $percentile")
      )
    }
  }
}
