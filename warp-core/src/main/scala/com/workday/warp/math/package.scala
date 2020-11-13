package com.workday.warp

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation

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


  /**
    * Standardizes `data` to have zero mean and unit variance.
    *
    * @param data
    * @return
    */
  def standardize(data: Iterable[Double]): Iterable[Double] = {
    val mean: Double = data.sum / data.size
    val stdDev: Double = (new StandardDeviation).evaluate(data.toArray, mean)
    data map { d: Double => if (stdDev > 0) (d - mean) / stdDev else d - mean }
  }
}
