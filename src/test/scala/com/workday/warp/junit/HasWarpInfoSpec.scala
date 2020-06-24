package com.workday.warp.junit

import com.workday.warp.common.spec.WarpJUnitSpec

/**
  * Created by tomas.mccandless on 6/24/20.
  */
class HasWarpInfoSpec extends WarpJUnitSpec {

  @WarpTest(trials = 3)
  def hasInfo(info: HasWarpInfo): Unit = {
    info.totalRepetitions should be (3)
  }
}
