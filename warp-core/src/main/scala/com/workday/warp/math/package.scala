package com.workday.warp

import scala.math._

/**
  * Created by tomas.mccandless on 11/12/20.
  */
package object math {

  /**
    * Truncates `p` to be within [0.0, 100.0].
    * @param p a percentage.
    * @return a truncated version of `p`.
    */
  def truncatePercent(p: Double): Double = max(0.0, min(100.0, p))
}
