package com.workday.warp.utils

import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.time.Duration

import Implicits._
import com.workday.warp.{PercentageDegradationRequirement, Required, TestId, ZScoreRequirement}
import org.junit.jupiter.api.Timeout
import org.junit.platform.commons.util.AnnotationUtils
import org.pmw.tinylog.Logger

import scala.util.Try


/**
 * Utilities for reading annotations
 *
 * Created by tomas.mccandless on 8/13/15.
 */
object AnnotationReader extends StackTraceFilter {

  /**
   * @param testId fully qualified name of the junit test method
   * @return an Option containing the WARP JUnit Class referred to by testId
   */
  @deprecated("use TestId and junit5", "4.4.0")
  protected def getWarpTestClass(testId: String): Option[Class[_]] = {
    // remove the method name to obtain the fully qualified class name
    val className: String = testId take testId.lastIndexOf('.')

    // return None if we can't locate the class
    // we have this double monadic nesting to avoid returning a Success(null)
    Try(Option(Class.forName(className))).toOption.flatten
  }



  /**
   * @param testId fully qualified name of the junit test method
   * @return an Option containing the Method referred to by testId
   */
    // TODO not sure if this will work correctly wrt method overloading, its possible that we will have
    // multiple methods with the same name and we cant disambiguate at the level of method name
  @deprecated("use TestId and junit5", "4.4.0")
  protected def getWarpTestMethod(testId: String): Option[Method] = {
    val methodName: String = testId drop testId.lastIndexOf('.') + 1
    // TODO won't work correctly wrt method overloading, its possible that we will have
    // multiple junit test methods with the same name and we can't disambiguate
    this.getWarpTestClass(testId).flatMap { cls =>
      val methods: Seq[Method] = cls.getMethods.filter(_.getName == methodName)
      if (methods.length > 1) {
        Logger.warn(s"detected overloaded methods for signature $testId, annotation processing may not work as expected.")
      }
      methods.headOption
    }
  }



  /**
   * Reads the value of the annotation annotationClass from the currently executing WARP Junit class.
   *
   * @param annotationClass the class of the annotation to read
   * @param testId fully qualified name of the junit test method
   * @tparam T a subtype of Annotation
   * @return an Option containing the annotation annotationClass from the current WARP Junit class
   */
  @deprecated("use TestId and junit5", "4.4.0")
  def getWarpTestClassAnnotation[T <: Annotation](annotationClass: Class[T], testId: String): Option[T] = {
    this.getWarpTestClass(testId) flatMap { clazz => Try(Option(clazz.getAnnotation(annotationClass))).toOption.flatten }
  }



  /**
   * Reads the value of the annotation annotationClass from the currently executing WARP Junit method.
   *
   * @param annotationClass the class of the annotation to read
   * @param testId fully qualified name of the junit test method
   * @tparam T a subtype of Annotation
   * @return an Option containing the annotation annotationClass from the current WARP Junit method
   */
  @deprecated("use TestId and junit5", "4.4.0")
  def getWarpTestMethodAnnotation[T <: Annotation](annotationClass: Class[T], testId: String): Option[T] = {
    this.getWarpTestMethod(testId) flatMap { method => Try(Option(method.getAnnotation(annotationClass))).toOption.flatten }
  }



  /**
   * Reads the max response time from the telemetron [[Required]] annotation.
   *
   * @param testId fully qualified name of the junit test method
   * @return max response time as a [[Duration]] for the test we are about to invoke
   */
  @deprecated("use TestId and junit5", "4.4.0")
  def getRequiredMaxValue(testId: String): Duration = {
    this.getWarpTestMethodAnnotation(classOf[Required], testId)
      .map(req => Duration.ofNanos(TimeUtils.toNanos(req.maxResponseTime, req.timeUnit)))
      .getOrElse(Duration.ofMillis(-1))
  }


  def getRequiredMaxValue(testId: TestId): Option[Duration] = {
    for {
      m <- testId.maybeTestMethod.toOption
      a <- AnnotationUtils.findAnnotation(m, classOf[Required]).toOption
    } yield Duration.ofNanos(TimeUtils.toNanos(a.maxResponseTime, a.timeUnit))
  }



