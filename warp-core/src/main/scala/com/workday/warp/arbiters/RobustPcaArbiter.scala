package com.workday.warp.arbiters

import com.workday.warp.config.CoreWarpProperty._
import com.workday.warp.logger.WarpLogging
import com.workday.warp.math.linalg.{RobustPca, RobustPcaRunner}
import com.workday.warp.persistence.TablesLike.TestExecutionRowLikeType
import com.workday.warp.persistence.Tables._

/**
  * Arbiter that performs robust principal component analysis (RPCA) for anomaly detection
  *
  * See the following papers for more detail:
  * https://statweb.stanford.edu/~candes/papers/RobustPCA.pdf (Candes, 2009)
  * http://arxiv.org/pdf/1001.2363v1.pdf (Zhou, 2010)
  *
  * Should only consider successful test history.
  *
  * Created by tomas.mccandless on 2/18/16.
  */
class RobustPcaArbiter(val lPenalty: Double = WARP_ANOMALY_RPCA_L_PENALTY.value.toDouble,
                       val sPenaltyNumerator: Double = WARP_ANOMALY_RPCA_S_PENALTY_NUMERATOR.value.toDouble)
      extends CanReadHistory with ArbiterLike with WarpLogging {

  /**
    * Checks that the measured test passed its performance requirement.
    * If the requirement is failed, constructs an error with a useful message wrapped in an Option.
    *
    * @return a wrapped error with a useful message, or None if the measured test passed its requirement.
    */
  override def vote[T: TestExecutionRowLikeType](ballot: Ballot, testExecution: T): Option[Throwable] = {
    // explicitly exclude the current result when reading from the database, but then append it onto the end to make sure
    // it is actually the last entry (another test could have been written to the database, thus causing the order to be
    // incorrect.
    // we need to ensure the response time for this test execution is the final entry in this list.
    val rawResponseTimes: Iterable[Double] = this.successfulResponseTimes(
      ballot.testId.id,
      testExecution.idTestExecution
    ) ++ List(testExecution.responseTime)

    val runner: RobustPcaRunner = RobustPcaRunner(this.lPenalty, this.sPenaltyNumerator)

    runner.robustPca(rawResponseTimes, ballot.testId) flatMap { rpca: RobustPca =>
      val responseTime: Double = rawResponseTimes.last
      val isAnomaly: Boolean = rpca.isAnomaly
      val errorComponent: Double = rpca.error.getData.head.last
      val lowRankComponent: Double = rpca.lowRank.getData.head.last
      val sparseComponent: Double = rpca.sparse.getData.head.last
      val idTestExecution: Int = testExecution.idTestExecution

      // record the low rank and sparse components
      this.persistenceUtils.recordMeasurement(idTestExecution, "robustPCA.errorComponent", errorComponent)
      this.persistenceUtils.recordMeasurement(idTestExecution, "robustPCA.lowRankComponent", lowRankComponent)
      this.persistenceUtils.recordMeasurement(idTestExecution, "robustPCA.sparseComponent", sparseComponent)
      this.persistenceUtils.recordMeasurement(idTestExecution, "robustPCA.isAnomaly", if (isAnomaly) 1 else 0)

      val methodSignature: String = this.persistenceUtils.getMethodSignature(testExecution)
      // log a warning message if appropriate
      if (isAnomaly) {
        val errorMessage: String = s"test anomaly detected (rpca): name=$methodSignature, " +
          s"id=${testExecution.idTestExecution}. responseTime=$responseTime, lowRank=${lowRankComponent.formatted("%.2f")}, " +
          s"sparse=${sparseComponent.formatted("%.2f")}"
        logger.warn(errorMessage)
        Option(new RequirementViolationException(errorMessage))
      }
      else {
        None
      }
    }
  }
}
