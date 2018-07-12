package com.workday.warp.common.utils

import java.util.concurrent.TimeUnit

import com.google.common.base.Stopwatch
import org.pmw.tinylog.Logger

/**
  * A simple wrapper around the guava [[Stopwatch]] class.
  *
  * Created by tomas.mccandless on 10/13/15.
  */
class WarpStopwatch private(private[this] val logMessage: String) {
  private[this] var isTiming: Boolean = true
  private[this] val stopwatch: Stopwatch = Stopwatch.createStarted()

  /**
    * Stops the stopwatch and logs timing information. Throws an exception if the stopwatch is not currently in use.
    *
    * This instance of WarpStopwatch should no longer be used. Client should request a new instance via
    * the start method.
    */
  @throws[IllegalStateException]("when stopwatch has not been started")
  def stop(): Unit = {
    if (!this.isTiming) {
      throw new IllegalStateException("Stopwatch must be started before it can be stopped.")
    }

    Logger.trace(s"WarpStopwatch: ${this.logMessage} ${this.stopwatch.stop()}")
    this.isTiming = false
  }


  /** @return elapsed time in milliseconds. */
  def elapsedMilliseconds(): Long = this.elapsed(TimeUnit.MILLISECONDS)


  /**
    * @param desiredTimeUnit desired TimeUnit for the result to be expressed in.
    * @return elapsed time in TimeUnit.
    */
  def elapsed(desiredTimeUnit: TimeUnit): Long = this.stopwatch.elapsed(desiredTimeUnit)
}


object WarpStopwatch {
  /**
    * This function returns a new WarpStopwatch whose timer has been started.
    *
    * @param logMessage the string that should be printed along with timing info.
    * @return a new WarpStopwatch. Client should call stop() on the returned object
    *         to print out the timing information.
    */
  def start(logMessage: String): WarpStopwatch = new WarpStopwatch(logMessage)

  /**
    * Overloaded start method. Simply calls the other version with an empty String.
    *
    * @return a new WarpStopwatch. Client should call stop() on the returned object
    *         to print out the timing information.
    */
  def start: WarpStopwatch = start("")
}
