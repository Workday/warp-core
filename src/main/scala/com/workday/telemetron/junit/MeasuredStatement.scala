package com.workday.telemetron.junit

import java.lang.management.ManagementFactory
import java.time.{Duration, Instant}

import com.workday.telemetron.annotation.{Measure, Required}
import com.workday.telemetron.utils.Implicits._
import com.workday.telemetron._
import com.workday.warp.collectors.{AbstractMeasurementCollectionController, Defaults}
import com.workday.warp.inject.WarpGuicer
import org.junit.runner.Description
import org.junit.runners.model.{MultipleFailureException, Statement}
import org.pmw.tinylog.Logger

import scala.collection.JavaConverters._


/**
  * Surrounds the user written test case with timing measurements.
  *
  * Implements the [[TestResult]] interface. Timings captured by this class may be read through the [[TestResult]] methods.
  *
  * Created by leslie.lam on 12/13/17.
  * Based on java class written by michael.ottati.
  */
class MeasuredStatement(base: Statement,
                        _description: Description,
                        resultReporter: ResultReporter,
                        private implicit val context: TelemetronContext,
                        val sequenceNumber: Int) extends Statement with TestResult {

  // These vars are set during evaluate()
  private var begin: Instant = Instant.MIN
  private var end: Instant = Instant.MIN
  private var cpuBegin: Long = 0
  private var cpuEnd: Long = 0

  override var status: Status.Value = Status.notStarted
  override val description: Option[Description] = Some(this._description)

  override def getThreadCPUTime: Duration = {
    val nanosEnd = if (this.cpuEnd == 0) ManagementFactory.getThreadMXBean.getCurrentThreadCpuTime else this.cpuEnd
    Duration.ofNanos(nanosEnd - this.cpuBegin)
  }

  override def getElapsedTime: Duration = {
    val endTime = if (this.begin.isAfter(this.end)) Instant.now else this.end
    Duration.between(this.begin, endTime)
  }

  override def evaluate(): Unit = {
    val maybeMeasure: Option[Measure] = Option(this._description.getAnnotation(classOf[Measure]))
    val maybeController: Option[AbstractMeasurementCollectionController] = maybeMeasure.map(_ => this.collectionController())
    ThreadLocalTestResult.set(this)

    Logger.trace("Starting: " + this._description.getMethodName + " (" + Thread.currentThread.getName + ")")

    this.status = Status.started
    this.responseTime = Duration.ZERO
    this.begin = Instant.now
    this.cpuBegin = ManagementFactory.getThreadMXBean.getCurrentThreadCpuTime

    maybeController foreach { _.beginMeasurementCollection() }
    try {
      this.base.evaluate()
      MultipleFailureException.assertEmpty(this.verifyRequirement.asJava)
      this.status = Status.succeeded
    } catch {
      case t: Throwable =>
        this.status = Status.failed
        this.errors += t
    }

    this.cpuEnd = ManagementFactory.getThreadMXBean.getCurrentThreadCpuTime
    this.end = Instant.now
    // Set the final responseTime to the difference between wall clock times if it has not already been set.
    if (this.responseTime.isZero) {
      this.responseTime = Instant.now - this.begin
    }

    maybeController foreach { _.endMeasurementCollection(this.responseTime) }

    /* Add result to result reporter if it not a warm-up */
    if (this.sequenceNumber > 0) {
      this.resultReporter.recordResult(this)
    }

    this.errors synchronized {
      if (this.errors.nonEmpty) {
        throw this.errors.last
      }
    }
  }

  /**
    * Verifies any requirements set in annotations. Currently, just checks the response time requirement.
    *
    * @return a List containing requirement violation exceptions.
    */
  private def verifyRequirement: List[Throwable] = {
    val maybeThrowable: Option[Throwable] = for {
      requirement <- Option(this._description.getAnnotation(classOf[Required]))
      error <- requirement.failedTimeRequirement(this.responseTime)
    } yield error
    maybeThrowable.toList
  }

  /** @return a configured [[AbstractMeasurementCollectionController]] ready to instrument a measured function. */
  private def collectionController(): AbstractMeasurementCollectionController = {
    // fully qualified name of the executing junit method.
    val testId: String = s"${this._description.getClassName}.${this._description.getMethodName}"
    WarpGuicer.getController(testId, Defaults.tags)
  }
}
