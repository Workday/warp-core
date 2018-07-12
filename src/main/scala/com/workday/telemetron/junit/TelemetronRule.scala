package com.workday.telemetron.junit

import java.time.Duration

import com.workday.telemetron.{ResultReporter, ThreadLocalTestResult}
import org.junit.rules.{ExpectedException, RuleChain, TestRule}
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
  * Created by leslie.lam on 12/14/17
  * Based on java class created by michael.ottati on 3/29/16.
  */
class TelemetronRule(context: TelemetronContext,
                     reportingRules: List[TestRule],
                     measurementCollectorRules: List[TestRule]) extends TestRule {

  private val nameRule: TelemetronNameRule = new TelemetronNameRule
  private val thrown: ExpectedException = ExpectedException.none()

  private var chain: RuleChain = RuleChain.outerRule(thrown)
  chain = chain.around(nameRule)

  reportingRules.foreach(tr => chain = chain.around(tr))
  val resultReporter = new ResultReporter
  chain = chain.around(new ReportingRule(resultReporter))

  measurementCollectorRules.foreach(tr => chain = chain.around(tr))
  chain = chain.around(new SurroundOnceRule)
  chain = chain.around(new SchedulingRule(resultReporter, context))

  override def apply(base: Statement, description: Description): Statement = this.chain.apply(base, description)


  /**
    * Returns thread CPU time as measured by [[java.lang.management.ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime]]
    *
    * @return thread CPU time.
    */
  def getThreadCPUTime: Duration = ThreadLocalTestResult.get.getThreadCPUTime

  /**
    * If {@link #setResponseTime(Duration)} has already been called, the set value will be returned. If
    * response time has not been explicitly been set, the value of {@link #getElapsedTime()} will be returned.
    *
    * @return the response time of the test.
    */
  def getResponseTime: Duration = ThreadLocalTestResult.get.responseTime

  /**
    * Sets the responseTime that will be reported for a test. This method may only be called once per
    * test invocation. Once the response time has been set, it is thereafter immutable. The intended
    * use case for this method is to allow test methods that can obtain a better representation of
    * the test response time than would be represented by wall clock time, to set their own response
    * time.
    *
    * @param duration A Duration representing response time.
    */
  def setResponseTime(duration: Duration): Unit = ThreadLocalTestResult.get.responseTime = duration

  /**
    * Returns the elapsed wall clock time of the test. This value will change during the execution of the test
    * and be immutably fixed upon test completion.
    *
    * @return elapsed (wall clock) time.
    */
  def getElapsedTime: Duration = ThreadLocalTestResult.get.getElapsedTime

  /**
    * Returns the fully qualified test name of the running JUnit test.
    *
    * @return fully qualified method name
    * @throws java.lang.IllegalStateException if description is null
    */
  def getTestName: String = this.nameRule.getTestName

  /**
    * Returns an ExpectedException object representing the exception that was thrown during the test.
    * This method was primarily implemented for unit test purposes within the framework.
    *
    * {{{
    *     @Test
    *     @Requirement (maxResponseTime = 10, timeUnit = TimeUnit.MILLISECONDS)
    *     public void exceedsTest() {
    *        final ExpectedException thrown = telemetron.getThrown();
    *        thrown.expect(RequirementViolationException.class);
    *        thrown.expectMessage("Response time requirement exceeded, specified: PT0.05S");
    *        try {
    *            Thread.sleep(11);
    *        } catch (InterruptedException e) {
    *            Thread.currentThread().interrupt();
    *        }
    *     }
    * }}}
    *
    * @return thrown excption or ExpectedException.none() if no exception was thrown during the test.
    */
  def getThrown: ExpectedException = this.thrown
}

object TelemetronRule {
  def apply(context: TelemetronContext = TelemetronContext(),
            reportingRules: List[TestRule] = List.empty,
            measurementCollectorRules: List[TestRule] = List.empty): TelemetronRule =
    new TelemetronRule(context, reportingRules, measurementCollectorRules)
}
