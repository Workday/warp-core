package com.workday.warp.utils

import com.workday.telemetron.annotation.{Required, Schedule}
import com.workday.warp.common.annotation.ZScoreRequirement
import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.common.utils.Implicits._
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Created by tomas.mccandless on 6/8/18.
  */
class AnnotationReaderSpec extends WarpJUnitSpec {

  class DoesNotHaveRoundRobin

  /**
    * Checks that we read the right defaults when there are no annotations present.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def noAnnotations(): Unit = {
    AnnotationReader.getWarpTestMethodAnnotation(classOf[Required], "this.method.does.not.exist") should be (empty)
    AnnotationReader.getWarpTestMethodAnnotation(classOf[Required], this.getClass.getCanonicalName + ".foo") should be (empty)
    AnnotationReader.getWarpTestClassAnnotation(classOf[Required], "this.class.does.not.exist") should be (empty)
    AnnotationReader.getRequiredMaxValue(this.getTestId) should be (-1 millis)
    AnnotationReader.getScheduleInvocations(this.getTestId) should be (1)
    AnnotationReader.getZScoreRequirement(this.getTestId) should be (ZScoreRequirement.DEFAULT_PERCENTILE)
  }


  /**
    * Checks that we can read the right telemetron thresholds.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  @Required(maxResponseTime = 10)
  def required(): Unit = {
    AnnotationReader.getRequiredMaxValue(this.getTestId) should be (10 seconds)
  }


  /**
    * Checks that we can read the right telemetron schedule.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  @Schedule(invocations = 5)
  def schedule(): Unit = {
    AnnotationReader.getScheduleInvocations(this.getTestId) should be (5)
  }
}
