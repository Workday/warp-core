package com.workday.warp.junit

/** Display name formats to contain
 *
 * Created by tomas.mccandless on 6/18/20.
 */
trait WarpTestDisplayNameFormatterLike {
  import WarpTestDisplayNameFormatterLike._

  // contains placeholders for display name, iterations
  val pattern: String
  // plain display name eg testAdd(java.lang.String)
  val displayName: String
  // warmup or trial
  val repetitionType: String

  /**
   *
   * @param currentRepetition
   * @param totalRepetitions
   * @return
   */
  def format(currentRepetition: Int, totalRepetitions: Int): String = {
    this.pattern
      .replace(displayNameToken, this.displayName)
      .replace(currentRepToken, String.valueOf(currentRepetition))
      .replace(totalRepsToken, String.valueOf(totalRepetitions))
      .replace(repTypeToken, this.repetitionType)
  }
}

object WarpTestDisplayNameFormatterLike {

  /** Placeholder for the plain [[org.junit.jupiter.api.TestInfo]] display name of a [[WarpTest]] method. */
  val displayNameToken = "{displayName}"

  /** Placeholder for the current repetition count of a [[WarpTest]] method. */
  val currentRepToken = "{currentRepetition}"

  /** Placeholder for the total number of repetitions of a [[WarpTest]] method. */
  val totalRepsToken = "{totalRepetitions}"

  /** Placeholder for the type of run of a [[WarpTest]] method, eg "warmup" or "trial". */
  val repTypeToken = "{repetitionType}"

  /** Display name pattern for a [[WarpTest]]. */
  val displayNamePattern: String = s"$displayNameToken [$repTypeToken $currentRepToken of $totalRepsToken]"
}

case class WarpTestDisplayNameFormatter(pattern: String,
                                        displayName: String,
                                        repetitionType: String) extends WarpTestDisplayNameFormatterLike
