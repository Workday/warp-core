package com.workday.warp.utils

import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.time.Duration

import com.workday.telemetron.annotation.{Required, Schedule}
import com.workday.telemetron.utils.TimeUtils
import com.workday.warp.common.annotation.{PercentageDegradationRequirement, ZScoreRequirement}
import com.workday.warp.common.utils.StackTraceFilter
import com.workday.warp.junit.RoundRobinExecution

/**
 * Utilities for reading annotations
 *
 * Created by tomas.mccandless on 8/13/15.
 */
object AnnotationReader extends StackTraceFilter {

  /**
   * @param testId fully qualified name of the junit test method
   * @return an Option containing the WARP JUnit Class refered to by testId
   */
  protected def getWarpTestClass(testId: String): Option[Class[_]] = {
    // remove the method name to obtain the fully qualified class name
    val className: String = testId take testId.lastIndexOf('.')

    try {
      Option(Class.forName(className))
    }
    catch {
      // return None if we can't locate the class
      case e: ClassNotFoundException =>
        None
    }
  }



  /**
   * @param testId fully qualified name of the junit test method
   * @return an Option containing the Method referred to by testId
   */
  protected def getWarpTestMethod(testId: String): Option[Method] = {
    val methodName: String = testId drop testId.lastIndexOf('.') + 1

    // get an Option containing the Class based on the current value of testID
    getWarpTestClass(testId) flatMap { clazz =>
      try {
        Option(clazz.getMethod(methodName, new Array[Class[_]](0): _*))
      }
      catch {
        // return None if we can't locate the method
        case e: NoSuchMethodException =>
          None
      }
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
  def getWarpTestClassAnnotation[T <: Annotation](annotationClass: Class[T], testId: String): Option[T] = {
    this.getWarpTestClass(testId) flatMap { clazz => Option(clazz.getAnnotation(annotationClass)) }
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
    this.getWarpTestMethod(testId) flatMap { method => Option(method.getAnnotation(annotationClass)) }
  }



  /**
   * Reads the max response time from the telemetron [[Required]] annotation.
   *
   * @param testId fully qualified name of the junit test method
   * @return max response time as a [[Duration]] for the test we are about to invoke
   */
  // TODO maybe return an Option?
  def getRequiredMaxValue(testId: String): Duration = {
    getWarpTestMethodAnnotation(classOf[Required], testId) match {
      case None => Duration.ofMillis(-1)
      case Some(required) => Duration.ofNanos(TimeUtils.toNanos(required.maxResponseTime, required.timeUnit))
    }
  }



  /**
   * Reads the @RoundRobinExecution annotation based on testId
   *
   * @param testId fully qualified name of the junit test method
   * @return number of parent scenario class invocations. Default value = 1 if no @RoundRobinExecution
   *         annotation set.
   */
  def getRoundRobinExecution(testId: String): Int = {
    getWarpTestClassAnnotation(classOf[RoundRobinExecution], testId) match {
      case None => RoundRobinExecution.DEFAULT_INVOCATIONS
      case Some(roundRobinExecution) => roundRobinExecution.invocations
    }
  }



  /**
   * Reads the @RoundRobinExecution annotation on aClass.
   *
   * @param aClass Class to read the @RoundRobinExecution annotation from.
   * @return number of parent scenario class invocations. Default value = 1 if no @RoundRobinExecution
   *         annotation set.
   */
  def getRoundRobinExecution(aClass: Class[_]): Int = {
    Option(aClass.getAnnotation(classOf[RoundRobinExecution])) match {
      case None => RoundRobinExecution.DEFAULT_INVOCATIONS
      case Some(roundRobinExecution) => roundRobinExecution.invocations
    }
  }



  /**
   * Reads the invocations value from the [[Schedule]] annotation.
   *
   * @param testId fully qualified name of the junit test method
   * @return number of invocations set by the [[Schedule]] annotation.
   */
  def getScheduleInvocations(testId: String): Int = {
    getWarpTestMethodAnnotation(classOf[Schedule], testId) match {
      case None => 1
      case Some(schedule) => schedule.invocations
    }
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
    getWarpTestMethodAnnotation(classOf[ZScoreRequirement], testId) match {
      case None => ZScoreRequirement.DEFAULT_PERCENTILE
      case Some(zScoreRequirement) => math.max(0.0, math.min(100.0, zScoreRequirement.percentile))
    }
  }



  /**
    * Reads the [[ZScoreRequirement]] annotation, returns true iff such an annotation exists.
    *
    * @param testId fully qualified name of the junit test method.
    * @return true iff the measured test is annotated with [[ZScoreRequirement]].
    */
  def hasZScoreRequirement(testId: String): Boolean = {
    getWarpTestMethodAnnotation(classOf[ZScoreRequirement], testId).isDefined
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
    getWarpTestMethodAnnotation(classOf[PercentageDegradationRequirement], testId) match {
      case None => PercentageDegradationRequirement.DEFAULT_PERCENTAGE
      case Some(percentageRequirement) => math.max(0.0, math.min(100.0, percentageRequirement.percentage))
    }
  }



  /**
    * Reads the [[PercentageDegradationRequirement]] annotation, returns true iff such an annotation exists.
    *
    * @param testId fully qualified name of the junit test method.
    * @return true iff the measured test is annotated with [[PercentageDegradationRequirement]].
    */
  def hasPercentageDegradationRequirement(testId: String): Boolean = {
    getWarpTestMethodAnnotation(classOf[PercentageDegradationRequirement], testId).isDefined
  }
}
