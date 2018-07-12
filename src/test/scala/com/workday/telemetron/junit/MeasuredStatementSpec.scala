package com.workday.telemetron.junit

import java.time.Duration

import com.workday.telemetron.annotation.Measure
import com.workday.telemetron.spec.TelemetronJUnitSpec
import com.workday.warp.common.category.UnitTest
import com.workday.warp.persistence.CorePersistenceAware
import org.junit.experimental.categories.Category
import org.junit.{AfterClass, Test}
import org.scalatest.Matchers

/**
  * Created by leslie.lam on 1/17/2018
  *
  * Checks that Measured Statements persist response times.
  */
class MeasuredStatementSpec extends TelemetronJUnitSpec with Matchers {

  /**
    * A measured statement that reports a duration of 10 seconds.
    */
  @Test
  @Measure
  @Category(Array(classOf[UnitTest]))
  def testPersistence(): Unit = {
    this.telemetron.setResponseTime(Duration.ofSeconds(10))
    this.telemetron.getThreadCPUTime.toMillis should be >= 0L
  }

  /**
    * An unmeasured statement that reports a duration of 5 seconds.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def testNoPersistence(): Unit = {
    this.telemetron.setResponseTime(Duration.ofSeconds(5))
  }
}

object MeasuredStatementSpec extends CorePersistenceAware with Matchers {

  // The fully qualified name of the measured method. Must match exactly.
  val MEASURED_TEST_ID: String = "com.workday.telemetron.junit.MeasuredStatementSpec.testPersistence"

  // The fully qualified name of the unmeasured method. Must match exactly.
  val UNMEASURED_TEST_ID: String = "com.workday.telemetron.junit.MeasuredStatementSpec.testNoPersistence"

  /**
    * Checks that the response time was persisted.
    */
  @AfterClass
  def verifyPersistence(): Unit = {
    val measuredQuery = this.persistenceUtils.readTestExecutionQuery(this.MEASURED_TEST_ID)
    val maybeTestExecution = this.persistenceUtils.synchronously(measuredQuery).headOption
    maybeTestExecution shouldBe defined
    maybeTestExecution.get.responseTime should equal (10)

    val unmeasuredQuery = this.persistenceUtils.readTestExecutionQuery(this.UNMEASURED_TEST_ID)
    this.persistenceUtils.synchronously(unmeasuredQuery).headOption shouldBe None
  }
}
