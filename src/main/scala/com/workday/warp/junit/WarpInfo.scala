package com.workday.warp.junit

/** Used with JUnit parameter resolvers to make test iteration metadata available to running test methods.
  *
  * Created by tomas.mccandless on 6/18/20.
  */
trait HasWarpInfo {

  /** Current repetition of the corresponding [[WarpTest]] method. */
  def currentRepetition: Int

  /** Total number of repetitions of the corresponding [[WarpTest]] method. */
  def totalRepetitions: Int
}


case class WarpInfo(currentRepetition: Int, totalRepetitions: Int) extends HasWarpInfo
