package com.workday.warp.utils

import com.workday.warp.junit.{WarpJUnitSpec, WarpTest}
import com.workday.warp.{Required, TestId}
import com.workday.warp.utils.Implicits.DecoratedRequired
import org.junit.jupiter.api.TestInfo

import java.time.Duration

class DecoratedRequiredSpec extends WarpJUnitSpec {

  @WarpTest
  @Required(maxResponseTime = 16.0)
  def required(info: TestInfo): Unit = {
    val maybeR: Option[Required] = AnnotationReader.getWarpTestAnnotation(classOf[Required], TestId.fromTestInfo(info))
    maybeR.flatMap(_.failedTimeRequirement(Duration.ofSeconds(32))) should not be empty
    maybeR.flatMap(_.failedTimeRequirement(Duration.ofSeconds(32), verifyResponseTime = false)) should be (empty)
  }
}