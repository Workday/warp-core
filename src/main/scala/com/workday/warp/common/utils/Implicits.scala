package com.workday.warp.common.utils

import java.math.BigInteger
import java.time.Duration
import java.util.Optional
import java.util.concurrent.TimeUnit

import com.google.gson._
import com.workday.telemetron.utils.TimeUtils
import com.workday.warp.common.utils.TypeAliases._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Try

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
        case t if t == classOf[BigDecimal] || t == classOf[JavaBigDecimal] => json.getAsBigDecimal
        case t if t == classOf[BigInteger] => json.getAsBigInteger
        case t if t == classOf[Boolean] || t == classOf[JavaBoolean] => json.getAsBoolean
        case t if t == classOf[Byte] || t == classOf[JavaByte] => json.getAsByte
        case t if t == classOf[Character] || t == classOf[JavaCharacter] => json.getAsCharacter
        case t if t == classOf[Double] || t == classOf[JavaDouble] => json.getAsDouble
        case t if t == classOf[Float] || t == classOf[JavaFloat] => json.getAsFloat
        case t if t == classOf[Int] || t == classOf[JavaInt] => json.getAsInt
        case t if t == classOf[JsonArray] => json.getAsJsonArray
        case t if t == classOf[JsonNull] => json.getAsJsonNull
        case t if t == classOf[JsonObject] => json.getAsJsonObject
        case t if t == classOf[JsonPrimitive] => json.getAsJsonPrimitive
        case t if t == classOf[Long] || t == classOf[JavaLong] => json.getAsLong
        case t if t == classOf[Number]=> json.getAsNumber
        case t if t == classOf[Short] || t == classOf[JavaShort] => json.getAsShort
        case t if t == classOf[String] => json.getAsString
      }

      result.asInstanceOf[T]
    }
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


  /**
    * Converts a java [[Optional]] to a scala [[Option]]. Unwraps and re-wraps in an [[Option]].
    *
    * @param optional [[Optional]] to be converted.
    * @tparam T type of the underlying element.
    * @return an [[Option]] with the same underling element as `optional`.
    */
  implicit def optional2Option[T](optional: Optional[T]): Option[T] = if (optional.isPresent) Option(optional.get) else None



  /**
    * Converts a scala [[Option]] to a java [[Optional]].
    *
    * @param option [[Option]] to be converted.
    * @tparam T type of the underlying element.
    * @return an [[Optional]] with the same underlying element as `option`.
    */
  implicit def option2Optional[T](option: Option[T]): Optional[T] = {
    option match {
      case Some(value) => Optional.of(value)
      case None => Optional.empty[T]
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
      def cleanup(anything: Any): Try[T] = {
        effect
        this.aTry
      }

      this.aTry transform (cleanup, cleanup)
    }
  }
}
