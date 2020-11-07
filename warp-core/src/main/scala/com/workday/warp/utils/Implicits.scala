package com.workday.warp.utils

import java.math.BigInteger
import java.time.temporal.Temporal
import java.time.{Duration, Instant}
import java.util.Optional
import java.util.concurrent.TimeUnit

import com.google.gson._
import com.workday.warp.utils.TimeUtils.toNanos
import com.workday.warp.Required
import com.workday.warp.arbiters.RequirementViolationException

import scala.util.{Failure, Success, Try}

/**
  * Utility implicits.
  *
  * Created by tomas.mccandless on 5/5/16.
  */
object Implicits {

  /**
    * Pimps [[Array]] with some utility combinators.
    *
    * @param array the underlying container [[Array]]
    * @tparam A the element type of the [[Array]]
    */
  implicit class DecoratedArray[A](array: Array[A]) {

    /**
      * Creates a Map from the list of (k, v) pairs obtained by applying `keyValuePair` to each element.
      *
      * @param keyValuePair function that accepts an A and computes a (key, value) pair of type (B, C)
      * @tparam B key type in the resulting Map
      * @tparam C value type in the resulting Map
      * @return a [[Map]] of (k, v) pairs obtained by applying `keyValuePair` to each element.
      */
    def mapWithImage[B, C](keyValuePair: A => (B, C)): Map[B, C] = {
      (this.array map { e: A => keyValuePair(e) }).toMap
    }


    /**
      * Creates a Map from the list of (k, v) pairs obtained by applying `key` and `value` to each element.
      *
      * @param key function that accepts an A and computes a key of type B in the resulting [[Map]].
      * @param value function that accepts an A and computes a value of type C in the resulting [[Map]].
      * @tparam B key type in the resulting [[Map]].
      * @tparam C value type in the resulting [[Map]].
      * @return a [[Map]] of (key(e), value(e)) pairs obtained by applying `key` and `value` to each element.
      */
    def mapWithImages[B, C](key: A => B)(value: A => C): Map[B, C] = {
      (this.array map { e: A => key(e) -> value(e) }).toMap
    }
  }


  /**
    * Decorates [[Required]] with nice failure functions.
    *
    * @param requirement
    */
  implicit class DecoratedRequired(requirement: Required) {

    /**
      * Checks if the time requirement has been violated
      *
      * @param responseTime A [[Duration]] containing the response time of the test.
      * @return Option containing a Throwable if the time requirement is violated.
      */
    // TODO do we need some other context?
    def failedTimeRequirement(responseTime: Duration, verifyResponseTime: Boolean = true): Option[Throwable] = {
      val threshold: Double = this.requirement.maxResponseTime
      val timeUnit: TimeUnit = this.requirement.timeUnit
      val maxResponseTime: Duration = Duration.ofNanos(toNanos(threshold, timeUnit))

      if (verifyResponseTime && maxResponseTime.isPositive && responseTime > maxResponseTime) {
        val error: String = s"Response time requirement exceeded, " +
          s"specified: ${maxResponseTime.humanReadable} (${maxResponseTime.toMillis} ms) " +
          s"observed: ${responseTime.humanReadable} (${responseTime.toMillis} ms)"
        Some(new RequirementViolationException(error))
      }
      else {
        None
      }
    }
  }

  implicit class DecoratedInstant(instant: Instant) {

    /**
      * Subtracts `other` from this [[Instant]].
      *
      * @param other
      * @return
      */
    def -(other: Temporal): Duration = Duration.between(other, this.instant)
  }


  /**
    * Pimps [[Duration]] with some convenience functions.
    *
    * @param duration
    */
  implicit class DecoratedDuration(duration: Duration) {

    def <(other: Duration): Boolean = this.duration.compareTo(other) < 0
    def <=(other: Duration): Boolean = this.duration.compareTo(other) <= 0
    def >=(other: Duration): Boolean = this.duration.compareTo(other) >= 0
    def >(other: Duration): Boolean = this.duration.compareTo(other) > 0

    /** @return true iff this [[Duration]] is strictly positive. */
    def isPositive: Boolean = this.duration > Duration.ZERO

    /** @return a human-readable representation of this [[Duration]]. */
    def humanReadable: String = TimeUtils.humanReadableDuration(this.duration)

    /** @return a [[Double]] representing this duration in seconds. */
    def doubleSeconds: Double = TimeUtils.millisToDoubleSeconds(this.duration.toMillis)

    /**
      * Adds `other` to this [[Duration]].
      *
      * @param other
      * @return the sum of this [[Duration]] and `other`.
      */
    def +(other: Duration): Duration = this.duration plus other

    /**
      * Subtracts `other` from this [[Duration]].
      *
      * @param other
      * @return
      */
    def -(other: Duration): Duration = this.duration minus other

    /**
      * Multiplies this [[Duration]] by `scalar`
      *
      * @param scalar
      * @return
      */
    def *(scalar: Long): Duration = this.duration multipliedBy scalar

    /**
      * Multiplies this [[Duration]] by `scalar`
      *
      * @param scalar
      * @return
      */
    def *(scalar: Double): Duration = (this.duration.toMillis * scalar).milliseconds

    /**
      * Determines the max of this [[Duration]] and `other`
      *
      * @param other
      * @return
      */
    def max(other: Duration): Duration = if (this.duration > other) this.duration else other
  }




