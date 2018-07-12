package com.workday.warp.common.utils

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Created by tomas.mccandless on 5/13/16.
  */
class StackTraceFilterSpec extends WarpJUnitSpec with StackTraceFilter {

  /** Checks that we can filter stacktrace by class name using the signature defined in the trait. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def stackTraceFilterClassName(): Unit = {
    val exception: RuntimeException = new RuntimeException
    // retain only frames corresponding to this class
    this.filter(exception).getStackTrace should have length 1
  }

  /** Checks that we can filter using a custom predicate using the signature defined in the companion object. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def stackTraceFilterCustomPredicate(): Unit = {
    // define a custom predicate function
    def predicate(element: StackTraceElement): Boolean = element.getClassName startsWith "com.workday.warp"

    val exception: RuntimeException = new RuntimeException
    StackTraceFilter.filter(exception, predicate).getStackTrace should have length 1
  }

  /** Checks that we can filter stacktrace cause. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def stackTraceFilterCause(): Unit = {
    val root: RuntimeException = new RuntimeException("i am the root cause")
    val cause: RuntimeException = new RuntimeException("i am an intermediate cause", root)
    val exception: RuntimeException = new RuntimeException("i was caused by something", cause)
    // retain only frames corresponding to this class
    StackTraceFilter.filter(
      exception,
      (frame: StackTraceElement) => frame.getClassName.startsWith("com.workday.warp")
    ).getStackTrace should have length 1
  }
}
