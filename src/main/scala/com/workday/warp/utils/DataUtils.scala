package com.workday.warp.utils

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation

/**
  * Created by tomas.mccandless on 8/29/16.
  */
object DataUtils {

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