  /**
    * Pimps [[JsonObject]] to decorate with some convenience functions.
    */
  implicit class DecoratedJsonObject(json: JsonObject) {

    /**
      * Gets the value associated with `key` in `json`. If `key` is not present, returns `default`.
      *
      * @param key will be looked up in `json`
      * @param default will be returned if `key` does not have a value in `json`
      * @tparam T the expected type of the value associated with `key`
      * @return the value associated with `key`, or `default` if `key` is not present in `json`
      */
    def getOrElse[T](key: String, default: T): T = {
      if (this.json.has(key)) this.getAsT(this.json.get(key), default.getClass) else default
    }


    // scalastyle:off cyclomatic.complexity
    /**
      * Converts the specified json element to an instance of type T. Small wrapper around the provided getAs* functions.
      *
      * @param json json element to convert
      * @param myType Class to convert json element to
      * @tparam T expected type of the json element
      * @return json element converted to type T
      */
    private[this] def getAsT[T](json: JsonElement, myType: Class[T]): T = {
      val result: Any = myType match {
        case t if t == classOf[BigDecimal] || t == classOf[java.math.BigDecimal] => json.getAsBigDecimal
        case t if t == classOf[BigInteger] => json.getAsBigInteger
        case t if t == classOf[Boolean] || t == classOf[java.lang.Boolean] => json.getAsBoolean
        case t if t == classOf[Byte] || t == classOf[java.lang.Byte] => json.getAsByte
        case t if t == classOf[Character] || t == classOf[java.lang.Character] => json.getAsCharacter
        case t if t == classOf[Double] || t == classOf[java.lang.Double] => json.getAsDouble
        case t if t == classOf[Float] || t == classOf[java.lang.Float] => json.getAsFloat
        case t if t == classOf[Int] || t == classOf[java.lang.Integer] => json.getAsInt
        case t if t == classOf[JsonArray] => json.getAsJsonArray
        case t if t == classOf[JsonNull] => json.getAsJsonNull
        case t if t == classOf[JsonObject] => json.getAsJsonObject
        case t if t == classOf[JsonPrimitive] => json.getAsJsonPrimitive
        case t if t == classOf[Long] || t == classOf[java.lang.Long] => json.getAsLong
        case t if t == classOf[Number]=> json.getAsNumber
        case t if t == classOf[Short] || t == classOf[java.lang.Short] => json.getAsShort
        case t if t == classOf[String] => json.getAsString
      }

      result.asInstanceOf[T]
    }
    // scalastyle:on
  }

  /**
    * Pimps [[java.sql.Date]] to decorate with some convenience functions
    */
  implicit class DecoratedDate(date: java.sql.Date) {
    import java.time.Duration
    def isWithin24HoursOf(other: java.sql.Date): Boolean = {
      val absoluteTimeDifferenceInDays: Long = Duration.ofMillis(this.date.getTime - other.getTime).abs().toDays

      absoluteTimeDifferenceInDays < 1
    }
  }



  /** Decorates [[Double]] with conversion functions to [[java.time.Duration]] */
  implicit class DecoratedDouble(val d: Double) {
    def nanoseconds: Duration = Duration ofNanos TimeUtils.toNanos(this.d, TimeUnit.NANOSECONDS)
    def nanos: Duration = this.nanoseconds
    def nano: Duration = this.nanoseconds
    def microseconds: Duration = Duration ofNanos TimeUtils.toNanos(this.d, TimeUnit.MICROSECONDS)
    def micros: Duration = this.microseconds
    def micro: Duration = this.microseconds
    def milliseconds: Duration = Duration ofNanos TimeUtils.toNanos(this.d, TimeUnit.MILLISECONDS)
    def millis: Duration = this.milliseconds
    def milli: Duration = this.milliseconds
    def seconds: Duration = Duration ofNanos TimeUtils.toNanos(this.d, TimeUnit.SECONDS)
    def second: Duration = this.seconds
    def minutes: Duration = Duration ofNanos TimeUtils.toNanos(this.d, TimeUnit.MINUTES)
    def minute: Duration = this.minutes
    def hours: Duration = Duration ofNanos TimeUtils.toNanos(this.d, TimeUnit.HOURS)
    def hour: Duration = this.hours
    def days: Duration = Duration ofNanos TimeUtils.toNanos(this.d, TimeUnit.DAYS)
    def day: Duration = this.days
  }



