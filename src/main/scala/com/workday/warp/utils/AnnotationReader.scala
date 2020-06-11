package com.workday.warp.utils

import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.time.Duration

import com.workday.telemetron.annotation.{Required, Schedule}
import com.workday.telemetron.utils.TimeUtils
import com.workday.warp.common.annotation.{PercentageDegradationRequirement, ZScoreRequirement}
import com.workday.warp.common.utils.StackTraceFilter
import com.workday.warp.junit.RoundRobinExecution

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
  protected def getWarpTestMethod(testId: String): Option[Method] = {
    val methodName: String = testId drop testId.lastIndexOf('.') + 1
    // we expect the reflected method to have 0 parameters
    val parameterTypes: Array[Class[_]] = Array.empty
    this.getWarpTestClass(testId) flatMap { clazz => Try(Option(clazz.getMethod(methodName, parameterTypes: _*))).toOption.flatten }
  }



  /**
   * Reads the value of the annotation annotationClass from the currently executing WARP Junit class.
   *
   * @param annotationClass the class of the annotation to read
   * @param testId fully qualified name of the junit test method
   * @tparam T a subtype of Annotation
   * @return an Option containing the annotation annotationClass from the current WARP Junit class
   */
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
  def getWarpTestMethodAnnotation[T <: Annotation](annotationClass: Class[T], testId: String): Option[T] = {
    this.getWarpTestMethod(testId) flatMap { method => Try(Option(method.getAnnotation(annotationClass))).toOption.flatten }
  }



  /**
   * Reads the max response time from the telemetron [[Required]] annotation.
   *
   * @param testId fully qualified name of the junit test method
   * @return max response time as a [[Duration]] for the test we are about to invoke
   */
  def getRequiredMaxValue(testId: String): Duration = {
    this.getWarpTestMethodAnnotation(classOf[Required], testId)
      .map(req => Duration.ofNanos(TimeUtils.toNanos(req.maxResponseTime, req.timeUnit)))
      .getOrElse(Duration.ofMillis(-1))
  }



  /**
   * Reads the @RoundRobinExecution annotation based on testId
   *
   * @param testId fully qualified name of the junit test method
   * @return number of parent scenario class invocations. Default value = 1 if no @RoundRobinExecution
   *         annotation set.
   */
  def getRoundRobinExecution(testId: String): Int = {
    this.getWarpTestClassAnnotation(classOf[RoundRobinExecution], testId)
      .map(_.invocations)
      .getOrElse(RoundRobinExecution.DEFAULT_INVOCATIONS)
  }



  /**
   * Reads the @RoundRobinExecution annotation on aClass.
   *
   * @param aClass Class to read the @RoundRobinExecution annotation from.
   * @return number of parent scenario class invocations. Default value = 1 if no @RoundRobinExecution
   *         annotation set.
   */
  def getRoundRobinExecution(aClass: Class[_]): Int = {
    Try(aClass.getAnnotation(classOf[RoundRobinExecution]))
      .map(_.invocations)
      .getOrElse(RoundRobinExecution.DEFAULT_INVOCATIONS)
  }



  /**
   * Reads the invocations value from the [[Schedule]] annotation.
   *
   * @param testId fully qualified name of the junit test method
   * @return number of invocations set by the [[Schedule]] annotation.
   */
  def getScheduleInvocations(testId: String): Int = {
    this.getWarpTestMethodAnnotation(classOf[Schedule], testId)
      .map(_.invocations)
      .getOrElse(Schedule.INVOCATIONS_DEFAULT)
  }



  /**
    * Reads the percentile (z-score) threshold requirement from the [[ZScoreRequirement]] annotation for this test.
    *
    * Truncates the value to be within 0.0 and 100.0
    *
    * @param testId fully qualified name of the junit test method
    * @return z-score percentile threshold requirement
    */
  def getZScoreRequirement(testId: String): Double = {
    this.getWarpTestMethodAnnotation(classOf[ZScoreRequirement], testId)
      .map(req => math.max(0.0, math.min(100.0, req.percentile)))
      .getOrElse(ZScoreRequirement.DEFAULT_PERCENTILE)
  }



  /**
    * Reads the [[ZScoreRequirement]] annotation, returns true iff such an annotation exists.
    *
    * @param testId fully qualified name of the junit test method.
    * @return true iff the measured test is annotated with [[ZScoreRequirement]].
    */
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
  def getPercentageDegradationRequirement(testId: String): Double = {
    this.getWarpTestMethodAnnotation(classOf[PercentageDegradationRequirement], testId)
      .map(req => math.max(0.0, math.min(100.0, req.percentage)))
      .getOrElse(PercentageDegradationRequirement.DEFAULT_PERCENTAGE)
  }



  /**
    * Reads the [[PercentageDegradationRequirement]] annotation, returns true iff such an annotation exists.
    *
    * @param testId fully qualified name of the junit test method.
    * @return true iff the measured test is annotated with [[PercentageDegradationRequirement]].
    */
  def hasPercentageDegradationRequirement(testId: String): Boolean = {
    this.getWarpTestMethodAnnotation(classOf[PercentageDegradationRequirement], testId).isDefined
  }
}
