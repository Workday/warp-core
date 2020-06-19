package com.workday.warp.junit

import java.util

import org.junit.jupiter.api.extension.{Extension, TestTemplateInvocationContext}

import scala.collection.JavaConverters.seqAsJavaList
/**
  * Created by tomas.mccandless on 6/18/20.
  */
trait WarpTestInvocationContextLike extends TestTemplateInvocationContext {

  val currentRepetition: Int
  val totalRepetitions: Int
  val formatter: WarpTestDisplayNameFormatterLike
  val additionalExtensions: Seq[Extension]

  override def getDisplayName(invocationIndex: Int): String = this.formatter.format(this.currentRepetition, this.totalRepetitions)

  override def getAdditionalExtensions: util.List[Extension] = {
    seqAsJavaList(new WarpInfoParameterResolver(this.currentRepetition, this.totalRepetitions) :: additionalExtensions.toList)
  }
}

case class WarpTestInvocationContext(
                                      currentRepetition: Int,
                                      totalRepetitions: Int,
                                      formatter: WarpTestDisplayNameFormatterLike,
                                      additionalExtensions: Seq[Extension] = Seq.empty
                                    ) extends WarpTestInvocationContextLike
