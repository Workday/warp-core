package com.workday.warp.adapters.gatling

import com.workday.warp.TestId
import com.workday.warp.config.CoreConstants.{ UNDEFINED_TEST_ID => DEFAULT_TEST_ID }

import scala.util.{Success, Try}

/**
  * Created by ruiqi.wang
  */
trait HasDefaultTestName extends HasBasePackageName {

  val testId: TestId
  private val defaultName = s"$packageName.${this.getClass.getSimpleName}"

  /**
    * Gets the fully qualified test name.
   */
  def canonicalName: TestId = {
    val currentTestId: String = this.testId.id
    if (currentTestId == DEFAULT_TEST_ID) new TestId(testId.maybeTestClass, testId.maybeTestMethod) {
      override lazy val maybeId: Try[String] = Success(defaultName)
    }
    else if (currentTestId.startsWith(packageName)) testId
    else new TestId(testId.maybeTestClass, testId.maybeTestMethod) {
      override lazy val maybeId: Try[String] = Success(s"$packageName.$currentTestId")
    }
  }
}
