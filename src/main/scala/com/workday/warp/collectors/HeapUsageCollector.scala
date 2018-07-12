package com.workday.warp.collectors

import com.workday.warp.collectors.abstracts.AbstractMeasurementCollector
import com.workday.warp.persistence.CorePersistenceAware
import com.workday.warp.persistence.TablesLike.TestExecutionRowLikeType
import com.workday.warp.persistence.Tables._

/**
  * A [[AbstractMeasurementCollector]] that records observed heap usage of the current process.
  *
  * @param testId fully qualified name of the method being measured.
  */
class HeapUsageCollector(testId: String) extends AbstractMeasurementCollector(testId) with CorePersistenceAware {

  private var heapUsedBefore: Long = _
  private var heapUsedAfter: Long = _

  private val beforeDescription: String = "heap used before"
  private val afterDescription: String = "heap used after"
  private val deltaDescription: String = "heap used delta"

  /**
    * Called prior to starting an individual test invocation.
    */
  override def startMeasurement(): Unit = {
    this.heapUsedBefore = this.heapUsedBytes()
  }

  /**
    * Called after finishing an individual test invocation.
    *
    * Records heap usage of the current process as observed before and after the executed test.
    *
    * @param maybeTestExecution Optional field. If the test execution is None the client should
    *                      not attempt to write out to the database.
    */
  override def stopMeasurement[T: TestExecutionRowLikeType](maybeTestExecution: Option[T]): Unit = {
    this.heapUsedAfter = this.heapUsedBytes()

    maybeTestExecution foreach { testExecution: T =>
      this.persistenceUtils.recordMeasurement(testExecution.idTestExecution, this.beforeDescription, this.heapUsedBefore)
      this.persistenceUtils.recordMeasurement(testExecution.idTestExecution, this.afterDescription, this.heapUsedAfter)
      this.persistenceUtils.recordMeasurement(
        testExecution.idTestExecution,
        this.deltaDescription,
        this.heapUsedAfter - this.heapUsedBefore
      )
    }
  }

  /** @return number of bytes currently used on the heap. */
  def heapUsedBytes(): Long = {
    val runtime: Runtime = Runtime.getRuntime
    runtime.totalMemory() - runtime.freeMemory()
  }
}
