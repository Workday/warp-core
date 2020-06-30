package com.workday.warp.junit

import com.workday.warp.common.spec.WarpJUnitSpec

/**
  * Created by tomas.mccandless on 6/24/20.
  */
class WarpInfoLikeSpec extends WarpJUnitSpec {

  /**
    * An example of requesting test invocation metadata indirectly via [[WarpInfoParameterResolverLike]]
    *
    * @param info info about test iterations.
    */
  @WarpTest(trials = 3)
  def hasInfo(info: WarpInfoLike): Unit = {
    info.totalRepetitions should be (3)
  }
}
