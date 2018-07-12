package com.workday.warp.persistence.exception

import com.workday.warp.common.exception.WarpRuntimeException

/**
  * Created by justin.teo on 12/19/17.
  */
class WarpFieldPersistenceException(message: String, cause: Throwable) extends WarpRuntimeException(message, cause) {
  def this(message: String) = this(message, None.orNull)
}
