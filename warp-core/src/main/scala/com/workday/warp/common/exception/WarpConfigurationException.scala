package com.workday.warp.common.exception

/**
  * Represents an error related to configuration. For example, a missing required parameter.
  *
  * Created with IntelliJ IDEA.
  * User: michael.ottati
  * Date: 3/29/13
  * Time: 8:30 PM
  */
class WarpConfigurationException(override val message: String,
                                 override val cause: Throwable) extends WarpRuntimeException(message, cause) {

  def this(message: String) = this(message, None.orNull)
}
