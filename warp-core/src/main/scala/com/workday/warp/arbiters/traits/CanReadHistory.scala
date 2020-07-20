package com.workday.warp.arbiters.traits

import java.time.LocalDate

import com.workday.warp.common.CoreWarpProperty._
import com.workday.warp.persistence.CorePersistenceAware
import com.workday.warp.persistence.CoreIdentifier
import com.workday.warp.persistence.CoreIdentifierType._


/**
  * Represents the ability to read historical test data.
  *
  * Mix this trait into your custom arbiters if you need, eg, historical training data.
  *
  * Created by tomas.mccandless on 3/9/16.
  */
trait CanReadHistory extends CorePersistenceAware {

  /**
    * Reads historical training data. Truncates to only include the most recent [[WARP_ARBITER_SLIDING_WINDOW_SIZE]]
    * measurements if [[WARP_ARBITER_SLIDING_WINDOW]] is enabled.
    *
    * @param testId            fully qualified method name of the test to read data for.
    * @param excludeIdTestExecution id of the [[com.workday.warp.persistence.Tables.TestExecution]] to exclude from the results.
    * @param startDateLowerBound optionally only include data that start after/on this date. If omitted, defaults
    *                            to day of epoch, effectively including all training data.
    * @param useSlidingWindow optionally enables the use of a sliding window from which to aggregate data. if omitted,
    *                         defaults to false and no window will be used.
    * @param slidingWindowSize include only data spanning the previous n executions.
    * @return training data to be used for voting algorithm.
    */
  def responseTimes(testId: String, excludeIdTestExecution: Int,
                    startDateLowerBound: LocalDate = CanReadHistory.DEFAULT_EPOCH_DAY,
                    useSlidingWindow: Boolean = WARP_ARBITER_SLIDING_WINDOW.value.toBoolean,
                    slidingWindowSize: Int = WARP_ARBITER_SLIDING_WINDOW_SIZE.value.toInt): Iterable[Double] = {
    val responseTimes: Iterable[Double] = this.allResponseTimes(testId, excludeIdTestExecution, startDateLowerBound)
    // check if we should use a sliding window, or return all historical data
    if (useSlidingWindow) responseTimes takeRight slidingWindowSize
    else responseTimes
  }

  /**
    * Reads historical training data. Returns all historical measurements except for `excludeIdTestExecution`.
    *
    * @param testId            fully qualified method name of the test to read data for.
    * @param excludeIdTestExecution id of the [[com.workday.warp.persistence.Tables.TestExecution]] to exclude from the results.
    * @return training data to be used for voting algorithm.
    */
  def allResponseTimes(testId: String,
                       excludeIdTestExecution: Int,
                       startDateLowerBound: LocalDate = CanReadHistory.DEFAULT_EPOCH_DAY): Iterable[Double] = {
    this.persistenceUtils.getResponseTimes(CoreIdentifier(methodSignature = testId), excludeIdTestExecution, startDateLowerBound)
  }
}

object CanReadHistory {
  val DEFAULT_EPOCH_DAY: LocalDate = LocalDate.ofEpochDay(0)
}
