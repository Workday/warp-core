package com.workday.warp.arbiters

import java.time.Instant
import java.util.UUID

import com.workday.warp.PercentageDegradationRequirement
import com.workday.warp.config.CoreWarpProperty._
import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import com.workday.warp.TestIdImplicits._
import com.workday.warp.persistence.CorePersistenceAware
import com.workday.warp.persistence.TablesLike.TestExecutionRowLike
import com.workday.warp.persistence.TablesLike.RowTypeClasses._
import com.workday.warp.utils.AnnotationReader
import org.junit.jupiter.api.TestInfo

/**
  * Created by tomas.mccandless on 5/16/16.
  */
class PercentageDegradationArbiterSpec extends WarpJUnitSpec with CorePersistenceAware {

  // minimum number of measurements necessary for percentage processing to continue.
  private[this] val minimumHistoricalData: Int = 3

  /** Checks that we can detect there isn't a [[PercentageDegradationRequirement]], and read the default percentage. */
  @UnitTest
  def noPercentageRequirement(info: TestInfo): Unit = {
    val testId: String = info.testId
    AnnotationReader.hasPercentageDegradationRequirement(testId) should be (false)
    AnnotationReader.getPercentageDegradationRequirement(testId) should be (WARP_PERCENTAGE_DEGRADATION_THRESHOLD.value.toDouble)
  }


  /** Checks that we can read the annotated [[PercentageDegradationRequirement]] and its percentage. */
  @UnitTest
  @PercentageDegradationRequirement(percentage = 50)
  def hasPercentageRequirement(info: TestInfo): Unit = {
    val testId: String = info.testId
    AnnotationReader.hasPercentageDegradationRequirement(testId) should be (true)
    AnnotationReader.getPercentageDegradationRequirement(testId) should be (50.0)
  }


  /** Checks behavior when there are not enough datapoints. */
  @UnitTest
  def notEnoughData(): Unit = {
    val testId: String = s"com.workday.warp.ZScore.${UUID.randomUUID().toString}"
    val ballot: Ballot = new Ballot(testId)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(testId, Instant.now(), 4.0, 3.0)
    val arbiter: PercentageDegradationArbiter = new PercentageDegradationArbiter
    arbiter.vote(ballot, testExecution) should be (empty)
  }


  /** Checks that the result of voting is defined or empty based on the provided response times. */
  @UnitTest
  @PercentageDegradationRequirement(percentage = 20)
  def percentageVote(info: TestInfo): Unit = {
    val ballot: Ballot = new Ballot(info.testId)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(info.testId, Instant.now(), 4.0, 5.0)

    val arbiter: PercentageDegradationArbiter = new PercentageDegradationArbiter
    arbiter.vote(List(1.0, 2.0, 3.0), ballot, testExecution, this.minimumHistoricalData) should be (defined)
    arbiter.vote(List(3.5, 3.5, 3.5), ballot, testExecution, this.minimumHistoricalData) should be (empty)
    // check with a number of measurements smaller than the minimum required
    arbiter.vote(List(3.0), ballot, testExecution, this.minimumHistoricalData) should be (empty)

    // shouldn't throw an exception here
    arbiter.maybeThrow(arbiter.vote(List(3.0, 3.5, 4.0), ballot, testExecution, this.minimumHistoricalData))

    // intercept the thrown exception
    intercept[RequirementViolationException] {
      arbiter.maybeThrow(arbiter.vote(List(2.5, 3.0, 3.5), ballot, testExecution, this.minimumHistoricalData))
    }
  }
}
