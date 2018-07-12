package com.workday.telemetron

import java.time.Duration

import org.junit.runner.Description

import scala.collection.mutable

/**
  * This abstract class describes the methods available on a TestResult.
  * The values of a TestResult are filled in by the infrastructure. The lone exception to this is ResponseTime. A test
  * may declare its own notion of ResponseTime and inject it into the TestResult with the setter for that attribute.
  *
  * This capability is allowed to accommodate the possibility that there is a more accurate representation of test
  * response time than wall clock time.
  *
  * Created by leslie.lam on 12/13/17.
  * Based on a java interface created by michael.ottati.
  */
trait TestResult {

  /**
    * Private mutable [[Duration]] representing the response time of a test.
    * The default accessor and mutator are overridden.
    */
  private var _responseTime: Duration = Duration.ZERO

  /**
    * The completion status of the test.
    */
  var status: Status.Value

  /**
    * Returns the JUnit [[Description]] containing information about the test.
    *
    * @return The [[Description]] object for this test.
    */
  val description: Option[Description]

  /**
    * Returns the sequence number of this test. Sequence numbers can be negative or zero. These denote
    * that this was a warmup iteration.
    *
    * @return sequenceNumber
    */
  val sequenceNumber: Int

  /**
    * Mutable list containing errors
    */
  val errors: mutable.ListBuffer[Throwable] = mutable.ListBuffer()

  /**
    * A [[Duration]] representing the response time of a test. The response time may be different than the
    * actual amount of time that was spent in the test method.
    *
    * @return measured response time of a test.
    */
  def responseTime: Duration = if (this._responseTime == Duration.ZERO) this.getElapsedTime else this._responseTime

  /**
    * Sets the response time value.
    *
    * This method should be used when a test can "self report" its own response time more accurately than could represented
    * by elapsed wall clock time. For example, a test may instead wish to report its response time by parsing a log.
    *
    * @param responseTime The response time to be set.
    */
  def responseTime_=(responseTime: Duration): Unit = {
    if (!(this._responseTime == Duration.ZERO)) {
      throw new IllegalStateException("Duration may only be set once. The responseTime has already been set to: " + this.responseTime)
    }
    else {
      this._responseTime = responseTime
    }
  }

  /**
    * Returns a [[Duration]] representing the elapsed wall clock time spent executing the test method.
    *
    * This time may be different from getResponseTime() for several reasons. The response
    * is often considerably less than the elapsed time because the response time does not always
    * include the time of measurement collection activities that often surround the test method.
    *
    * @return a [[Duration]] representing elapsed time.
    */
  def getElapsedTime: Duration

  /**
    * Returns a [[Duration]] representing the total CPU time spent inside the test method.
    *
    * @return a [[Duration]] representing thread CPU time.
    */
  def getThreadCPUTime: Duration

  /**
    * Returns empty if this test result did not throw an error.
    * Returns an immutable list of throwable causes if this test actually threw an exception.
    *
    * @return a collection of [[Throwable]] thrown by the measured test.
    */
  def getErrors: Iterable[Throwable] = this.errors.toList

  /**
    * An enumeration representing the possible states of a test result.
    */
  object Status {
    sealed trait Value
    case object notStarted extends Value
    case object started extends Value
    case object succeeded extends Value
    case object failed extends Value
    case object skipped extends Value
  }
}
