package com.workday.telemetron.utils

import java.time.Duration
import java.util.concurrent.TimeUnit


/**
  * This class is a collection point for time utilities.
  *
  * Created by leslie.lam on 12/13/17.
  * Based on a java class created by michael.ottati on 9/17/15.
  */
object TimeUtils {

  /**
    * Converts a long nanosecond value to a double representing the same value.
    *
    * @param nanos Nanoseconds
    * @return seconds
    */
  def nanosToDoubleSeconds(nanos: Long): Double = nanos.toDouble / TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS).toDouble

  /**
    * Converts a long millisecond value to a double representing the same value.
    *
    * @param millis Milliseconds
    * @return seconds
    */
  def millisToDoubleSeconds(millis: Long): Double = millis.toDouble / TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS).toDouble

  /**
    * @param duration The { @link java.time.Duration} to be converted.
    * @return seconds
    */
  def durationToDoubleSeconds(duration: Duration): Double = nanosToDoubleSeconds(duration.toNanos)

  /**
    * Convert a {@link java.time.Duration} into a human readable string with a format of:
    * HH:MM:SS.mmm
    * Where:
    * HH = Hours
    * MM = Minutes
    * SS = Seconds
    * mmm = Milliseconds
    *
    * @param duration The duration to be converted.
    * @return Human readable time string.
    */
  def humanReadableDuration(duration: Duration): String = {
    val hours: Long = duration.toHours
    val minutes: Long = duration.minusHours(hours).toMinutes
    val seconds: Long = duration.minusHours(hours).minusMinutes(minutes).getSeconds
    val milliseconds: Long = duration.minusHours(hours).minusMinutes(minutes).minusSeconds(seconds).toMillis
    f"$hours%01d:$minutes%02d:$seconds%02d.$milliseconds%03d"
  }

  /**
    * Converts a long millisecond value to a human readable string.
    *
    * @param millis
    * @return
    */
  def millisToHumanReadable(millis: Long): String = humanReadableDuration(Duration.ofMillis(millis))

  /**
    * Converts a double in the specified time unit to a long value representing that same duration in nanoseconds.
    *
    * @param time     time duration to convert
    * @param timeUnit time unit to interpret time under
    * @return time converted to nanoseconds
    */
  def toNanos(time: Double, timeUnit: TimeUnit): Long = (time * timeUnit.toNanos(1)).toLong
}
