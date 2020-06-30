package com.workday.warp.junit

/** Used with JUnit parameter resolvers to make test iteration metadata available to running test methods.
  *
  * Created by tomas.mccandless on 6/18/20.
  */
trait WarpInfoLike {

  def testId: String

  /** Current repetition of the corresponding [[WarpTest]] method. */
  def currentRepetition: Int

  def repetitionType: RepetitionType

  def numWarmups: Int

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
