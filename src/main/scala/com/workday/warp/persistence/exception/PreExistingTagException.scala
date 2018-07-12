package com.workday.warp.persistence.exception

import com.workday.warp.common.exception.WarpRuntimeException

/**
  * Represents an error where we attempted to overwrite an "immutable" tag.
  *
  * Created by vignesh.kalidas on 1/9/17.
  */
class PreExistingTagException(message: String) extends WarpRuntimeException(message: String)
