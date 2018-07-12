package com.workday.telemetron.math

import org.apache.commons.math3.distribution.{AbstractRealDistribution, NormalDistribution}

/**
  * A wrapped normal distribution parameterized by the given mean and standard deviation.
  *
  * Created by leslie.lam on 12/19/17.
  * Based on java class created tomas.mccandless.
  */
class GaussianDistribution(val parameters: Array[Double]) extends WrappedApacheDistribution {
  override protected val distribution: AbstractRealDistribution =
    this.getApacheDistribution(classOf[NormalDistribution], parameters, 2)
}

object GaussianDistribution {
  def apply(mean: Double, stdDev: Double): GaussianDistribution = GaussianDistribution(Array(mean, stdDev))

  def apply(parameters: Array[Double]): GaussianDistribution = new GaussianDistribution(parameters)
}
