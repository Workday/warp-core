package com.workday.telemetron.math

import com.workday.telemetron.annotation.{Distribution, Schedule}
import com.workday.telemetron.spec.TelemetronJUnitSpec
import com.workday.warp.common.category.UnitTest
import org.junit.experimental.categories.Category
import org.junit.Test
import org.scalatest.Matchers

/**
  * Created by leslie.lam on 12/12/17
  * Based on java class created tomas.mccandless
  */
class DistributionSpec extends TelemetronJUnitSpec with Matchers {

  @Test
  @Category(Array(classOf[UnitTest]))
  @Schedule(invocations = 10,
            // use a normal distribution with mean 50 and std dev 4
            distribution = new Distribution(clazz = classOf[GaussianDistribution], parameters = Array(50.0, 4.0)))
  def gaussian(): Unit = {}

  @Test
  @Category(Array(classOf[UnitTest]))
  @Schedule(invocations = 10,
            // use an exponential distribution with mean 50
            distribution = new Distribution(clazz = classOf[ExponentialDistribution], parameters = Array(50.0)))
  def exponential(): Unit = {}

  @Test
  @Category(Array(classOf[UnitTest]))
  @Schedule(invocations = 10,
            // use a weibull distribution with shape 1.0 and scale 1.0
            distribution = new Distribution(clazz = classOf[WeibullDistribution], parameters = Array(1.0, 1.0)))
  def weibull(): Unit = {}

  /**
    * Attempts to construct a GaussianDistribution with the wrong number of parameters.
    */
  @Test(expected = classOf[RuntimeException])
  @Category(Array(classOf[UnitTest]))
  def misMatchedGaussianParameters(): Unit = {
    val parameters: Array[Double] = Array(1.0, 2.0, 3.0, 4.0)
    DistributionLikeFactory.getDistribution(classOf[GaussianDistribution], parameters)
  }

  /**
    * Attempts to construct a ChiSquaredDistribution with the wrong number of parameters.
    */
  @Test(expected = classOf[RuntimeException])
  @Category(Array(classOf[UnitTest]))
  def misMatchedChiSquaredParameters(): Unit = {
    val parameters: Array[Double] = Array(1.0, 2.0, 3.0, 4.0)
    DistributionLikeFactory.getDistribution(classOf[ChiSquaredDistribution], parameters)
  }


  /**
    * Checks additional constructors for distribution types.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
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
