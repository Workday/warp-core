package com.workday.warp.adapters.gatling.traits

import com.workday.warp.config.CoreConstants.{UNDEFINED_TEST_ID => DEFAULT_TEST_ID}

/**
  * Created by ruiqi.wang
  */
trait HasDefaultTestName extends HasBasePackageName {

  val testId: String
  private val defaultName = s"$packageName.${this.getClass.getSimpleName}"

  /**
    * Gets the fully qualified test name.
   */
  def canonicalName: String = {
    testId match {
      case DEFAULT_TEST_ID => defaultName
      case s => s"$packageName.$s"
    }
  }
}
