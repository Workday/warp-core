package com.workday.warp.arbiters

import java.util.concurrent.TimeUnit
import java.time.Instant

import com.workday.warp.Required
import com.workday.warp.TestIdImplicits._
import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import com.workday.warp.persistence.CorePersistenceAware
import com.workday.warp.persistence.TablesLike.TestExecutionRowLike
import com.workday.warp.persistence.TablesLike.RowTypeClasses._
import org.junit.jupiter.api.TestInfo

/**
  * Created by tomas.mccandless on 7/7/16.
  */
class ResponseTimeArbiterSpec extends WarpJUnitSpec with CorePersistenceAware {


  /** Checks that [[ResponseTimeArbiter]] gives an empty vote when there is no [[Required]] annotation. */
  @UnitTest
  def noRequired(info: TestInfo): Unit = {
    val ballot: Ballot = new Ballot(info.testId)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(info.testId, Instant.now(), 1.0, 4.0)
    val arbiter: ResponseTimeArbiter = new ResponseTimeArbiter

    arbiter.vote(ballot, testExecution) should be (empty)
  }


  /** Checks that [[ResponseTimeArbiter]] gives an empty vote when threshold in [[Required]] annotation is not exceeded. */
  @UnitTest
  @Required(maxResponseTime = 3.0)
  def requiredPassed(info: TestInfo): Unit = {
    val testId: String = info.testId
    val ballot: Ballot = new Ballot(testId)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(testId, Instant.now(), 2.0, 3.0)
    val arbiter: ResponseTimeArbiter = new ResponseTimeArbiter

    arbiter.vote(ballot, testExecution) should be (empty)
  }


  /** Checks that [[ResponseTimeArbiter]] votes on failure when threshold in [[Required]] annotation is exceeded. */
  @UnitTest
  @Required(maxResponseTime = 3.0)
  def requiredFailed(info: TestInfo): Unit = {
    val testId: String = info.testId
    val ballot: Ballot = new Ballot(testId)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(testId, Instant.now(), 4.0, 3.0)
    val arbiter: ResponseTimeArbiter = new ResponseTimeArbiter

    val vote: Option[Throwable] = arbiter.vote(ballot, testExecution)
    vote should not be empty
    vote.get.getMessage should be (s"$testId violated response time requirement: expected 0:00:03.000 (3000 ms), but " +
      "measured 0:00:04.000 (4000 ms)")
  }


  /** Checks that [[ResponseTimeArbiter]] votes on failure when threshold in [[Required]] annotation is exceeded. */
  @UnitTest
  @Required(maxResponseTime = 3000, timeUnit = TimeUnit.MILLISECONDS)
  def requiredFailedMillis(info: TestInfo): Unit = {
    val testId: String = info.testId
    val ballot: Ballot = new Ballot(testId)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(testId, Instant.now(), 4.0, 3.0)
    val arbiter: ResponseTimeArbiter = new ResponseTimeArbiter

    val vote: Option[Throwable] = arbiter.vote(ballot, testExecution)
    vote should not be empty
    vote.get.getMessage should be (s"$testId violated response time requirement: expected 0:00:03.000 (3000 ms), but " +
      "measured 0:00:04.000 (4000 ms)")
  }
}
