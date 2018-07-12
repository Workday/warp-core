package com.workday.warp.arbiters

import java.util.Date
import java.util.concurrent.TimeUnit

import com.workday.telemetron.annotation.Required
import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.persistence.CorePersistenceAware
import com.workday.warp.persistence.TablesLike.TestExecutionRowLike
import com.workday.warp.persistence.TablesLike.RowTypeClasses._
import com.workday.warp.utils.Ballot
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Created by tomas.mccandless on 7/7/16.
  */
class ResponseTimeArbiterSpec extends WarpJUnitSpec with CorePersistenceAware {


  /** Checks that [[ResponseTimeArbiter]] gives an empty vote when there is no [[Required]] annotation. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def noRequired(): Unit = {
    val ballot: Ballot = new Ballot(this.getTestId)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(this.getTestId, new Date, 1.0, 4.0)
    val arbiter: ResponseTimeArbiter = new ResponseTimeArbiter

    arbiter.vote(ballot, testExecution) should be (empty)
  }


  /** Checks that [[ResponseTimeArbiter]] gives an empty vote when threshold in [[Required]] annotation is not exceeded. */
  @Test
  @Category(Array(classOf[UnitTest]))
  @Required(maxResponseTime = 3.0)
  def requiredPassed(): Unit = {
    val testId: String = this.getTestId
    val ballot: Ballot = new Ballot(testId)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(testId, new Date, 2.0, 3.0)
    val arbiter: ResponseTimeArbiter = new ResponseTimeArbiter

    arbiter.vote(ballot, testExecution) should be (empty)
  }


  /** Checks that [[ResponseTimeArbiter]] votes on failure when threshold in [[Required]] annotation is exceeded. */
  @Test
  @Category(Array(classOf[UnitTest]))
  @Required(maxResponseTime = 3.0)
  def requiredFailed(): Unit = {
    val testId: String = this.getTestId
    val ballot: Ballot = new Ballot(testId)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(testId, new Date, 4.0, 3.0)
    val arbiter: ResponseTimeArbiter = new ResponseTimeArbiter

    val vote: Option[Throwable] = arbiter.vote(ballot, testExecution)
    vote should not be empty
    vote.get.getMessage should be (s"$testId violated response time requirement: expected 0:00:03.000 (3000 ms), but " +
      "measured 0:00:04.000 (4000 ms)")
  }


  /** Checks that [[ResponseTimeArbiter]] votes on failure when threshold in [[Required]] annotation is exceeded. */
  @Test
  @Category(Array(classOf[UnitTest]))
  @Required(maxResponseTime = 3000, timeUnit = TimeUnit.MILLISECONDS)
  def requiredFailedMillis(): Unit = {
    val testId: String = this.getTestId
    val ballot: Ballot = new Ballot(testId)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(testId, new Date, 4.0, 3.0)
    val arbiter: ResponseTimeArbiter = new ResponseTimeArbiter

    val vote: Option[Throwable] = arbiter.vote(ballot, testExecution)
    vote should not be empty
    vote.get.getMessage should be (s"$testId violated response time requirement: expected 0:00:03.000 (3000 ms), but " +
      "measured 0:00:04.000 (4000 ms)")
  }
}
