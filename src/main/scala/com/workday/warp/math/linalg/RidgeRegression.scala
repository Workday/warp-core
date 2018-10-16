package com.workday.warp.math.linalg

import org.apache.commons.math3.linear.{MatrixUtils, RealMatrix, SingularValueDecomposition}

/**
  * Analyzes multiple regression data that suffer from multicollinearity.
  *
  * When multicollinearity occurs, least squares estimates are unbiased, but their variances are large so they may be far
  * from the true value. By adding a degree of bias to the regression estimates, ridge regression reduces the standard errors.
  * It is hoped that the net effect will be to give estimates that are more reliable.
  *
  * Adapted from https://github.com/Netflix/Surus/blob/master/src/main/java/org/surus/math/RidgeRegression.java
  *
  * Created by tomas.mccandless on 9/26/18.
  */
class RidgeRegression(val x: Array[Array[Double]], val y: Array[Double], val l2Penalty: Double = 0.0001) {


  val X: RealMatrix = MatrixUtils.createRealMatrix(this.x)
  val X_svd: SingularValueDecomposition = new SingularValueDecomposition(this.X)

  val (
    coefficients: Array[Double],
    fitted: Array[Double],
    residuals: Array[Double],
    standardErrors: Array[Double]
  ) = this.updateCoefficients


  /**
    * Computes ridge regression.
    *
    * The ridge estimator (`coefficients`, in the code below) is given by:
    *
    * w = V * S^-1 * D * U^T * y
    *
    * where:
    *   X=UDV^T is the SVD of the design matrix (x).
    *   S=D^2 + l I
    *
    * @return a tuple containing the result of ridge regression (coefficients of the estimator, along with the fitted data,
    *         residuals and standard errors)
    */
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
