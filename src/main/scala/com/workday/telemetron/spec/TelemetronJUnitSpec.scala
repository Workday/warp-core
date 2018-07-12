package com.workday.telemetron.spec

/**
  * Abstract base class extensible from Java for JUnit tests using Telemetron Rules.
  *
  * Created by leslie.lam on 12/19/17.
  */
abstract class TelemetronJUnitSpec(override val shouldVerifyResponseTime: Boolean) extends HasTelemetron {

  // Required default constructor for super classes extended in Java
  def this() = this(shouldVerifyResponseTime = true)
}
