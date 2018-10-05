package com.workday.warp.math.linalg

import org.apache.commons.math3.linear.{MatrixUtils, RealMatrix, SingularValueDecomposition}

/**
  * Created by tomas.mccandless on 9/26/18.
  */
class RidgeRegression(val x: Array[Array[Double]], val y: Array[Double], val l2Penalty: Double) {


  val X: RealMatrix = MatrixUtils.createRealMatrix(this.x)
  val X_svd: SingularValueDecomposition = new SingularValueDecomposition(this.X)

  val (
    coefficients: Array[Double],
    fitted: Array[Double],
    residuals: Array[Double],
    standardErrors: Array[Double]
    ) = this.updateCoefficients


  def updateCoefficients: (Array[Double], Array[Double], Array[Double], Array[Double]) = {
    val V: RealMatrix = this.X_svd.getV
    val s: Array[Double] = this.X_svd.getSingularValues.map(a => a / (a * a + l2Penalty))
    val U: RealMatrix = this.X_svd.getU

    val S: RealMatrix = MatrixUtils.createRealDiagonalMatrix(s)
    val Z: RealMatrix = V.multiply(S).multiply(U.transpose)

    val coefficients: Array[Double] = Z.operate(this.y)
    val fitted: Array[Double] = this.X.operate(coefficients)

    val residuals: Array[Double] = this.y zip fitted map { case (a, b) => a - b }
    val errorVariance: Double = residuals.fold(0.0)((acc, a) => acc + a * a) / (this.X.getRowDimension - this.X.getColumnDimension)

    val errorVarianceMatrix: RealMatrix = MatrixUtils.createRealIdentityMatrix(this.y.length).scalarMultiply(errorVariance)
    val coefficientsCovarianceMatrix: RealMatrix = Z.multiply(errorVarianceMatrix).multiply(Z.transpose)
    val standardErrors: Array[Double] = this.getDiagonal(coefficientsCovarianceMatrix)

    // sets coefficients
    // sets fitted
    // sets residuals
    // sets standard error
    (coefficients, fitted, residuals, standardErrors)
  }


  /**
    * @param X
    * @return diagonal entries of X. Does not assume that X is square.
    */
  private[linalg] def getDiagonal(X: RealMatrix): Array[Double] = {
    (0 until X.getColumnDimension).map(i => X.getEntry(i, i)).toArray
  }
}
