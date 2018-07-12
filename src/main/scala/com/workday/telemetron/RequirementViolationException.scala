package com.workday.telemetron

/**
  * An exception thrown to indicate that a requirement is not satisfied.
  *
  * Created by leslie.lam on 12/14/17.
  * Based on java class created by michael.ottati on 9/18/15.
  */
// scalastyle:off null
class RequirementViolationException(message: String = null,
                                    cause: Throwable = null,
                                    enableSuppression: Boolean = false,
                                    writableStackTrace: Boolean = false)
  extends RuntimeException(message, cause, enableSuppression, writableStackTrace)
// scalastyle:on null
