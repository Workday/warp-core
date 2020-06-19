package com.workday.warp.junit

import org.junit.jupiter.api.RepeatedTest.{CURRENT_REPETITION_PLACEHOLDER, DISPLAY_NAME_PLACEHOLDER, TOTAL_REPETITIONS_PLACEHOLDER}

/**
  * Created by tomas.mccandless on 6/18/20.
  */
trait WarpTestDisplayNameFormatterLike {
  val pattern: String
  val displayName: String

  def format(currentRepetition: Int, totalRepetitions: Int): String = {
    this.pattern
      .replace(DISPLAY_NAME_PLACEHOLDER, this.displayName)
      .replace(CURRENT_REPETITION_PLACEHOLDER, String.valueOf(currentRepetition))
      .replace(TOTAL_REPETITIONS_PLACEHOLDER, String.valueOf(totalRepetitions))
  }
}

case class WarpTestDisplayNameFormatter(pattern: String, displayName: String) extends WarpTestDisplayNameFormatterLike
