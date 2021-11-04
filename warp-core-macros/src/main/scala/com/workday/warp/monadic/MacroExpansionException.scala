package com.workday.warp.monadic

/**
  * An Exception indicating something went wrong during macro expansion.
  *
  * @param message the detail message.
  * @param cause the underlying cause of the error. Might be null.
  */
class MacroExpansionException(message: String, cause: Throwable = None.orNull) extends RuntimeException(message, cause)