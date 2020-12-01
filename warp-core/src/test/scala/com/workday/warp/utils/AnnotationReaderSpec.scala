package com.workday.warp.utils

import java.util.concurrent.TimeUnit

import com.workday.warp.{Required, TestId}
import Implicits._
import com.workday.warp.TestIdImplicits._
import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import org.junit.jupiter.api.{TestInfo, Timeout}

/**
  * Created by tomas.mccandless on 6/8/18.
  */
class AnnotationReaderSpec extends WarpJUnitSpec {

  /** Checks behavior when annotations are not present. */
  @UnitTest
  def noAnnotations(info: TestInfo): Unit = {
    AnnotationReader.getRequiredMaxValue(TestId.fromString("this.class.does.not.exist")) should be (empty)
    AnnotationReader.getRequiredMaxValue(info) should be (None)
    AnnotationReader.getZScoreRequirement(info) should be (None)
    AnnotationReader.getPercentageDegradationRequirement(info) should be (None)
  }


  /**
    * Checks that we can read the right telemetron thresholds.
    */
  @UnitTest
  @Required(maxResponseTime = 10)
  def required(info: TestInfo): Unit = {
    AnnotationReader.getRequiredMaxValue(info) should be (Some(10 seconds))
  }


  /** Checks that we can read the right junit thresholds. */
  @UnitTest
  @Timeout(value = 10, unit = TimeUnit.SECONDS)
  def timeout(info: TestInfo): Unit = {
    AnnotationReader.getTimeoutValue(info) should be (Some(10 seconds))
  }
}