  /**
    * Reads the max response time from the junit [[Timeout]] annotation.
    *
    * @param testId fully qualified name of the junit test method
    * @return max response time as a [[Duration]] for the test we are about to invoke
    */
  @deprecated("use getTimeoutValue(TestId) instead", since = "5.0.0")
  def getTimeoutValue(testId: String): Duration = {
    this.getWarpTestMethodAnnotation(classOf[Timeout], testId)
      .map(timeout => Duration.ofNanos(TimeUtils.toNanos(timeout.value, timeout.unit)))
      .getOrElse(Duration.ofMillis(-1))
  }

  /**
    * Reads the max response time from the junit [[Timeout]] annotation.
    *
    * @param testId [[TestId]] for test method.
    * @return max response time as a [[Duration]] for the test we are about to invoke
    */
  def getTimeoutValue(testId: TestId): Option[Duration] = {
    for {
      m <- testId.maybeTestMethod.toOption
      a <- AnnotationUtils.findAnnotation(m, classOf[Timeout]).toOption
    } yield TimeUtils.durationOf(a.value, a.unit)
  }

  /**
    * Reads the percentile (z-score) threshold requirement from the [[ZScoreRequirement]] annotation for this test.
    *
    * Truncates the value to be within 0.0 and 100.0
    *
    * @param testId fully qualified name of the junit test method
    * @return z-score percentile threshold requirement
    */
  @deprecated("use TestId and junit5", "4.4.0")
  def getZScoreRequirement(testId: String): Double = {
    this.getWarpTestMethodAnnotation(classOf[ZScoreRequirement], testId)
      .map(req => math.max(0.0, math.min(100.0, req.percentile)))
      .getOrElse(ZScoreRequirement.DEFAULT_PERCENTILE)
  }


  /**
    * Looks up [[ZScoreRequirement]] on the annotated test element.
    *
    * @param testId
    * @return
    */
  def getZScoreRequirement(testId: TestId): Option[Double] = {
    for {
      m <- testId.maybeTestMethod.toOption
      a <- AnnotationUtils.findAnnotation(m, classOf[ZScoreRequirement]).toOption
    } yield math.max(0.0, math.min(100.0, a.percentile))
  }



  /**
    * Reads the [[ZScoreRequirement]] annotation, returns true iff such an annotation exists.
    *
    * @param testId fully qualified name of the junit test method.
    * @return true iff the measured test is annotated with [[ZScoreRequirement]].
    */
  @deprecated("use TestId and junit5", "4.4.0")
  def hasZScoreRequirement(testId: String): Boolean = {
    this.getWarpTestMethodAnnotation(classOf[ZScoreRequirement], testId).isDefined
  }



  /**
    * Reads the percentage threshold requirement from the [[PercentageDegradationRequirement]] annotation for this test.
    *
    * Truncates the value to be within 0.0 and 100.0.
    *
    * @param testId fully qualified name of the junit test method.
    * @return percentage threshold requirement.
    */
  @deprecated("use TestId and junit5", "4.4.0")
  def getPercentageDegradationRequirement(testId: String): Double = {
    this.getWarpTestMethodAnnotation(classOf[PercentageDegradationRequirement], testId)
      .map(req => math.max(0.0, math.min(100.0, req.percentage)))
      .getOrElse(PercentageDegradationRequirement.DEFAULT_PERCENTAGE)
  }


  def getPercentageDegradationRequirement(testId: TestId): Option[Double] = {
    for {
      m <- testId.maybeTestMethod.toOption
      a <- AnnotationUtils.findAnnotation(m, classOf[PercentageDegradationRequirement]).toOption
    } yield math.max(0.0, math.min(100.0, a.percentage))
  }


  /**
    * Reads the [[PercentageDegradationRequirement]] annotation, returns true iff such an annotation exists.
    *
    * @param testId fully qualified name of the junit test method.
    * @return true iff the measured test is annotated with [[PercentageDegradationRequirement]].
    */
  @deprecated("use TestId and junit5", "4.4.0")
  def hasPercentageDegradationRequirement(testId: String): Boolean = {
    this.getWarpTestMethodAnnotation(classOf[PercentageDegradationRequirement], testId).isDefined
  }
}
