package com.workday.warp.junit

import org.junit.jupiter.api.extension.{ExtensionContext, ParameterContext, ParameterResolver}

/** Makes information about current test iteration available to a running test.
  *
  * Created by tomas.mccandless on 6/18/20.
  */
trait WarpInfoParameterResolverLike extends ParameterResolver with HasWarpInfo {

  override def supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean = {
    parameterContext.getParameter.getType == classOf[WarpInfo]
  }

  override def resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): WarpInfo = {
    this.warpInfo
  }
}


case class WarpInfoParameterResolver(warpInfo: WarpInfo) extends WarpInfoParameterResolverLike
