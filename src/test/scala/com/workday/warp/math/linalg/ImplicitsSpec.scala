package com.workday.warp.math.linalg

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.math.linalg.Implicits._
import org.apache.commons.math3.linear.{MatrixUtils, RealMatrix}
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Created by tomas.mccandless on 6/11/18.
  */
class ImplicitsSpec extends WarpJUnitSpec {

  /**
    * Checks our implicits for matrix ops.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def realMatrix(): Unit = {
    val r1: RealMatrix = MatrixUtils.createRealMatrix(Array(Array(1, 2), Array(3, 4)))
    val r2: RealMatrix = MatrixUtils.createRealMatrix(Array(Array(5, 6), Array(7, 8)))

    val sum: RealMatrix = r1 + r2
    sum.getData should be (Array(Array(6, 8), Array(10, 12)))

    val diff: RealMatrix = r1 - r2
    diff.getData should be (Array(Array(-4, -4), Array(-4, -4)))

    val product: RealMatrix = r1 * r2
    product.getData should be (Array(Array(19, 22), Array(43, 50)))
  }
}
