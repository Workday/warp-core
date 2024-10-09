package com.workday.warp.arbiters

import com.workday.warp.TestId
import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import com.workday.warp.persistence.Tables._
import com.workday.warp.persistence.Tables.RowTypeClasses._
import com.workday.warp.persistence.TablesLike._
import com.workday.warp.persistence.TablesLike.RowTypeClasses._

import java.time.Instant
import java.util.UUID

class ArbiterLikeSpec extends WarpJUnitSpec with ArbiterLike {

  @UnitTest
  def readSpikeFilterSettings(): Unit = {
    val methodSignature = s"com.workday.warp.arbiters.${UUID.randomUUID.toString}"
    val testId = TestId.fromString(methodSignature)
    val ballot = new Ballot(testId)
    val testExec = this.persistenceUtils.createTestExecution(testId, Instant.now(), 5.0, 6.0)

    val settingsRow = SpikeFilterSettingsRow(testExec.idTestDefinition, false, 10, 10)
    this.persistenceUtils.writeSpikeFilterSettings(Seq(settingsRow))

    this.spikeFilterSettings(ballot, testExec) should be (settingsRow.spikeFilterEnabled, settingsRow.alertOnNth)
  }

  /**
    * Checks that the measured test passed its performance requirement. If the requirement is failed, constructs an
    * error with a useful message wrapped in an Option.
    *
    * @param ballot        box used to register vote result.
    * @param testExecution [[TestExecutionRowLikeType]] we are voting on.
    * @return a wrapped error with a useful message, or None if the measured test passed its requirement.
    */
  override def vote[T: TestExecutionRowLikeType](ballot: Ballot, testExecution: T): Option[Throwable] = None
}
