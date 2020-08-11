package com.workday.warp.persistence

/**
  * Holds individual components of a method signature.
  *
  * Created by tomas.mccandless on 1/30/17.
  */
case class MethodSignature(product: String, subproduct: String, className: String, method: String)


object MethodSignature {

  /**
    * Deconstructs `signature` into a [[MethodSignature]] of product, subproduct, class, and method.
    *
    * @param signature fully qualified method signature.
    * @return a [[MethodSignature]] containing product, subproduct, class, method.
    */
  @throws[RuntimeException]("when signature does not have at least 4 components")
  def apply(signature: String): MethodSignature = {
    // Break the fully qualified name of the test up into individual tokens that can be reported on.
    val tokens: Array[String] = signature split "\\."

    if (tokens.length < 4) {
      throw new RuntimeException(s"invalid warp test name: [${tokens mkString ", "}] (must have length >= 4)")
    }

    tokens takeRight 4 match { case Array(product, subproduct, clazz, method) => MethodSignature(product, subproduct, clazz, method) }
  }
}
