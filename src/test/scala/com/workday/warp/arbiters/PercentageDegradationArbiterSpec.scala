package com.workday.warp.arbiters

import java.util.{Date, UUID}

import com.workday.telemetron.RequirementViolationException
import com.workday.warp.common.CoreWarpProperty._
import com.workday.warp.common.annotation.PercentageDegradationRequirement
import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.persistence.CorePersistenceAware
import com.workday.warp.persistence.TablesLike.TestExecutionRowLike
import com.workday.warp.persistence.TablesLike.RowTypeClasses._
import com.workday.warp.utils.{AnnotationReader, Ballot}
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Created by tomas.mccandless on 5/16/16.
  */
class PercentageDegradationArbiterSpec extends WarpJUnitSpec with CorePersistenceAware {

  // minimum number of measurements necessary for percentage processing to continue.
  private[this] val minimumHistoricalData: Int = 3

  /** Checks that we can detect there isn't a [[PercentageDegradationRequirement]], and read the default percentage. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def noPercentageRequirement(): Unit = {
    val testId: String = this.getTestId
    AnnotationReader.hasPercentageDegradationRequirement(testId) should be (false)
    AnnotationReader.getPercentageDegradationRequirement(testId) should be (WARP_PERCENTAGE_DEGRADATION_THRESHOLD.value.toDouble)
  }


  /** Checks that we can read the annotated [[PercentageDegradationRequirement]] and its percentage. */
  @Test
  @Category(Array(classOf[UnitTest]))
  @PercentageDegradationRequirement(percentage = 50)
  def hasPercentageRequirement(): Unit = {
    val testId: String = this.getTestId
    AnnotationReader.hasPercentageDegradationRequirement(testId) should be (true)
    AnnotationReader.getPercentageDegradationRequirement(testId) should be (50.0)
  }


  /** Checks behavior when there are not enough datapoints. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def notEnoughData(): Unit = {
    val testId: String = s"com.workday.warp.ZScore.${UUID.randomUUID().toString}"
    val ballot: Ballot = new Ballot(testId)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(testId, new Date, 4.0, 3.0)
    val arbiter: PercentageDegradationArbiter = new PercentageDegradationArbiter
    arbiter.vote(ballot, testExecution) should be (empty)
  }


  /** Checks that the result of voting is defined or empty based on the provided response times. */
  @Test
  @Category(Array(classOf[UnitTest]))
  @PercentageDegradationRequirement(percentage = 20)
  def percentageVote(): Unit = {
    val ballot: Ballot = new Ballot(this.getTestId)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(this.getTestId, new Date, 4.0, 5.0)

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
