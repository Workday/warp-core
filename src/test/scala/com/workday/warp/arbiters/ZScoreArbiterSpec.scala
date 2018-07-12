package com.workday.warp.arbiters

import java.util.{Date, UUID}

import com.workday.telemetron.RequirementViolationException
import com.workday.warp.common.annotation.ZScoreRequirement
import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.persistence.CorePersistenceAware
import com.workday.warp.persistence.TablesLike.TestExecutionRowLike
import com.workday.warp.persistence.TablesLike.RowTypeClasses._
import com.workday.warp.utils.{AnnotationReader, Ballot}
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Created by tomas.mccandless on 1/26/16.
  */
class ZScoreArbiterSpec extends WarpJUnitSpec with CorePersistenceAware {

  // minimum number of measurements necessary for percentile processing to continue.
  private[this] val minimumHistoricalData: Int = 3

  /** Checks that we can set a custom threshold. */
  @Test
  @Category(Array(classOf[UnitTest]))
  @ZScoreRequirement(percentile = 99.9)
  def hasPercentileThreshold(): Unit = {
    val testId: String = this.getTestId
    AnnotationReader.getZScoreRequirement(testId) shouldBe 99.9
    AnnotationReader.hasZScoreRequirement(testId) shouldBe true
  }

  /** Checks that we can detect there is no percentile requirement set */
  @Test
  @Category(Array(classOf[UnitTest]))
  def noPercentileThreshold(): Unit = {
    AnnotationReader.hasZScoreRequirement(this.getTestId) shouldBe false
  }

  /** Checks that the provided percentile threshold is truncated to 100.0. */
  @Test
  @Category(Array(classOf[UnitTest]))
  @ZScoreRequirement(percentile = 100.2345)
  def percentileThreshold(): Unit = {
    AnnotationReader.getZScoreRequirement(this.getTestId) shouldBe 100.0
  }


  /** Checks that the provided percentile threshold is truncated to 0.0. */
  @Test
  @Category(Array(classOf[UnitTest]))
  @ZScoreRequirement(percentile = -1.2345)
  def percentileThresholdNegative(): Unit = {
    AnnotationReader.getZScoreRequirement(this.getTestId) shouldBe 0.0
  }


  /** Checks behavior when there are not enough datapoints. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def notEnoughData(): Unit = {
    val testId: String = s"com.workday.warp.ZScore.${UUID.randomUUID().toString}"
    val ballot: Ballot = new Ballot(testId)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(testId, new Date, 4.0, 3.0)
    val arbiter: ZScoreArbiter = new ZScoreArbiter
    arbiter.vote(ballot, testExecution) should be (empty)
  }


  /** Checks that the Option[Throwable] is defined or empty based on the provided response times. */
  @Test
  @Category(Array(classOf[UnitTest]))
  @ZScoreRequirement(percentile = 95.0)
  def percentileVote(): Unit = {
    val ballot: Ballot = new Ballot(this.getTestId)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(this.getTestId, new Date, 4.0, 3.0)
    val arbiter: ZScoreArbiter = new ZScoreArbiter

    arbiter.vote(List(1.0, 1.0, 1.0, 2.0, 4.0), ballot, testExecution, this.minimumHistoricalData) shouldBe defined
    arbiter.vote(List(4.0, 4.0, 4.0, 4.0), ballot, testExecution, this.minimumHistoricalData) shouldBe empty
  }


  /** Checks that a test with a response time within the specified percentile threshold does not fail. */
  @Test
  @Category(Array(classOf[UnitTest]))
  @ZScoreRequirement(percentile = Double.MaxValue)
  def percentilePassed(): Unit = {
    val ballot: Ballot = new Ballot(this.getTestId)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(this.getTestId, new Date, 1.0, 0.0)
    val arbiter: ZScoreArbiter = new ZScoreArbiter

    arbiter.maybeThrow(arbiter.vote(List(1.0, 1.0, 1.0, 1.0), ballot, testExecution, this.minimumHistoricalData))
  }


  /** Checks that a test with a response time outside the specified percentile threshold will fail. */
  @Test
  @Category(Array(classOf[UnitTest]))
  @ZScoreRequirement(percentile = 75.0)
  def percentileFailed(): Unit = {
    val ballot: Ballot = new Ballot(this.getTestId)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(this.getTestId, new Date, 4.0, 0.0)
    val arbiter: ZScoreArbiter = new ZScoreArbiter

    // catch the thrown exception
    intercept[RequirementViolationException] {
      arbiter.maybeThrow(arbiter.vote(List(1.0, 1.0, 1.0, 4.0), ballot, testExecution, this.minimumHistoricalData))
    }
  }
}
