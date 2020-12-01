package com.workday.warp.persistence

import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}

import scala.util.Try

/**
  * Created by tomas.mccandless on 1/30/17.
  */
class MethodSignatureSpec extends WarpJUnitSpec {

  private val signature: String = "com.workday.warp.product.subproduct.Class.method"

  /** Checks that we can deconstruct a fully qualified method signature. */
  @UnitTest
  def methodSignature(): Unit = {
    val expected: MethodSignature = MethodSignature("product", "subproduct", "Class", "method")
    MethodSignature(this.signature) should be (expected)
    MethodSignature(this.signature split "\\." drop 3 mkString ".") should be (expected)

    // we require at least 4 components
    Try(MethodSignature("some.invalid.thing")) should die
  }
}
