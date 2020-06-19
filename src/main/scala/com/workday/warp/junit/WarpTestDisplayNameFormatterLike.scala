package com.workday.warp.junit

import com.workday.warp.junit.WarpTest._

/**
  * Created by tomas.mccandless on 6/18/20.
  */
trait WarpTestDisplayNameFormatterLike {
  val pattern: String
  val displayName: String
  val op: String

  def format(currentRepetition: Int, totalRepetitions: Int): String = {
    this.pattern
      .replace(DISPLAY_NAME_PLACEHOLDER, this.displayName)
      .replace(CURRENT_REPETITION_PLACEHOLDER, String.valueOf(currentRepetition))
      .replace(TOTAL_REPETITIONS_PLACEHOLDER, String.valueOf(totalRepetitions))
      .replace(OP_PLACEHOLDER, op)
  }
}

case class WarpTestDisplayNameFormatter(pattern: String, displayName: String, op: String) extends WarpTestDisplayNameFormatterLike
