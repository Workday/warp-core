package com.workday.warp.collectors.abstracts

import com.workday.warp.common.CoreConstants
import com.workday.warp.common.utils.StackTraceFilter.filter
import com.workday.warp.persistence.TablesLike.TestExecutionRowLikeType
import com.workday.warp.common.utils.{MeasurementUtils, WarpStopwatch}
import org.apache.commons.io.FileUtils.byteCountToDisplaySize
import org.pmw.tinylog.Logger

/**
  * Used to start and stop measurement collection.
  *
  * Implementations of a measurement or measurement collection should extend this class, and probably mix in
  * [[com.workday.warp.persistence.CorePersistenceAware]]. Note that the collection of a measurement may encompass a number of
  * different types of operations. It may include such activities as collecting from JMX, scraping server logs, or as
  * simple as starting and stopping a clock.
  *
  * Created by tomas.mccandless on 8/13/15.
  * Based on a java interface created by michael.ottati on 8/20/13.
  *
  * @constructor create a new measurement collector with a test id.
  * @param _testId id of the test being measured. mutable, but the setter is only accessible within the collectors
  *                package.
  */
abstract class AbstractMeasurementCollector(protected[collectors] var _testId: String = CoreConstants.UNDEFINED_TEST_ID) {

  // TODO consider adding a separate persist method, so measurements can be obtained and then persisted separately

  // whether collector is enabled. can be set to false during initialization, for example if jmx is unavailable
  var isEnabled: Boolean = true

  /**
   * Priority for this collector. A lower number indicates that this collector should run closer to the test it is
   * measuring.
   */
  var priority: Int = Int.MaxValue

  /**
    * An intrusive collector is one that should not be enabled for nested measurements. For example, execution metrics
    * or log collectors.
    */
  val isIntrusive: Boolean = false


  /**
    * Filters the stacktrace of `exception` to elide spurious stackframes.
    *
    * Default base implementation is no-op.
    * Subclasses can override this method to implement custom filtering logic.
    *
    * @param exception
    * @return
    */
  def filterStackTrace(exception: Exception): Exception = {
    exception
  }

  /** Public accessor for testId. */
  def testId: String = this._testId

  /**
    * Auxiliary constructor with a default test id.
    * Make sure to call the primary constructor if your collector requires access to test id.
    */
  def this() = this(CoreConstants.UNDEFINED_TEST_ID)

  /**
   * Called prior to starting an individual test invocation.
   */
  def startMeasurement(): Unit


  /**
   * Simple error handling around `startMeasurement`. Sets enabled=false if there is an error.
   */
  final def tryStartMeasurement(): Unit = {
    Logger.trace(s"starting collector ${this.name}")
    try {
      this.startMeasurement()
    }
    catch {
      case exception: Exception =>
        Logger.error(this.filterStackTrace(exception), s"error starting collector: ${this.toString}:")
        this.isEnabled = false
    }
  }


  /**
   * Called after finishing an individual test invocation.
   *
   * @param maybeTestExecution     Optional field. If the test execution is None the client should
   *                          not attempt to write out to the database.
   */
  def stopMeasurement[T: TestExecutionRowLikeType](maybeTestExecution: Option[T]): Unit


  /**
   * Simple error handling around `stopMeasurement`.
   *
   * @param maybeTestExecution     Optional field. If the test execution is None the client should
   *                          not attempt to write out to the database.
   */
  final def tryStopMeasurement[T: TestExecutionRowLikeType](maybeTestExecution: Option[T]): Unit = {
    Logger.trace(s"stopping collector ${this.name}")

    // TODO use scala.util.Try
    try {
      val initialHeap: Long = MeasurementUtils.heapUsed
      val stopwatch: WarpStopwatch = WarpStopwatch.start(s"Elapsed time to stop ${this.name}")
      this.stopMeasurement(maybeTestExecution)
      stopwatch.stop()
      val endHeap: Long = MeasurementUtils.heapUsed
      Logger.trace(s"Initial heap usage: ${byteCountToDisplaySize(initialHeap)}, " +
        s"Resulting heap usage: ${byteCountToDisplaySize(endHeap)}, " +
        s"Difference: ${byteCountToDisplaySize(endHeap-initialHeap)}")
    }
    catch {
      case exception: Exception =>
        Logger.error(this.filterStackTrace(exception), s"error stopping collector: ${this.toString}:")
    }
  }


  /** @return a String containing fully qualified name, as well as this collectors enabled status and priority. */
  override def toString: String = s"${this.name}: enabled=${this.isEnabled}, priority=${this.priority}"


  /** @return the fully qualified class name of this collector. */
  def name: String = this.getClass.getCanonicalName
}
