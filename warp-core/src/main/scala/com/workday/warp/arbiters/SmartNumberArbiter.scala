package com.workday.warp.arbiters

import java.time.{Duration, LocalDate}
import java.util.concurrent.TimeUnit
import com.workday.warp.config.CoreWarpProperty._
import com.workday.warp.config.CoreConstants
import com.workday.warp.logger.WarpLogging
import com.workday.warp.utils.Implicits._
import com.workday.warp.math.linalg.{CanSmoothTimeSeries, RobustPcaRunner}
import com.workday.warp.persistence.TablesLike._
import com.workday.warp.persistence.Tables._
import com.workday.warp.persistence.exception.WarpFieldPersistenceException
import com.workday.warp.utils.TimeUtils

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

/**
  * Uses bisection method with [[RobustPca]] to automatically determine a static threshold.
  *
  * Should only consider successful test history.
  *
  * Created by tomas.mccandless on 9/13/16.
  */
class SmartNumberArbiter(val lPenalty: Double = WARP_ANOMALY_RPCA_L_PENALTY.value.toDouble,
                         val sPenaltyNumerator: Double = WARP_ANOMALY_RPCA_S_PENALTY_NUMERATOR.value.toDouble,
                         val useDoubleRpca: Boolean = WARP_ANOMALY_DOUBLE_RPCA.value.toBoolean,
                         val startDateLowerBound: LocalDate = CanReadHistory.DEFAULT_EPOCH_DAY,
                         val useSlidingWindow: Boolean = WARP_ARBITER_SLIDING_WINDOW.value.toBoolean,
                         val slidingWindowSize: Int = WARP_ARBITER_SLIDING_WINDOW_SIZE.value.toInt,
                         val toleranceFactor: Double = WARP_ANOMALY_RPCA_S_THRESHOLD.value.toDouble,
                         val smartScalarNumber: Double = WARP_ANOMALY_SMART_SCALAR.value.toDouble)
  extends CanReadHistory with CanSmoothTimeSeries with ArbiterLike with WarpLogging {

  /**
    * Persist smart threshold to the execution metatag table, associated to the TestDefinitionTag table by the rowID
    *
    * @param threshold the threshold to persist
    * @param testExecution the testExecution that the threshold was calculated for
    */
  private def tryRecordSmartThreshold[T: TestExecutionRowLikeType](threshold: Duration, testExecution: T) : Try[Unit] = {
    Try {
      val testExecutionTagRowId: Int = this.persistenceUtils.recordTestExecutionTag(
        testExecution.idTestExecution,
        CoreConstants.WARP_SPECIFICATION_FIELDS_STRING,
        "",
        isUserGenerated = false
      ).idTestExecutionTag

      persistenceUtils.recordTestExecutionMetaTag(testExecutionTagRowId, CoreConstants.SMART_THRESHOLD_STRING,
        threshold.doubleSeconds.toString, isUserGenerated = false)
    }
  }

  /**
    * Checks that the measured test passed its performance requirement. If the requirement is failed, constructs an
    * error with a useful message wrapped in an Option.
    *
    * @param ballot   box used to register vote result.
    * @param testExecution [[TestExecutionRowLikeType]] we are voting on.
    * @return a wrapped error with a useful message, or None if the measured test passed its requirement.
    */
  override def vote[T: TestExecutionRowLikeType](ballot: Ballot, testExecution: T): Option[Throwable] = {
    if (useSlidingWindow && slidingWindowSize < WARP_ANOMALY_RPCA_MINIMUM_N.value.toInt) {
      val err: Exception = new IllegalArgumentException(
          s"sliding window is enabled, but window size (${slidingWindowSize}) is less than " +
            s"${WARP_ANOMALY_RPCA_MINIMUM_N.propertyName} (${WARP_ANOMALY_RPCA_MINIMUM_N.value.toInt})"
      )
      logger.error(s"${this.getClass.getCanonicalName} could not finish voting: ", err)
      None
    }
    else {
      // we don't care about today's response time for this
      val rawResponseTimes: Iterable[Double] = this.successfulResponseTimes(ballot.testId.id, testExecution.idTestExecution,
        startDateLowerBound, useSlidingWindow, slidingWindowSize)
      val threshold: Duration = this.smartNumber(rawResponseTimes).seconds
      val responseTime: Duration = TimeUtils.toNanos(testExecution.responseTime, TimeUnit.SECONDS).nanoseconds

      if (threshold.isPositive) {
        tryRecordSmartThreshold(threshold, testExecution) match {
          case Success(_) =>
            logger.trace("Smart threshold successfully persisted to TestExecutionMetaTag table")
            this.vote(testExecution, responseTime, threshold)

          // fatal error threshold is value and try persistence fails
          case Failure(exception) =>
            logger.error(s"Smart threshold failed to persist to TestExecutionMetaTag table with exception $exception")
            Option(new WarpFieldPersistenceException(s"Smart Threshold failed to persist", exception))
        }
      }
      else {
        None
      }
    }
  }


  /**
    * Checks that the measured response time is not greater than the calculated threshold.
    *
    * @param testExecution [[TestExecutionRowLikeType]] we are voting on.
    * @param responseTime measured response time of the test execution.
    * @param threshold calculated threshold of the test history.
    * @tparam T
    * @return a wrapped error with a useful message, or None if the measured test passed its requirement.
    */
  private[this] def vote[T: TestExecutionRowLikeType](testExecution: T, responseTime: Duration, threshold: Duration): Option[Throwable] = {
    if (threshold.isPositive && responseTime > threshold) {
      val testId: String = this.persistenceUtils.getMethodSignature(testExecution)
      Option(new RequirementViolationException(
        s"$testId violated smart number requirement: expected ${threshold.humanReadable} (${threshold.toMillis} ms)" +
          s", but measured ${responseTime.humanReadable} (${responseTime.toMillis} ms)"
      ))
    }
    else {
      None
    }
  }


  /**
    * Determines the SMART number threshold for this test given historical response times. Uses bisection method to find
    * the minimum response time that would be flagged as an anomaly.
    *
    * @param rawResponseTimes unstandardized historical response times.
    * @return minimum approximate response time that would be considered anomalous given the provided historical data.
    *         Returns -1 if not enough historical data
    */
  def smartNumber(rawResponseTimes: Iterable[Double]): Double = {
    val runner: RobustPcaRunner = RobustPcaRunner(sPenaltyNumerator = this.sPenaltyNumerator,
      lPenalty = this.lPenalty,
      sThreshold = this.toleranceFactor,
      slidingWindowSize = this.slidingWindowSize,
      useSlidingWindow = this.useSlidingWindow,
      useDoubleRpca = this.useDoubleRpca)

    logger.debug(s"smart number arbiter got ${rawResponseTimes.size} historical data points")

    if (rawResponseTimes.size >= WARP_ANOMALY_RPCA_MINIMUM_N.value.toInt) {
      // if double rpca is enabled, then get all the normal historical measurements up front.
      // we'll use single rpca later to avoid repeating the same computation.
      val responseTimes: Iterable[Double] = if (this.useDoubleRpca) {
        runner.getNormalMeasurements(rawResponseTimes)
      }
      else {
        rawResponseTimes
      }

      this.smartNumber(
        this.smooth(responseTimes, k = WARP_ANOMALY_SMART_SMOOTHING.value.toInt),
        left = 0.0,
        right = 2.0 * responseTimes.max
      )
    }
    else {
      logger.info(
        s"not enough data points for smart threshold (got ${rawResponseTimes.size} but we require ${WARP_ANOMALY_RPCA_MINIMUM_N.value})"
      )
      -1
    }
  }


  /**
    * Determines the SMART number threshold for this test given historical response times. Uses bisection method to find
    * the minimum response time that would be flagged as an anomaly.
    *
    * @param rawResponseTimes unstandardized historical response times.
    * @param left lower bound for search interval.
    * @param right upper bound for search interval.
    * @param iterations current iteration number.
    * @return minimum approximate response time that would be considered anomalous given the provided historical data.
    */
  @tailrec
  private[this] def smartNumber(rawResponseTimes: Iterable[Double], left: Double, right: Double, iterations: Int = 0): Double = {
    val middle: Double = left + (right - left) / 2
    val isMiddleAnomaly: Boolean = this.isAnomaly(rawResponseTimes, middle)

    val almostMiddle: Double = middle * 0.99
    val isAlmostMiddleAnomaly: Boolean = this.isAnomaly(rawResponseTimes, almostMiddle)

    if ((isMiddleAnomaly && !isAlmostMiddleAnomaly) || iterations > WARP_ANOMALY_SMART_MAX_ITERATIONS.value.toInt) {
      logger.trace(s"smart number calculation took $iterations iterations")
      middle * smartScalarNumber
    }
    else if (isMiddleAnomaly && isAlmostMiddleAnomaly) {
      this.smartNumber(rawResponseTimes, left, middle, iterations + 1)
    }
    else {
      this.smartNumber(rawResponseTimes, middle, right, iterations + 1)
    }
  }


  /**
    * Uses single RPCA to check if `responseTime` is an anomaly given the historical data in `responseTimes`.
    *
    * @param rawResponseTimes unstandardized historical response times.
    * @param responseTime most recent historical response time.
    * @return true iff `responseTime` is an anomaly.
    */
  private[arbiters] def isAnomaly(rawResponseTimes: Iterable[Double], responseTime: Double): Boolean = {
    val runner: RobustPcaRunner = RobustPcaRunner(this.lPenalty, this.sPenaltyNumerator, this.toleranceFactor)
    runner.singleRobustPca(rawResponseTimes ++ List(responseTime)).exists(_.isAnomaly)
  }
}

object SmartNumberArbiter {
  val SMART_THRESHOLD_DESCRIPTION: String = "Smart Threshold"
}
