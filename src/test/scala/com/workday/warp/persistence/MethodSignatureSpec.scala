package com.workday.warp.persistence

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.Test
import org.junit.experimental.categories.Category

import scala.util.Try

/**
  * Created by tomas.mccandless on 1/30/17.
  */
class MethodSignatureSpec extends WarpJUnitSpec {

  private val signature: String = "com.workday.warp.product.subproduct.Class.method"

  /** Checks that we can deconstruct a fully qualified method signature. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def methodSignature(): Unit = {
    val expected: MethodSignature = MethodSignature("product", "subproduct", "Class", "method")
    MethodSignature(this.signature) should be (expected)
    MethodSignature(this.signature split "\\." drop 3 mkString ".") should be (expected)

    // we require at least 4 components
    Try(MethodSignature("some.invalid.thing")) should die
  }
}
