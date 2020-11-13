package com.workday.warp.arbiters

import java.time.Instant
import java.util.UUID

import com.workday.warp.TestIdImplicits._
import com.workday.warp.ZScoreRequirement
import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import com.workday.warp.persistence.CorePersistenceAware
import com.workday.warp.persistence.TablesLike.TestExecutionRowLike
import com.workday.warp.persistence.TablesLike.RowTypeClasses._
import com.workday.warp.utils.AnnotationReader
import org.junit.jupiter.api.TestInfo

/**
  * Created by tomas.mccandless on 1/26/16.
  */
class ZScoreArbiterSpec extends WarpJUnitSpec with CorePersistenceAware {

  // minimum number of measurements necessary for percentile processing to continue.
  private[this] val minimumHistoricalData: Int = 3


  /** Checks that the provided percentile threshold is not truncated during reading. */
  @UnitTest
  @ZScoreRequirement(percentile = 100.2345)
  def percentileThreshold(info: TestInfo): Unit = {
    AnnotationReader.getZScoreRequirement(info) shouldBe Some(100.2345)
  }


  /** Checks that the provided percentile threshold is not truncated during reading. */
  @UnitTest
  @ZScoreRequirement(percentile = -1.2345)
  def percentileThresholdNegative(info: TestInfo): Unit = {
    AnnotationReader.getZScoreRequirement(info) shouldBe Some(-1.2345)
  }


  /** Checks behavior when there are not enough datapoints. */
  @UnitTest
  def notEnoughData(): Unit = {
    val testId: String = s"com.workday.warp.ZScore.${UUID.randomUUID().toString}"
    val ballot: Ballot = new Ballot(testId)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(testId, Instant.now(), 4.0, 3.0)
    val arbiter: ZScoreArbiter = new ZScoreArbiter
    arbiter.vote(ballot, testExecution) should be (empty)
  }


  /** Checks that the Option[Throwable] is defined or empty based on the provided response times. */
  @UnitTest
  @ZScoreRequirement(percentile = 95.0)
  def percentileVote(info: TestInfo): Unit = {
    val ballot: Ballot = new Ballot(info.id)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(info.id, Instant.now(), 4.0, 3.0)
    val arbiter: ZScoreArbiter = new ZScoreArbiter

    arbiter.vote(List(1.0, 1.0, 1.0, 2.0, 4.0), ballot, testExecution, this.minimumHistoricalData) shouldBe defined
    arbiter.vote(List(4.0, 4.0, 4.0, 4.0), ballot, testExecution, this.minimumHistoricalData) shouldBe empty
  }


  /** Checks that a test with a response time within the specified percentile threshold does not fail. */
  @UnitTest
  @ZScoreRequirement(percentile = Double.MaxValue)
  def percentilePassed(info: TestInfo): Unit = {
    val ballot: Ballot = new Ballot(info.id)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(info.id, Instant.now(), 1.0, 0.0)
    val arbiter: ZScoreArbiter = new ZScoreArbiter

    arbiter.maybeThrow(arbiter.vote(List(1.0, 1.0, 1.0, 1.0), ballot, testExecution, this.minimumHistoricalData))
  }


  /** Checks that a test with a response time outside the specified percentile threshold will fail. */
  @UnitTest
  @ZScoreRequirement(percentile = 75.0)
  def percentileFailed(info: TestInfo): Unit = {
    val ballot: Ballot = new Ballot(info.id)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(info.id, Instant.now(), 4.0, 0.0)
    val arbiter: ZScoreArbiter = new ZScoreArbiter

    // catch the thrown exception
    intercept[RequirementViolationException] {
      arbiter.maybeThrow(arbiter.vote(List(1.0, 1.0, 1.0, 4.0), ballot, testExecution, this.minimumHistoricalData))
    }
  }
}
