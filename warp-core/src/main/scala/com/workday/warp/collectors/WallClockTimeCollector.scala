package com.workday.warp.collectors

import com.workday.warp.persistence.CorePersistenceAware
import com.workday.warp.persistence.TablesLike._
import com.workday.warp.persistence.Tables._

/**
  * A [[AbstractMeasurementCollector]] that records elapsed wall clock time.
  *
  */
class WallClockTimeCollector extends AbstractMeasurementCollector with CorePersistenceAware {

  // scalastyle:off var.field
  private var timeBeforeMs: Long = _
  private var timeAfterMs: Long = _
  // scalastyle:on

  private val description: String = "elapsed wall clock time"

  /**
    * Called prior to starting an individual test invocation.
    */
  override def startMeasurement(): Unit = {
    this.timeBeforeMs = System.currentTimeMillis()
  }

  /**
    * Called after finishing an individual test invocation.
    *
    * @param maybeTestExecution Optional field. If the test execution is None the client should
    *                      not attempt to write out to the database.
    */
  override def stopMeasurement[T: TestExecutionRowLikeType](maybeTestExecution: Option[T]): Unit = {
    this.timeAfterMs = System.currentTimeMillis()
    val durationMs: Long = this.timeAfterMs - this.timeBeforeMs

    maybeTestExecution foreach { testExecution: T =>
      this.persistenceUtils.recordMeasurement(testExecution.idTestExecution, this.description, durationMs)
    }
  }
}
