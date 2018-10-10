package com.workday.warp.dsl

import java.time.Duration

import com.workday.warp.TrialResult
import com.workday.warp.collectors.ResponseTimeCollector
import com.workday.warp.common.utils.Implicits.DecoratedDuration
import com.workday.warp.math.linalg.RobustPcaRunner
import org.scalatest.matchers.{BeMatcher, MatchResult, Matcher}
import org.scalatest.words.ResultOfNotWordForAny

/** Matchers to be used with scalatest.
  *
  * Comes with a companion object so these can be either imported or mixed into a class.
  *
  * Created by tomas.mccandless on 3/28/16.
  */
trait WarpMatchers {

  /** Creates a [[MatchResult]] that determines whether `responseTime` is less than `threshold`.
    * Result is negated if `negate` is true. (If this function is called from a [[ResultOfNotWordForAny]]).
    *
    * @param threshold response time threshold.
    * @param responseTimes measured response times.
    * @param negate whether to negate the result.
    * @return a [[MatchResult]] for whether `responseTime` is less than `threshold`.
    */
  private[this] def matchResult(threshold: Duration,
                                responseTimes: List[Option[Duration]],
                                negate: Boolean = false): MatchResult = {
    // check if there exists a responseTime greater than the threshold; xor for negating the result
    // responseTimes.flatten removes any None values in List[Option[Duration]] and returns a List[Duration]
    val failed: Boolean = (responseTimes.flatten exists { _ > threshold }) ^ negate
    val message: String = s"response time $responseTimes exceeded threshold $threshold"
    MatchResult(!failed, message, message)
  }


  /** [[Matcher]] for response time requirements set on [[TrialResult]] */
  class ResponseTimeRequirementMatcher(threshold: Duration) extends Matcher[List[TrialResult[_]]] {

    /** Updates thresholds in mysql and influxdb, and creates a [[MatchResult]] */
    override def apply(left: List[TrialResult[_]]): MatchResult = {
      ResponseTimeCollector.updateThresholds(left, this.threshold)
      matchResult(this.threshold, left map { _.maybeResponseTime })
    }
  }


  /** Part of the dsl. Allows syntax similar to `should notExceedThreshold (64 seconds)`
    *
    * @param threshold maximum response time threshold.
    * @return a [[ResponseTimeRequirementMatcher]] for `threshold`.
    */
  @DslApi
  def notExceedThreshold(threshold: Duration): ResponseTimeRequirementMatcher = {
    new ResponseTimeRequirementMatcher(threshold)
  }


  /** Part of the dsl. Allows syntax similar to `should notExceed (64 seconds)`
    *
    * @param threshold maximum response time threshold.
    * @return a [[ResponseTimeRequirementMatcher]] for `threshold`.
    */
  @DslApi
  def notExceed(threshold: Duration): ResponseTimeRequirementMatcher = {
    new ResponseTimeRequirementMatcher(threshold)
  }


  /** Creates a [[MatchResult]] that determines whether or not the latest [[TrialResult]] is an anomaly
    *
    * @param responseTimes a list containing [[TrialResult]]
    * @return a [[MatchResult]] that holds true if there's an anomaly, otherwise false
    */
  private[this] def rpcaMatchResult(responseTimes: List[Double]): MatchResult = {
    val runner: RobustPcaRunner = RobustPcaRunner(slidingWindowSize = 0, useSlidingWindow = false, useDoubleRpca = false)
    val result: Seq[(Double, Int)] = runner.singleRobustPca(responseTimes).get.containsAnomalies
    val matchResult: Boolean = result.nonEmpty
    val message: String = "No anomaly found."
    val negatedMessage: String = formatNegatedMessage(result.map(_._2))

    MatchResult(matchResult, message, negatedMessage)
  }


  /** Helper function to format the negated message to include all indices where an anomaly occurred
    *
    * @param result a [[Seq]] containing anomalous indices
    * @return the formatted negated message
    */
  private def formatNegatedMessage(result: Seq[Int]): String = {
    val negatedMessage: StringBuilder = new StringBuilder
    val plural: Boolean = result.size > 1
    negatedMessage.append(s"Anomal${if (plural) "ies" else "y"} found at ind${if (plural) "ices" else "ex"}: " +
      s"${result.mkString(", ")}")

    negatedMessage.toString()
  }


  /** [[BeMatcher]] for anomaly detection */
  class AnomalyMatcher extends BeMatcher[List[TrialResult[_]]] {
    def apply(left: List[TrialResult[_]]): MatchResult = {
      // get each TrialResult's response time, flatten, then convert Duration to Double
      val responseTimes: List[Double] = left.flatMap(_.maybeResponseTime).map(_.doubleSeconds)
      rpcaMatchResult(responseTimes)
    }
  }


  /** Part of the dsl. Allows syntax like
    *
    * 'shouldBe anomalous'
    * 'should not be anomalous'
    *
    * @return an [[AnomalyMatcher]]
    */
  @DslApi
  def anomalous(): AnomalyMatcher = {
    new AnomalyMatcher
  }


  /** Implicit class to support syntax like `should not exceed (64 seconds)`.
    *
    * @param negated a [[ResultOfNotWordForAny]] representing a negated match condition.
    */
  implicit class NegatedMatcher[T <: TrialResult[Any]](negated: ResultOfNotWordForAny[List[T]]) {

    /** Part of the dsl. Allows syntax like `should not exceed (64 seconds)`. Uses scalatest dsl to create a
      * [[Matcher]] and a [[MatchResult]] for the measured response time.
      *
      * @param threshold maximum response time threshold.
      */
    @DslApi
    def exceed(threshold: Duration): Unit = {
      this.negated be BeMatcher[List[TrialResult[_]]] { left: List[TrialResult[_]] =>
        ResponseTimeCollector.updateThresholds(left, threshold)

        val measuredResponseTimes: List[Option[Duration]] = left map { _.maybeResponseTime }
        matchResult(threshold, measuredResponseTimes, negate = true)
      }
    }
  }
}

// can be imported or mixed in
object WarpMatchers extends WarpMatchers
