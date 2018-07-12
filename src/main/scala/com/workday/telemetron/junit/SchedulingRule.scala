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
  */
class SchedulingRule(resultReporter: ResultReporter, context: TelemetronContext) extends TestRule {
  override def apply(statement: Statement, description: Description): ScheduledStatement =
    ScheduledStatement(statement, description, this.resultReporter, this.context)
}
