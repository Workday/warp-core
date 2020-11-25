package com.workday.warp.utils

import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.time.Duration

import Implicits._
import com.workday.warp.{PercentageDegradationRequirement, Required, TestId, ZScoreRequirement}
import org.junit.jupiter.api.Timeout
import org.junit.platform.commons.util.AnnotationUtils


/**
 * Utilities for reading annotations
 *
 * Created by tomas.mccandless on 8/13/15.
 */
object AnnotationReader extends StackTraceFilter {

  /**
    * Reads the value of the specified annotation from the identified test.
    *
    * @param annotationClass the class of the annotation to read
    * @param testId fully qualified name of the junit test method
    * @tparam T a subtype of Annotation
    * @return an Option containing the annotation annotationClass from the current WARP Junit method
    */
  def getWarpTestAnnotation[T <: Annotation](annotationClass: Class[T], testId: TestId): Option[T] = {
    for {
      m: Method <- testId.maybeTestMethod.toOption
      a: T <- AnnotationUtils.findAnnotation(m, annotationClass).toOption
    } yield a
  }

  /**
    * Reads the max response time from the telemetron [[Required]] annotation.
    *
    * @param testId fully qualified name of the junit test method
    * @return max response time as a [[Duration]] for the test we are about to invoke
    */
  def getRequiredMaxValue(testId: TestId): Option[Duration] = {
    getWarpTestAnnotation(classOf[Required], testId).map { a: Required =>
      Duration.ofNanos(TimeUtils.toNanos(a.maxResponseTime, a.timeUnit))
    }
  }


  /**
    * Reads the max response time from the junit [[Timeout]] annotation.
    *
    * @param testId [[TestId]] for test method.
    * @return max response time as a [[Duration]] for the test we are about to invoke
    */
  def getTimeoutValue(testId: TestId): Option[Duration] = {
    getWarpTestAnnotation(classOf[Timeout], testId).map { a: Timeout =>
      TimeUtils.durationOf(a.value, a.unit)
    }
  }

  /**
    * Looks up [[ZScoreRequirement]] on the annotated test element.
    *
    * @param testId
    * @return
    */
  def getZScoreRequirement(testId: TestId): Option[Double] = {
    getWarpTestAnnotation(classOf[ZScoreRequirement], testId).map(_.percentile)
  }


  /**
    * Reads the percentage threshold requirement from the [[PercentageDegradationRequirement]] annotation for this test.
    *
    * @param testId fully qualified name of the junit test method.
    * @return percentage threshold requirement.
    */
  def getPercentageDegradationRequirement(testId: TestId): Option[Double] = {
    getWarpTestAnnotation(classOf[PercentageDegradationRequirement], testId).map(_.percentage)
  }
}
