package com.workday.telemetron.junit

import org.junit.rules.{TestName, TestRule}
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
  * JUnit rule exposing a way to read the fully qualified method name of the running test.
  *
  * Created by leslie.lam on 12/15/17.
  * Based on java class created by tomas.mccandless on 5/19/16.
  */
class TelemetronNameRule extends TestRule {

  private val testName: TestName = new TestName
  private var maybeDescription: Option[Description] = None

  override def apply(base: Statement, description: Description): Statement = {
    this.maybeDescription = Some(description)
    this.testName.apply(base, description)
  }

  /**
    * Returns the fully qualified method name of the running JUnit test.
    *
    * @return fully qualified method name
    * @throws java.lang.IllegalStateException if maybeDescription is empty
    */
  def getTestName: String = this.maybeDescription match {
    case Some(desc: Description) => s"${desc.getClassName}.${this.testName.getMethodName}"
    case _ => throw new IllegalStateException("Rule has not yet been applied")
  }
}
