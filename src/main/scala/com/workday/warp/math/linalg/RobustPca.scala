package com.workday.warp.math.linalg

import com.workday.warp.common.CoreWarpProperty._
import org.apache.commons.math3.linear.{MatrixUtils, RealMatrix, SingularValueDecomposition}
import com.workday.warp.math.linalg.Implicits._

/**
  * Implementation of robust principal component analysis.
  * Based on https://github.com/Netflix/Surus/blob/master/src/main/java/org/surus/math/RPCA.java
  *
  * Created by tomas.mccandless on 9/14/16.
  */
class RobustPca(val data: RealMatrix,
                val lPenalty: Double,
                val sPenalty: Double,
                val sThreshold: Double = WARP_ANOMALY_RPCA_S_THRESHOLD.value.toDouble) {

  val rows: Int = data.getRowDimension
  val cols: Int = data.getColumnDimension

  // normal, anomalous, and noise components (L, S, E matrices in the paper)
  val (lowRank: RealMatrix, sparse: RealMatrix, error: RealMatrix) = this.rsvd

  // auxiliary constructor accepting 2d array
  def this(data: Array[Array[Double]], lPenalty: Double, sPenalty: Double, sThreshold: Double) = {
    this(MatrixUtils.createRealMatrix(data), lPenalty, sPenalty, sThreshold)
  }

  def this(data: Array[Array[Double]], lPenalty: Double, sPenalty: Double) = {
    this(MatrixUtils.createRealMatrix(data), lPenalty, sPenalty)
  }


  /** Primary function, computes decomposition of M into L + S + E. */
  private[this] def rsvd: (RealMatrix, RealMatrix, RealMatrix) = {
    var mu: Double = this.cols * this.rows / (4 * this.data.l1Norm)
    var objectivePrev: Double = 0.5 * math.pow(this.data.getFrobeniusNorm, 2)
    var objective: Double = objectivePrev
    // accepted tolerance between subsequent iterations
    val tolerance: Double = 1e-8 * objectivePrev
    // difference between subsequent objective function computations
    var difference: Double = 2 * tolerance
    var converged: Boolean = false
    var iterations: Int = 0

    var lowRankEstimate: RealMatrix = MatrixUtils.createRealMatrix(this.rows, this.cols)
    var sparseEstimate: RealMatrix = MatrixUtils.createRealMatrix(this.rows, this.cols)
    var errorEstimate: RealMatrix = MatrixUtils.createRealMatrix(this.rows, this.cols)

    while (!converged && iterations < RobustPca.maxIterations) {
      // update our estimates of the decompositions
      val (newSparseEstimate: RealMatrix, nuclearNorm: Double) = this.estimateSparse(lowRankEstimate, mu)
      sparseEstimate = newSparseEstimate
      val (newLowRankEstimate: RealMatrix, l1Norm: Double) = this.estimateLowRank(sparseEstimate, mu)
      lowRankEstimate = newLowRankEstimate
      val (newErrorEstimate: RealMatrix, l2Norm: Double) = this.estimateError(lowRankEstimate, sparseEstimate)
      errorEstimate = newErrorEstimate

      // recompute the objective function and check if we converged
      objective = this.objective(nuclearNorm, l1Norm, l2Norm)
      difference = math.abs(objectivePrev - objective)
      objectivePrev = objective
      mu = this.dynamicMu(errorEstimate)
      converged = difference <= tolerance
      iterations += 1
    }

    (lowRankEstimate, sparseEstimate, errorEstimate)
  }


  /**
    * Computes an updated estimate of the low-rank component.
    *
    * @param sparseEstimate current estimate of sparse component.
    * @param mu dynamically computed scalar based on standard deviation of our current estimate of error component.
    * @return a tuple containing an updated low-rank [[RealMatrix]] and a penalized l1-norm [[Double]].
    */
  private[this] def estimateLowRank(sparseEstimate: RealMatrix, mu: Double): (RealMatrix, Double) = {
    val penalty: Double = this.lPenalty * mu
    val svd: SingularValueDecomposition = new SingularValueDecomposition(this.data - sparseEstimate)
    // thresholded singular values
    val penalizedD: Array[Double] = svd.getSingularValues.softThreshold(penalty)
    val lowRank: RealMatrix = svd.getU * MatrixUtils.createRealDiagonalMatrix(penalizedD) * svd.getVT
    // we can use sum instead of l1 norm here because the entries are already non-negative
    // time optimization makes the computation linear instead of quadratic.
    val l1Norm: Double = penalizedD.sum * penalty
    (lowRank, l1Norm)
  }


  /**
    * Computes an updated estimate of the sparse component.
    *
    * @param lowRankEstimate current estimate of low-rank component.
    * @param mu dynamically computed scalar based on standard deviation of our current estimate of error component.
    * @return a tuple containing an updated sparse [[RealMatrix]] and a penalized l1-norm [[Double]].
    */
  private[this] def estimateSparse(lowRankEstimate: RealMatrix, mu: Double): (RealMatrix, Double) = {
    val penalty: Double = this.sPenalty * mu
    val penalizedS: RealMatrix = (this.data - lowRankEstimate).softThreshold(penalty)
    (penalizedS, penalizedS.l1Norm * penalty)
  }


  /**
    * Computes an updated estimate of the error component.
    *
    * @param lowRankEstimate current estimate of low-rank component.
    * @param sparseEstimate current estimate of sparse component.
    * @return a tuple containing an updated sparse [[RealMatrix]] and a penalized l1-norm [[Double]].
    */
  private[this] def estimateError(lowRankEstimate: RealMatrix, sparseEstimate: RealMatrix): (RealMatrix, Double) = {
    val error: RealMatrix = this.data - lowRankEstimate - sparseEstimate
    val l2Norm: Double = math.pow(error.getFrobeniusNorm, 2)
    (error, l2Norm)
  }


  /**
    * The objective function we are trying to optimize.
    *
    * @param nuclearNorm
    * @param l1Norm
    * @param l2Norm
    * @return
    */
  private[this] def objective(nuclearNorm: Double, l1Norm: Double, l2Norm: Double): Double = {
    0.5 * l2Norm + nuclearNorm + l1Norm
  }


  /** @return dynamically computed mu scalar based on the standard deviation of our current estimated error component. */
  private[this] def dynamicMu(errorEstimate: RealMatrix): Double = {
    val errorStdDev: Double = errorEstimate.standardDeviation
    val mu: Double = errorStdDev * math.sqrt(2 * math.max(this.rows, this.cols))
    math.max(.01, mu)
  }


  /**
    * Uses robust principal component analysis to determine whether the most recent recorded response time is an anomaly.
    * Checks whether the S component of the decomposed data exceeds a user-defined threshold.
    *
    * @return true iff the most recent recorded measurement is an anomaly.
    */
  def isAnomaly: Boolean = {
    val sparseComponent: Double = this.sparse.getData.head.last
    sparseComponent > this.sThreshold
  }

  /** @return true iff the most recent recorded measurement is not an anomaly. */
  def isNormal: Boolean = !this.isAnomaly

  /**
    * Determines whether any anomalies exist within the recorded response times.
    *
    * @return a [[Seq]] of tuples containing the anomalous value (if it exists) and its index
    */
  def containsAnomalies: Seq[(Double, Int)] = {
    this.sparse.getData.head.zipWithIndex.filter(_._1 > this.sThreshold)
  }
}


object RobustPca {
  val maxIterations: Int = 228
}
