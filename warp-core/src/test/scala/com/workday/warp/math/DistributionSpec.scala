package com.workday.warp.math

import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}

/**
  * Created by leslie.lam on 12/12/17
  * Based on java class created tomas.mccandless
  */
class DistributionSpec extends WarpJUnitSpec {

  /**
    * Attempts to construct a GaussianDistribution with the wrong number of parameters.
    */
  @UnitTest
  def misMatchedGaussianParameters(): Unit = {
    val parameters: Array[Double] = Array(1.0, 2.0, 3.0, 4.0)
    intercept[RuntimeException] {
      DistributionLikeFactory.getDistribution(classOf[GaussianDistribution], parameters)
    }
  }

  /**
    * Attempts to construct a ChiSquaredDistribution with the wrong number of parameters.
    */
  @UnitTest
  def misMatchedChiSquaredParameters(): Unit = {
    val parameters: Array[Double] = Array(1.0, 2.0, 3.0, 4.0)
    intercept[RuntimeException] {
      DistributionLikeFactory.getDistribution(classOf[ChiSquaredDistribution], parameters)
    }
  }


  /**
    * Checks additional constructors for distribution types.
    */
  @UnitTest
  def constructors(): Unit = {
    val chi: ChiSquaredDistribution = ChiSquaredDistribution(degreesOfFreedom = 4)
    chi.parameters.length should be (1)
    chi.parameters(0) should be (4)

    val exp: ExponentialDistribution = ExponentialDistribution(mean = 5)
    exp.parameters.length should be (1)
    exp.parameters(0) should be (5)

    val gauss: GaussianDistribution = GaussianDistribution(8, 16)
    gauss.parameters.length should be (2)
    gauss.parameters(0) should be (8)
    gauss.parameters(1) should be (16)

    val weibull: WeibullDistribution = WeibullDistribution(10, 20)
    weibull.parameters.length should be (2)
    weibull.parameters(0) should be (10)
    weibull.parameters(1) should be (20)
  }
}
