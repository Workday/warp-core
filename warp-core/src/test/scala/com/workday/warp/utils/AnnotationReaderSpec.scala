package com.workday.warp.utils

import java.util.concurrent.TimeUnit

import com.workday.telemetron.annotation.{Required, Schedule}
import com.workday.warp.common.annotation.ZScoreRequirement
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.common.utils.Implicits._
import com.workday.warp.TestIdImplicits._
import com.workday.warp.junit.UnitTest
import org.junit.jupiter.api.{TestInfo, Timeout}

/**
  * Created by tomas.mccandless on 6/8/18.
  */
class AnnotationReaderSpec extends WarpJUnitSpec {

  class DoesNotHaveRoundRobin

  /**
    * Checks that we read the right defaults when there are no annotations present.
    */
  @UnitTest
  def noAnnotations(info: TestInfo): Unit = {
    AnnotationReader.getWarpTestMethodAnnotation(classOf[Required], "this.method.does.not.exist") should be (empty)
    AnnotationReader.getWarpTestMethodAnnotation(classOf[Required], this.getClass.getCanonicalName + ".foo") should be (empty)
    AnnotationReader.getWarpTestClassAnnotation(classOf[Required], "this.class.does.not.exist") should be (empty)
    AnnotationReader.getRequiredMaxValue(info.testId) should be (-1 millis)
    AnnotationReader.getScheduleInvocations(info.testId) should be (1)
    AnnotationReader.getZScoreRequirement(info.testId) should be (ZScoreRequirement.DEFAULT_PERCENTILE)
  }


  /**
    * Checks that we can read the right telemetron thresholds.
    */
  @UnitTest
  @Required(maxResponseTime = 10)
  def required(info: TestInfo): Unit = {
    AnnotationReader.getRequiredMaxValue(info.testId) should be (10 seconds)
  }


  /** Checks that we can read the right junit thresholds. */
  @UnitTest
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  def timeout(info: TestInfo): Unit = {
    AnnotationReader.getTimeoutValue(info.testId) should be (10 seconds)
  }


  /**
    * Checks that we can read the right telemetron schedule.
    */
  @UnitTest
  @Schedule(invocations = 5)
  def schedule(info: TestInfo): Unit = {
    AnnotationReader.getScheduleInvocations(info.testId) should be (5)
  }
}
