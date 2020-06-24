package com.workday.warp.junit

import org.junit.jupiter.api.extension.{ExtensionContext, ParameterContext, ParameterResolver}

/** Makes information about current test iteration available to a running test.
  *
  * Created by tomas.mccandless on 6/18/20.
  */
trait WarpInfoParameterResolverLike extends ParameterResolver {
  val currentRepetition: Int
  val totalRepetitions: Int

  override def supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean = {
    parameterContext.getParameter.getType == classOf[HasWarpInfo]
  }

  override def resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): HasWarpInfo = {
    // TODO possibly store test id here? is warmup?
    WarpInfo(this.currentRepetition, this.totalRepetitions)
  }
}


case class WarpInfoParameterResolver(currentRepetition: Int, totalRepetitions: Int) extends WarpInfoParameterResolverLike
