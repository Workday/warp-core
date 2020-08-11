package com.workday.telemetron.math

import org.apache.commons.math3.distribution.AbstractRealDistribution

/**
  * A wrapped Weibull distribution parameterized by shape and scale, both of which must be strictly positive.
  *
  * Created by leslie.lam on 12/19/17.
  * Based on java class created by tomas.mccandless on 4/28/16.
  */
class WeibullDistribution(val parameters: Array[Double]) extends WrappedApacheDistribution {
  override protected val distribution: AbstractRealDistribution =
    this.getApacheDistribution(classOf[org.apache.commons.math3.distribution.WeibullDistribution], parameters, 2)
}

object WeibullDistribution {
  def apply(shape: Double, scale: Double): WeibullDistribution = WeibullDistribution(Array(shape, scale))

  def apply(parameters: Array[Double]): WeibullDistribution = new WeibullDistribution(parameters)
}
