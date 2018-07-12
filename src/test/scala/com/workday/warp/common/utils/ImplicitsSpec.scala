package com.workday.warp.common.utils

import java.util.Optional

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.common.utils.Implicits._
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Created by tomas.mccandless on 6/8/18.
  */
class ImplicitsSpec extends WarpJUnitSpec {

  @Test
  @Category(Array(classOf[UnitTest]))
  def convertOptionals(): Unit = {

    val javaOptional: Optional[Int] = Option(2)
    javaOptional.get should be (2)

    val scalaOption: Option[Int] = javaOptional
    scalaOption.get should be (2)

    val emptyJavaOptional: Optional[Int] = None
    emptyJavaOptional should be (Optional.empty[Int])

    val none: Option[Int] = emptyJavaOptional
    none should be (None)
  }
}
