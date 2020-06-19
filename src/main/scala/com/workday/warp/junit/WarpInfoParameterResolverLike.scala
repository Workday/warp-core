package com.workday.warp.junit

import org.junit.jupiter.api.extension.{ExtensionContext, ParameterContext, ParameterResolver}

/**
  * Created by tomas.mccandless on 6/18/20.
  */
trait WarpInfoParameterResolverLike extends ParameterResolver {
  val currentRepetition: Int
  val totalRepetitions: Int

  override def supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean = {
    parameterContext.getParameter.getType == classOf[WarpInfo]
  }

  override def resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): WarpInfo = {
    // TODO possibly store test id here? is warmup?
    DefaultWarpInfo(this.currentRepetition, this.totalRepetitions)
  }
}


case class WarpInfoParameterResolver(currentRepetition: Int, totalRepetitions: Int) extends WarpInfoParameterResolverLike
