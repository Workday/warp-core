package com.workday.telemetron.math

import java.lang.reflect.InvocationTargetException

/**
  * Used to construct instances of [[DistributionLike]].
  *
  * We are somewhat limited by the Java annotation mechanism, and resort to providing distribution parameters
  * as an array of doubles.
  *
  * Created by leslie.lam on 12/18/17.
  * Based on java class created by tomas.mccandless.
  */
object DistributionLikeFactory {

  /**
    * Creates an instance of the specified class with the provided parameters.
    *
    * @param distributionClass class extending Sampleable
    * @param parameters
    * @return
    */
  def getDistribution[T <: DistributionLike](distributionClass: Class[T], parameters: Array[Double]): T = {
    try {
      distributionClass.getDeclaredConstructor(parameters.getClass).newInstance(parameters)
    }
    catch {
      case exception@(_: InstantiationException |
                      _: IllegalAccessException |
                      _: InvocationTargetException |
                      _: NoSuchMethodException) =>
        throw new RuntimeException(s"unable to create ${distributionClass.getCanonicalName} with parameters ${parameters mkString ", "}",
                                   exception)
    }
  }
}
