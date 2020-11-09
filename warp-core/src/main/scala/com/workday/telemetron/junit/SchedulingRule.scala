package com.workday.telemetron.junit

import com.workday.telemetron.ResultReporter
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
  * Control structure to create a [[ScheduledStatement]]
  *
  * Created by leslie.lam on 12/15/17.
  * Based on java class created by michael.ottati
  *
  * @deprecated use junit5
  */
@deprecated("use junit5", since = "4.4.0")
class SchedulingRule(resultReporter: ResultReporter, context: TelemetronContext) extends TestRule {
  override def apply(statement: Statement, description: Description): ScheduledStatement =
    ScheduledStatement(statement, description, this.resultReporter, this.context)
}
