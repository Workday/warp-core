package com.workday.telemetron.math

import org.apache.commons.math3.distribution.AbstractRealDistribution

/**
  * A wrapped exponential distribution parameterized by the given mean.
  *
  * Created by leslie.lam on 12/19/17.
  * Based on java class created by tomas.mccandless.
  */
class ExponentialDistribution(val parameters: Array[Double]) extends WrappedApacheDistribution {
  override protected val distribution: AbstractRealDistribution =
    this.getApacheDistribution(classOf[org.apache.commons.math3.distribution.ExponentialDistribution], parameters, 1)
}

object ExponentialDistribution {
  def apply(mean: Double): ExponentialDistribution = ExponentialDistribution(Array(mean))

  def apply(parameters: Array[Double]): ExponentialDistribution = new ExponentialDistribution(parameters)
}
