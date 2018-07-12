package com.workday.telemetron.math

import java.lang.reflect.InvocationTargetException

import org.apache.commons.math3.distribution.AbstractRealDistribution
import org.pmw.tinylog.Logger

/**
  * Trait providing a thin wrapper around an Apache distribution.
  *
  * Created by leslie.lam on 12/18/17.
  * Based on abstract java class created by tomas.mccandless.
  */
trait WrappedApacheDistribution extends DistributionLike {
  protected val distribution: AbstractRealDistribution

  protected def getApacheDistribution(distributionClass: Class[_ <: AbstractRealDistribution],
                                      parameters: Array[Double],
                                      expectedNumParameters: Int): AbstractRealDistribution = {
    val numParameters: Int = parameters.length
    if (numParameters != expectedNumParameters) {
      throw new IllegalArgumentException(s"expected $expectedNumParameters distribution parameters but received $numParameters")
    }

    // Array of parameter types used to look up the constructor
    val parameterTypes: Array[Class[_]] = Array.fill(numParameters)(classOf[Double])

    // Box doubles as java.lang.Double for varargs
    val boxedParameters: Array[java.lang.Double] = parameters.map(java.lang.Double.valueOf)

    try {
      Logger.debug(s"creating new WrappedApacheDistribution ${this.getClass.getCanonicalName} with parameters ${parameters mkString ","}\n")
      // Use reflection to instantiate proper distribution
      distributionClass.getDeclaredConstructor(parameterTypes: _*).newInstance(boxedParameters: _*)
    }
    catch {
      case exception@(_: InstantiationException |
                      _: IllegalAccessException |
                      _: InvocationTargetException |
                      _: NoSuchMethodException) =>
        throw new RuntimeException(exception)
    }
  }

  /**
    * @return a long sampled from the underlying distribution.
    */
  override def sample: Long = this.distribution.sample.toLong
}
