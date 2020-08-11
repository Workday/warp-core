package com.workday.warp.math.linalg

import org.apache.commons.math3.linear.{MatrixUtils, RealMatrix, RealVector}

/**
  * Tests the null hypothesis that a unit root is present in a time series sample.
  *
  * The alternative hypothesis is stationarity or trend-stationarity.
  * The augmented Dickey–Fuller (ADF) statistic used in the test, is a negative number. The more negative it is, the stronger
  * the rejection of the hypothesis that there is a unit root at some level of confidence.
  *
  * Adapted from https://github.com/Netflix/Surus/blob/master/src/main/java/org/surus/math/AugmentedDickeyFuller.java
  *
  * Created by tomas.mccandless on 9/25/18.
  */
class AugmentedDickeyFuller(val timeSeries: Array[Double], val lag: Int) {

  val (isStationary: Boolean, zeroPaddedDiff: Array[Double]) = this.computeAdfStatistics

  def this(timeSeries: Array[Double]) = {
    this(timeSeries, lag = Math.floor(Math.cbrt(timeSeries.length - 1)).toInt)
  }

  /**
    * Computes Augmented Dickey-Fuller statistics.
    *
    * @return a tuple containing [[Boolean]] representing whether the time series needs to be further modified to remove
    *         global trend, and an [[Array]] containing diffs between consecutive elements of the time series. Note
    *         the diffs are zero-padded to match the length of the input time series.
    */
  private[linalg] def computeAdfStatistics: (Boolean, Array[Double]) = {
    val diffs: Array[Double] = this.diff(this.timeSeries)

    val k: Int = this.lag + 1
    val n: Int = this.timeSeries.length - 1

    val z: RealMatrix = MatrixUtils.createRealMatrix(laggedMatrix(diffs, k))
    // slice is exclusive wrt end boundary condition. we want inclusive slice up to n-1
    val xt1: Array[Double] = this.timeSeries.slice(k - 1, n)

    val designMatrix: RealMatrix = if (k > 1) {
      val design: RealMatrix = MatrixUtils.createRealMatrix(n - k + 1, 3 + k - 1)
      val yt1: RealMatrix = z.getSubMatrix(0, n - k, 1, k - 1)
      design.setSubMatrix(yt1.getData, 0, 3)
      design
    }
    else {
      MatrixUtils.createRealMatrix(n - k + 1, 3)
    }

    val trend: Array[Double] = (k to n).map(_.toDouble).toArray
    designMatrix.setColumn(0, xt1)
    designMatrix.setColumn(1, this.ones(n - k + 1))
    designMatrix.setColumn(2, trend)

    val zCol1: RealVector = z.getColumnVector(0)
    val regression: RidgeRegression = new RidgeRegression(designMatrix.getData, zCol1.toArray)
    val beta: Array[Double] = regression.coefficients
    val sd: Array[Double] = regression.standardErrors

    val adf: Double = beta.head / sd.head

    val isStationary: Boolean = adf > AugmentedDickeyFuller.pValueThreshold
    // prepend zero
    val zeroPaddedDiff: Array[Double] = 0.0 +: diffs
    (isStationary, zeroPaddedDiff)
  }


  /**
    * @return an [[Array]] containing consecutive differences of entries of `x`.
    */
  private[linalg] def diff(x: Array[Double]): Array[Double] = {
    // note that we need a zero-padded version of this as well
    x.sliding(2).map(slice => slice(1) - slice(0)).toArray
  }


  /**
    * Embeds time series `x` into a low-dimensional Euclidean space.
    *
    * Equivalent to R's embed.
    *
    * @param x
    * @param lag
    */
  private[linalg] def laggedMatrix(x: Array[Double], lag: Int): Array[Array[Double]] = {
    val laggedMatrix: Array[Array[Double]] = Array.ofDim[Double](x.length - lag + 1, lag)
    for {
      j <- 0 until lag
      i <- 0 until (x.length - lag + 1)
    } laggedMatrix(i)(j) = x(lag - j - 1 + i)

    laggedMatrix
  }


  /**
    * @param length
    * @return an [[Array]] of the specified length with all unit entries.
    */
  private[linalg] def ones(length: Int): Array[Double] = Array.fill(length)(1.0)

}

object AugmentedDickeyFuller {

  val pValueThreshold: Double = -3.45

}

