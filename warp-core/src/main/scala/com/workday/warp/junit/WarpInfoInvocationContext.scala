package com.workday.warp.junit

import org.junit.jupiter.api.extension.Extension

/**
  * Created by tomas.mccandless on 10/23/20.
  */
trait WarpInfoInvocationContextLike extends WarpTestInvocationContextLike {

  override def getDisplayName(invocationIndex: Int): String = s"$plainDisplayName [$invocationIndex]"
}

case class WarpInfoInvocationContext( plainDisplayName: String,
                                      warpInfo: WarpInfo,
                                      additionalExtensions: List[Extension] = Nil) extends WarpInfoInvocationContextLike
