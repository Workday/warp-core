package com.workday.warp.arbiters

import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import com.workday.warp.persistence.{CorePersistenceAware, TablesLike}
import com.workday.warp.TestIdImplicits._
import com.workday.warp.persistence.TablesLike.TestExecutionRowLike
import com.workday.warp.persistence.TablesLike.RowTypeClasses._

import java.time.Instant
import java.util.UUID

class ArbiterSpikeFilterSpec extends WarpJUnitSpec with CorePersistenceAware {



  @UnitTest
  def spikeFilter(): Unit = {
    val testId: String = s"com.workday.warp.SpikeFilter.${UUID.randomUUID().toString}"
    val ballot: Ballot = new Ballot(testId)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(testId, Instant.now(), 4.0, 3.0)
    val testExecution2: TestExecutionRowLike = this.persistenceUtils.createTestExecution(testId, Instant.now(), 4.0, 3.0)
    val arbiter: AlwaysFailArbiter = new AlwaysFailArbiter
    arbiter.voteWithSpikeFilter(ballot, testExecution, true, -1) should not be empty
    // first time failure should be ignored
    arbiter.voteWithSpikeFilter(ballot, testExecution, true, 2) should be (empty)
    arbiter.voteWithSpikeFilter(ballot, testExecution2, true, 2) should not be empty
  }


  @UnitTest
  def spikeFilterAlertOn6th(): Unit = {
    val testId: String = s"com.workday.warp.SpikeFilter.${UUID.randomUUID().toString}"
    val ballot: Ballot = new Ballot(testId)
    val arbiter: AlwaysFailArbiter = new AlwaysFailArbiter
    (1 to 5).foreach { _ =>
      val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(testId, Instant.now(), 4.0, 3.0)
      arbiter.voteWithSpikeFilter(ballot, testExecution, true, 6) should be (empty)
    }
    // first 5 failures should be ignored
    val testExecution2: TestExecutionRowLike = this.persistenceUtils.createTestExecution(testId, Instant.now(), 4.0, 3.0)
    arbiter.voteWithSpikeFilter(ballot, testExecution2, true, 6) should not be empty
  }
}


class AlwaysFailArbiter extends ArbiterLike {
  override def vote[T: TablesLike.TestExecutionRowLikeType](ballot: Ballot,
                                                            testExecution: T): Option[Throwable] = Option(new RuntimeException)
}
