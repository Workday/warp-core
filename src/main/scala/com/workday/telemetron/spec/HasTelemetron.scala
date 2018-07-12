package com.workday.telemetron.spec

import com.workday.telemetron.junit.{TelemetronContext, TelemetronRule}
import org.junit.Rule

/**
  * Base trait for tests that use Telemetron. Adds a telemetron rule with response time verification.
  * Extend this trait to use annotations such as [[com.workday.telemetron.annotation.Schedule]].
  *
  * Created by leslie.lam on 12/19/17.
  */
trait HasTelemetron {
  def shouldVerifyResponseTime: Boolean = true
  private[this] val _telemetron: TelemetronRule = TelemetronRule(context = TelemetronContext(this.shouldVerifyResponseTime))

  @Rule
  def telemetron: TelemetronRule = this._telemetron

  /**
    * @return fully qualified name of the executing junit method.
    */
  def getTestId: String = this.telemetron.getTestName
}
