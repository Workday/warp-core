package com.workday.warp

import java.time.Duration
import java.util.concurrent.TimeUnit

import com.workday.telemetron.utils.TimeUtils
import com.workday.warp.common.utils.Implicits.DecoratedLong
import com.workday.warp.persistence.TablesLike.TestExecutionRowLike

/**
  * Holds results of a warp test.
  *
  * Created by timothy.soppet on 6/17/16.
  * Based on WarpTestResult created by tomas.mccandless on 3/28/16.
  */
case class TrialResult[+TrialType](maybeResponseTime: Option[Duration] = None,
                                   maybeThreshold: Option[Duration] = None,
                                   maybeTestExecution: Option[TestExecutionRowLike] = None,
                                   maybeDocumentation: Option[String] = None,
                                   maybeResult: Option[TrialType] = None) {

  /**
    * Auxiliary constructor.
    *
    * @param responseTime [[Duration]] representing the measured response time.
    * @return a [[TrialResult]] with the specified response time and an empty [[TestExecutionRowLike]]
    */
  def this(responseTime: Duration) = this(Option(responseTime), None, None)

  /**
    * Auxiliary constructor.
    *
    * @param responseTime [[Duration]] representing the measured response time.
    * @param threshold [[Duration]] representing the maximum acceptable measured response time.
    * @return a [[TrialResult]] with the specified response time and an empty [[TestExecutionRowLike]]
    */
  def this(responseTime: Duration, threshold: Duration) = this(Option(responseTime), Option(threshold), None)
}


object TrialResult {

  /** @return a [[TrialResult]] with the specified responseTime Duration. */
  def apply(responseTime: Duration): TrialResult[_] = new TrialResult(responseTime)

  /** @return a [[TrialResult]] with the specified responseTime and threshold Duration values. */
  def apply(responseTime: Duration, threshold: Duration): TrialResult[_] = new TrialResult(responseTime, threshold)

  /** @return a [[TrialResult]] with default constructor values indicating a measurement was not recorded. */
  def empty[TrialType]: TrialResult[TrialType] = new TrialResult
}
