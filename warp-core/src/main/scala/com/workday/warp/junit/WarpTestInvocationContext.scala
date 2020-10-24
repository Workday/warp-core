package com.workday.warp.junit

import java.util

import org.junit.jupiter.api.extension.{Extension, TestTemplateInvocationContext}

import scala.collection.JavaConversions._

/**
  * JUnit invocation context for a single test invocation.
  *
  * Created by tomas.mccandless on 6/18/20.
  */
trait WarpTestInvocationContextLike extends TestTemplateInvocationContext with HasWarpInfo {

  // plain vanilla method display name
  def plainDisplayName: String
  def additionalExtensions: List[Extension]


  /**
   * Formats display name including current repetition info.
   *
   * @param invocationIndex unused.
   * @return A test invocation display name including current repetition info.
   */
  override def getDisplayName(invocationIndex: Int): String = {
    s"$plainDisplayName [${warpInfo.repetitionType.name} ${warpInfo.currentRepetition} of ${warpInfo.currentRepLimit}]"
  }

  /**
    * Gets additional JUnit extensions for this invocation.
    *
    * We always use a [[WarpInfoParameterResolver]], and we use a [[MeasurementExtension]] for measured trials only.
    *
    * @return additional JUnit extensions for this test invocation.
    */
  override def getAdditionalExtensions: util.List[Extension] = {
    WarpInfoParameterResolver(this.warpInfo) :: additionalExtensions
  }
}

case class WarpTestInvocationContext(plainDisplayName: String,
                                     warpInfo: WarpInfo,
                                     additionalExtensions: List[Extension] = List.empty) extends WarpTestInvocationContextLike