  /** Decorates [[Int]] with conversion functions to [[java.time.Duration]] */
  implicit class DecoratedInt(val n: Int) {
    def nanoseconds: Duration = Duration ofNanos this.n
    def nanos: Duration = this.nanoseconds
    def nano: Duration = this.nanoseconds
    def microseconds: Duration = Duration ofNanos TimeUtils.toNanos(this.n, TimeUnit.MICROSECONDS)
    def micros: Duration = this.microseconds
    def micro: Duration = this.microseconds
    def milliseconds: Duration = Duration ofMillis this.n
    def millis: Duration = this.milliseconds
    def milli: Duration = this.milliseconds
    def seconds: Duration = Duration ofSeconds this.n
    def second: Duration = this.seconds
    def minutes: Duration = Duration ofMinutes this.n
    def minute: Duration = this.minutes
    def hours: Duration = Duration ofHours this.n
    def hour: Duration = this.hours
    def days: Duration = Duration ofDays this.n
    def day: Duration = this.days


    /**
      * Invokes `function` n times, collecting all results in a [[List]].
      *
      * @param function function to be invoked.
      * @tparam T return type of `function`.
      * @return a [[List]] containing results of n invocations of `function`.
      */
    def times[T](function: => T): List[T] = (1 to this.n).toList map { _ => function }
  }



  /** Decorates [[Long]] with conversion functions to [[java.time.Duration]] */
  implicit class DecoratedLong(val n: Long) {
    def nanoseconds: Duration = Duration ofNanos this.n
    def nanos: Duration = this.nanoseconds
    def nano: Duration = this.nanoseconds
    def microseconds: Duration = Duration ofNanos TimeUtils.toNanos(this.n, TimeUnit.MICROSECONDS)
    def micros: Duration = this.microseconds
    def micro: Duration = this.microseconds
    def milliseconds: Duration = Duration ofMillis this.n
    def millis: Duration = this.milliseconds
    def milli: Duration = this.milliseconds
    def seconds: Duration = Duration ofSeconds this.n
    def second: Duration = this.seconds
    def minutes: Duration = Duration ofMinutes this.n
    def minute: Duration = this.minutes
    def hours: Duration = Duration ofHours this.n
    def hour: Duration = this.hours
    def days: Duration = Duration ofDays this.n
    def day: Duration = this.days
  }


  /** Decorates [[Try]] with additional operations. */
  implicit class DecoratedTry[T](val aTry: Try[T]) {

    /**
      * Cleans up after a computation has been wrapped in [[Try]].
      *
      * @param effect function used to cleanup. Return type is ignored.
      * @tparam Ignored return type of `effect`. Unused.
      * @return the result of transforming `aTry` using `effect`.
      */
    def andThen[Ignored](effect: => Ignored): Try[T] = {

       /*
        * Invokes `effect` and returns `aTry`.
        *
        * @param anything ignored. Defined so we can invoke `transform`.
        * @return `aTry`, after invoking `effect` for cleanup.
        */
      val cleanup: Any => Try[T] = _ => {
        effect
        this.aTry
      }

      this.aTry transform (cleanup, cleanup)
    }


    /**
      * Transforms this [[Try]] to an [[Either]].
      * @return
      */
    def toEither: Either[Throwable, T] = aTry match {
      case Success(value) => Right(value)
      case Failure(err) => Left(err)
    }
  }


  /**
    * Decorates [[Option]] with some additional operations.
    *
    * @param maybeT decorated [[Option]].
    */
  implicit class DecoratedOption[T](val maybeT: Option[T]) {

    /** Transforms this [[Option]] to a [[Try]]. */
    def toTry: Try[T] = Try(maybeT.get)
  }


  /**
    * Decorates [[Optional]] with some additional operations.
    *
    * @param maybeT decorated [[Optional]].
    */
  implicit class DecoratedOptional[T](val maybeT: Optional[T]) {
    /** Transforms this [[Optional]] to an [[Option]]. */
    def toOption: Option[T] = if (maybeT.isPresent) Option(maybeT.get) else None

    /** Transforms this [[Optional]] to a [[Try]]. */
    def toTry: Try[T] = toOption.toTry
  }
}
