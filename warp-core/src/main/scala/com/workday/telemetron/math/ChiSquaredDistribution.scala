package com.workday.telemetron.math

import org.apache.commons.math3.distribution.AbstractRealDistribution

/**
  * A wrapped chi-squared distribution parameterized by the given degrees of freedom.
  *
  * Created by leslie.lam on 12/19/17.
  * Based on java class created by tomas.mccandless.
  */
class ChiSquaredDistribution(val parameters: Array[Double]) extends WrappedApacheDistribution {
  override protected val distribution: AbstractRealDistribution =
    this.getApacheDistribution(classOf[org.apache.commons.math3.distribution.ChiSquaredDistribution], parameters, 1)
}

object ChiSquaredDistribution {
  def apply(degreesOfFreedom: Double): ChiSquaredDistribution = ChiSquaredDistribution(Array(degreesOfFreedom))

  def apply(parameters: Array[Double]): ChiSquaredDistribution = new ChiSquaredDistribution(parameters)
}
