package com.workday.telemetron.spec

import org.scalatest.matchers.should.Matchers

/**
  * Abstract base class extensible from Java for JUnit tests using Telemetron Rules.
  *
  * Created by leslie.lam on 12/19/17.
  */
abstract class TelemetronJUnitSpec(override val shouldVerifyResponseTime: Boolean) extends HasTelemetron with Matchers {

  // Required default constructor for super classes extended in Java
  def this() = this(shouldVerifyResponseTime = true)
}
