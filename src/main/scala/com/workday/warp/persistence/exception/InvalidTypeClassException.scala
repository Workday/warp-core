package com.workday.warp.persistence.exception

/**
  * Represents an error where an unexpected instance of a typeclass is provided.
  *
  * See [[com.workday.warp.persistence.Tables.RowTypeClasses]]
  *
  * Created by leslie.lam on 2/8/18.
  */
class InvalidTypeClassException(message: String, cause: Throwable) extends IllegalArgumentException(message, cause) {
  def this(message: String) = this(message, None.orNull)
}
