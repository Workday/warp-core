package com.workday.warp.junit

/** Used with JUnit parameter resolvers to make test iteration metadata available to running test methods.
  *
  * Created by tomas.mccandless on 6/18/20.
  */
trait WarpInfoLike {

  /** Test ID we use to record results. Typically fully qualified method name. */
  def testId: String

  /** Current repetition of the corresponding [[WarpTest]] method. */
  def currentRepetition: Int

  /** Type of the current test invocation. Warmup, or trial. */
  def repetitionType: RepetitionType

  /** Number of unmeasured warmup invocations for this test. */
  def numWarmups: Int

  /** Number of measured trial invocations for this test. */
  def numTrials: Int

  /** Total number of repetitions of the corresponding [[WarpTest]] method. (warmups + trials) */
  def totalRepetitions: Int = numWarmups + numTrials
}

trait HasWarpInfo {
  def warpInfo: WarpInfoLike
}


case class WarpInfo(testId: String,
                    currentRepetition: Int,
                    repetitionType: RepetitionType,
                    numWarmups: Int,
                    numTrials: Int) extends WarpInfoLike
