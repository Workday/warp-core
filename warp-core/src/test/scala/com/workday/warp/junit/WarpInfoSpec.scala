package com.workday.warp.junit

/**
  * Created by tomas.mccandless on 6/24/20.
  */
class WarpInfoSpec extends WarpJUnitSpec {

  /**
    * An example of requesting test invocation metadata indirectly via [[WarpInfoParameterResolverLike]]
    *
    * @param info info about test iterations.
    */
  @WarpTest(trials = 3)
  def hasInfo(info: WarpInfo): Unit = {
    info.totalRepetitions should be (3)
  }
}
