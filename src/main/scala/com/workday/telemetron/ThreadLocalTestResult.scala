package com.workday.telemetron

import java.time.Duration

import org.junit.runner.Description

/**
  * Thread-local reference to the result of measuring a test.
  *
  * Created by leslie.lam on 12/14/17.
  * Based on java class created by michael.ottati on 9/17/15.
  */
object ThreadLocalTestResult {
  private val initialResult = new TestResult {

    override var status: Status.Value = Status.notStarted

    override val description: Option[Description] = None

    override val sequenceNumber: Int = 0

    override val responseTime: Duration = Duration.ZERO

    override val getElapsedTime: Duration = Duration.ZERO

    override val getThreadCPUTime: Duration = Duration.ZERO

    override val getErrors: List[Throwable] = List.empty
  }

  private val testResult = new ThreadLocal[TestResult]() {
    override protected def initialValue: TestResult = initialResult
  }

  def get: TestResult = testResult.get

  def set(result: TestResult): Unit = testResult.set(result)
}
