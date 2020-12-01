package com.workday.warp.math.linalg

import com.workday.warp.TestId
import com.workday.warp.config.CoreWarpProperty._
import com.workday.warp.math.standardize
import com.workday.warp.utils.WarpStopwatch
import org.pmw.tinylog.Logger

import scala.collection.mutable

/**
  * Thin wrapper around [[RobustPca]].
  *
  * Holds penalty-related parameters, implements double rpca.
  *
  * Performs Augmented Dickey-Fuller test to check for time series stationarity.
  *
  * Created by tomas.mccandless on 9/14/16.
  */
case class RobustPcaRunner(lPenalty: Double = WARP_ANOMALY_RPCA_L_PENALTY.value.toDouble,
                           // the final value here is a function of the number of measurements being analyzed, so we only
                           // pass in the numerator
                           sPenaltyNumerator: Double = WARP_ANOMALY_RPCA_S_PENALTY_NUMERATOR.value.toDouble,
                           sThreshold: Double = WARP_ANOMALY_RPCA_S_THRESHOLD.value.toDouble,
                           slidingWindowSize: Int = WARP_ARBITER_SLIDING_WINDOW_SIZE.value.toInt,
                           requiredMeasurements: Int = WARP_ANOMALY_RPCA_MINIMUM_N.value.toInt,
                           useSlidingWindow: Boolean = WARP_ARBITER_SLIDING_WINDOW.value.toBoolean,
                           useDoubleRpca: Boolean = WARP_ANOMALY_DOUBLE_RPCA.value.toBoolean,
                           useDiff: Option[Boolean] = Option(WARP_ANOMALY_USE_DIFF.value).map(_.toBoolean)) {

  /**
    * Attempts to run robust principal component analysis on the provided list of response times. Today's measurement should
    * be the last item in `rawResponseTimes`.
    *
    * Top-level function that delegates to either single or robust rpca.
    *
    * @param rawResponseTimes collection of response times to analyze.
    * @param testId [[TestId]] used to log status.
    * @return None if responseTimes is empty or has size < `requiredMeasurements`, otherwise a wrapped RobustPCA object.
    */
  def robustPca(rawResponseTimes: Iterable[Double],
                testId: TestId = TestId.undefined): Option[RobustPca] = {

    if (rawResponseTimes.size < this.requiredMeasurements) {
      Logger.debug(s"insufficient historical data for ${testId.id} (found ${rawResponseTimes.size} but we require " +
        s"${this.requiredMeasurements}). rpca anomaly detection will not be performed.")
      None
    }
    else if (this.useDoubleRpca) {
      this.doubleRobustPca(rawResponseTimes)
    }
    else {
      this.singleRobustPca(rawResponseTimes)
    }
  }


  /**
    * Runs robust PCA on historical data.
    *
    * Performs Augmented Dickey-Fuller test for series stationarity and
    * standardizes the data (zero mean, unit variance) before proceeding with analysis.
    *
    * If the data exhibits a downward trend, we switch to using RPCA on a vector consisting of consecutive diffs between
    * time series points. This allows our smart thresholds to more tightly hug the observed data.
    *
    * If the time series points are stationary, or exhibiting an upward trend, we use RPCA on the original time series.
    * During an upward trend, we want to be more conservative and have our anomaly detection "lag behind" any trend in
    * the original data.
    *
    * @param rawResponseTimes collection of raw (unstandardized) response times to analyze, including today's reading.
    * @return
    */
  def singleRobustPca(rawResponseTimes: Iterable[Double], checkSlidingWindow: Boolean = true): Option[RobustPca] = {
    val truncatedData: Iterable[Double] = if (checkSlidingWindow) {
      this.slidingWindow(rawResponseTimes)
    }
    else {
      rawResponseTimes
    }

    val dickeyFuller: AugmentedDickeyFuller = new AugmentedDickeyFuller(truncatedData.toArray)

    val trend: Double = dickeyFuller.zeroPaddedDiff.sum

    // we want a tight threshold
    val testedData: Iterable[Double] = this.useDiff match {
      // check user overrides
      case Some(true) =>
        Logger.debug(s"override in place (useDiff=true). time series will be treated as non-stationary.")
        dickeyFuller.zeroPaddedDiff
      case Some(false) =>
        Logger.debug(s"override in place (useDiff=false). time series will be treated as stationary.")
        truncatedData
      // check actual results of the test
      case _ if !dickeyFuller.isStationary && trend < 0.0 =>
        dickeyFuller.zeroPaddedDiff
      case _ =>
        truncatedData
    }

    // standardize the data, taking sliding window if appropriate
    val responseTimes: Iterable[Double] = standardize(testedData)
    // wrap response times to create a 2d matrix
    val responseTimeMatrix: Array[Array[Double]] = Array(responseTimes.toArray)

    val stopwatch: WarpStopwatch = WarpStopwatch.start(s"single robust pca processed ${truncatedData.size} test executions in")
    // run the pca algorithm and store the result so we can access stored decomposed matrices
    val robustPCA: Option[RobustPca] = Option(new RobustPca(
      data = responseTimeMatrix,
      lPenalty = this.lPenalty,
      sPenalty = this.sPenaltyNumerator / math.sqrt(responseTimes.size),
      sThreshold = this.sThreshold
    ))

    stopwatch.stop()
    robustPCA
  }


  /**
    * Runs robust PCA on historical data (excluding todays measurement), filters out anomalous readings, and reruns the
    * algorithm including today's data using only normal historical measurements. This prevents the system from believing
    * that repeated runs after a regression are normal.
    *
    * @param rawResponseTimes collection of raw (unstandardized) response times to analyze, including today's reading.
    * @return
    */
  def doubleRobustPca(rawResponseTimes: Iterable[Double]): Option[RobustPca] = {
    // first collect all the past normal readings
    val rawNormalMeasurements: Iterable[Double] = this.getNormalMeasurements(rawResponseTimes)
    // check the size of the normal measurements, if we have enough then run rpca one final time, otherwise return none
    if (rawNormalMeasurements.size < this.requiredMeasurements) None
    else this.singleRobustPca(rawNormalMeasurements, checkSlidingWindow = true)
  }


  /**
    * Gets historical normal measurements as part of double robust PCA.
    *
    * @param rawResponseTimes
    * @return
    */
  def getNormalMeasurements(rawResponseTimes: Iterable[Double]): Iterable[Double] = {
    val stopwatch: WarpStopwatch = WarpStopwatch.start(s"double robust pca processed ${rawResponseTimes.size} test executions in")
    // its possible to read all of the processed historical data out of the database, something that would potentially save
    // us a few seconds of overhead at the cost of losing some correctness, as it might be the case that historical tests
    // were run with different rpca parameters. for now, we'll just recompute everything, and for tests with a very large
    // history, we'll only consider a subset of the full data as a time optimization
    val truncatedResponseTimes: List[Double] = rawResponseTimes.toList takeRight WARP_ANOMALY_DOUBLE_RPCA_TRUNCATION.value.toInt

    val trainingDataSize: Int = if (this.useSlidingWindow) this.slidingWindowSize else this.requiredMeasurements

    // we'll store all raw normal measurements here, do one final run of rpca with todays measurement
    val rawNormalMeasurements: mutable.ListBuffer[Double] = new mutable.ListBuffer ++ truncatedResponseTimes take trainingDataSize

    // each historical point that we'll classify. we want to exclude todays measurement and the initial training data
    val pointsToClassify: List[Double] = truncatedResponseTimes dropRight 1 drop trainingDataSize

    pointsToClassify foreach { responseTime: Double =>
      // during the historical processing phase we want to use only the normal measurements we've already detected.
      val maybeRpca: Option[RobustPca] = this.singleRobustPca(rawNormalMeasurements :+ responseTime, checkSlidingWindow = false)
      // if we have a normal measurement, add that to our list of normal measurements
      if (maybeRpca.isEmpty || (maybeRpca.isDefined && maybeRpca.get.isNormal)) {
        rawNormalMeasurements += responseTime
      }
    }

    stopwatch.stop()
    rawNormalMeasurements.toList
  }


  /**
    * Truncates `responseTimes` to retain only the most recent measurements for sliding window analysis.
    *
    * @param responseTimes
    * @return
    */
  def slidingWindow(responseTimes: Iterable[Double]): Iterable[Double] = {
    if (this.useSlidingWindow) responseTimes takeRight this.slidingWindowSize + 1
    else responseTimes
  }
}
