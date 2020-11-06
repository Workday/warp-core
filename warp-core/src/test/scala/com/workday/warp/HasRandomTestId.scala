package com.workday.warp

import java.util.UUID

/**
  * Created by tomas.mccandless on 6/16/20.
  */
trait HasRandomTestId {

  def randomTestId(): String = this.getClass.getCanonicalName + "." + UUID.randomUUID().toString
}
