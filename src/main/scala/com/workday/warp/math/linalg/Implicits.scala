package com.workday.warp.math.linalg

import org.apache.commons.math3.linear.{MatrixUtils, RealMatrix}
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

/**
  * Utility functions for mathematics.
  *
  * Created by tomas.mccandless on 9/14/16.
  */
object Implicits {

  /**
    * Decorates [[RealMatrix]] with some additional functions.
    *
    * @param matrix
    */
  implicit class DecoratedRealMatrix(matrix: RealMatrix) {

    private[this] def data: Array[Array[Double]] = this.matrix.getData

    /** @return the l-1 norm of the wrapped matrix. */
    def l1Norm: Double = this.data.flatten.foldLeft(0.0)((a: Double, b: Double) => a + math.abs(b))

    /** @return the standard deviation of the wrapped matrix. */
    def standardDeviation: Double = new DescriptiveStatistics(this.data.flatten).getStandardDeviation

    /**
      * @param penalty penalty to subtract from each element.
      * @return soft thresholded matrix.
      */
    def softThreshold(penalty: Double): RealMatrix = {
      MatrixUtils.createRealMatrix(this.data map { row: Array[Double] => row.softThreshold(penalty) })
    }

    /**
      * @param other [[RealMatrix]] to add.
      * @return the sum of this and other.
      */
    def +(other: RealMatrix): RealMatrix = this.matrix.add(other)

    /**
      * @param other [[RealMatrix]] to subtract
      * @return the difference between this and other.
      */
    def -(other: RealMatrix): RealMatrix = this.matrix.subtract(other)

    /**
      * @param other [[RealMatrix]] to multiply.
      * @return this multiplied by other.
      */
    def *(other: RealMatrix): RealMatrix = this.matrix.multiply(other)
  }


  /**
    * Decorates [[Array]] with some convenient linear algebra functions.
    *
    * @param array
    */
  implicit class DecoratedArray(array: Array[Double]) {

    /**
      * @param penalty penalty to subtract from each element.
      * @return soft thresholded array.
      */
    def softThreshold(penalty: Double): Array[Double] = {
      this.array map { d: Double => math.signum(d) * math.max(math.abs(d) - penalty, 0)}
    }
  }
}
