package com.workday.telemetron.junit

import java.lang.annotation.Annotation

import com.workday.telemetron.ResultReporter
import com.workday.telemetron.annotation.Schedule
import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
  * Created by tomas.mccandless on 6/11/18.
  */
class ScheduledStatementSpec extends WarpJUnitSpec {

  @Schedule(invocations = -1, warmupInvocations = -2, threads = -3)
  def hasInvalidSchedule(): Unit = { }

  /**
    * Checks that invalid settings for warmups, etc, are captured as errors.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def errors(): Unit = {
    val statement: ScheduledStatement = ScheduledStatement(
      statement = new Statement {
        override def evaluate(): Unit = { }
      },
      description = Description.createTestDescription(
        "com.workday.warp.SomeClass",
        "someTest",
        this.getClass.getMethod("hasInvalidSchedule").getAnnotations: _*
      ),
      resultReporter = new ResultReporter(),
      context = TelemetronContext()
    )

    statement.errors should have length 3
  }
}
