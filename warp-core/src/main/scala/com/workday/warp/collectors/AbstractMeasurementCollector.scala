package com.workday.warp.collectors

import com.workday.warp.config.CoreWarpProperty.WARP_LOG_MC_STACKTRACES
import com.workday.warp.persistence.TablesLike.TestExecutionRowLikeType
import com.workday.warp.utils.{MeasurementUtils, WarpStopwatch}
import org.apache.commons.io.FileUtils.byteCountToDisplaySize
import com.workday.warp.logger.WarpLogging

import scala.util.Try

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
  */
abstract class AbstractMeasurementCollector extends WarpLogging {

  // TODO consider adding a separate persist method, so measurements can be obtained and then persisted separately

  // whether collector is enabled. can be set to false during initialization, for example if jmx is unavailable
  var isEnabled: Boolean = true // scalastyle:ignore

  /**
   * Priority for this collector. A lower number indicates that this collector should run closer to the test it is
   * measuring.
   */
  val priority: Int = Int.MaxValue

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

  /**
   * Called prior to starting an individual test invocation.
   */
  def startMeasurement(): Unit


  /**
   * Simple error handling around `startMeasurement`. Sets enabled=false if there is an error.
   */
  final def tryStartMeasurement(shouldLogStacktrace: Boolean = WARP_LOG_MC_STACKTRACES.value.toBoolean): Unit = {
    logger.trace(s"starting collector ${this.name}")

    Try(this.startMeasurement()) recover { case exception: Exception =>
      this.isEnabled = false
      if (shouldLogStacktrace) logger.error(s"error starting collector: ${this.toString}:", this.filterStackTrace(exception))
      else logger.error(s"error starting collector: ${this.toString}: ${exception.getMessage}")
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
  final def tryStopMeasurement[T: TestExecutionRowLikeType](
                                                             maybeTestExecution: Option[T],
                                                             shouldLogStacktrace: Boolean = WARP_LOG_MC_STACKTRACES.value.toBoolean
                                                           ): Unit = {
    logger.trace(s"stopping collector ${this.name}")

    Try {
      val initialHeap: Long = MeasurementUtils.heapUsed
      val stopwatch: WarpStopwatch = WarpStopwatch.start(s"Elapsed time to stop ${this.name}")
      this.stopMeasurement(maybeTestExecution)
      stopwatch.stop()
      val endHeap: Long = MeasurementUtils.heapUsed
      logger.trace(s"Initial heap usage: ${byteCountToDisplaySize(initialHeap)}, " +
        s"Resulting heap usage: ${byteCountToDisplaySize(endHeap)}, " +
        s"Difference: ${byteCountToDisplaySize(endHeap-initialHeap)}")
    } recover { case exception: Exception =>
      if (shouldLogStacktrace) logger.error(s"error stopping collector: ${this.toString}:", this.filterStackTrace(exception))
      else logger.error(s"error stopping collector: ${this.toString}: ${exception.getMessage}")
    }
  }


  /** @return a String containing fully qualified name, as well as this collectors enabled status and priority. */
  override def toString: String = s"${this.name}: enabled=${this.isEnabled}, priority=${this.priority}"


  /** @return the fully qualified class name of this collector. */
  def name: String = this.getClass.getCanonicalName
}
