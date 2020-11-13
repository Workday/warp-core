package com.workday.warp.arbiters

/**
  * An exception thrown to indicate that a requirement is not satisfied.
  *
  * Created by leslie.lam on 12/14/17.
  * Based on java class created by michael.ottati on 9/18/15.
  */
class RequirementViolationException(message: String = None.orNull,
                                    cause: Throwable = None.orNull,
                                    enableSuppression: Boolean = false,
                                    writableStackTrace: Boolean = false)
  extends RuntimeException(message, cause, enableSuppression, writableStackTrace)
