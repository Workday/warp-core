package com.workday.warp.junit

/** Used with JUnit parameter resolvers to make test iteration metadata available to running test methods.
  *
  * @param testId test identifier we use to record results. Typically fully qualified method name.
  * @param currentRepetition current repetition of the corresponding test method.
  * @param repetitionType type of the current test invocation.
  * @param numWarmups number of unmeasured warmup invocations for this test.
  * @param numTrials number of measured trial invocations for this test.
  *
  * Created by tomas.mccandless on 6/18/20.
  */
case class WarpInfo(testId: String, currentRepetition: Int, repetitionType: RepetitionType, numWarmups: Int, numTrials: Int) {

  /** Total number of repetitions of the corresponding [[WarpTest]] method. (warmups + trials) */
  def totalRepetitions: Int = numWarmups + numTrials

  /** Repetition limit for the current repetition type. Number of warmups or measured trials. */
  def currentRepLimit: Int = repetitionType match {
    case Warmup => numWarmups
    case Trial => numTrials
  }
}


trait HasWarpInfo {
  def warpInfo: WarpInfo
}
