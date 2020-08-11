package com.workday.telemetron.junit

import com.workday.telemetron.ResultReporter
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.{MultipleFailureException, Statement}
import org.pmw.tinylog.Logger

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * JUnit TestRule that handles reporting of measurements from tests.
  *
  * Created by leslie.lam on 12/14/17
  * Based on java class created by michael.ottati.
  */
class ReportingRule(val resultReporter: ResultReporter) extends TestRule {

  override def apply(base: Statement, description: Description): Statement =
    new Statement() {
      override def evaluate(): Unit = {
        val errors: mutable.ListBuffer[Throwable] = mutable.ListBuffer()

        try {
          base.evaluate()
        }
        catch {
          case error: Throwable =>
            errors += error
        }
        finally {
          ReportingRule.this.finished(description)
        }
        errors ++= ReportingRule.this.resultReporter.getErrors
        MultipleFailureException.assertEmpty(errors.asJava)
      }
    }

  protected def finished(description: Description): Unit = {
    Logger.info("\nResults for: " + description.getClassName + "." + description.getMethodName)
    Logger.info(this.resultReporter)
  }
}
