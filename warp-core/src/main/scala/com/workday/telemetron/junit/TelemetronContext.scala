package com.workday.telemetron.junit

/**
  * Holds any additional context/configuration needed for the telemetron rule chain.
  *
  * Created by leslie.lam on 12/15/17.
  * Based on java created by tomas.mccandless on 7/11/16.
  *
  * @deprecated use junit5
  */
@deprecated("use junit5", since = "4.4.0")
case class TelemetronContext(verifyResponseTime: Boolean = true)
