package com.workday.warp.common.exception

import org.pmw.tinylog.Logger

/**
  * Common Warp RuntimeException that all other named runtime exceptions are derived from.
  * In some situations where errors are thrown in the constructor, the whole stack does not get
  * printed in a dump. This class ensures that at least the error message is logged to stderr.
  *
  * Created by michael.ottati on 5/12/15.
  */
class WarpRuntimeException(val message: String, val cause: Throwable) extends RuntimeException(message, cause) {

  WarpRuntimeException.logMessage(this.message)

  def this(message: String) = this(message, None.orNull)
}

object WarpRuntimeException {

  private def logMessage(message: String): Unit = Logger.error(message)
}
