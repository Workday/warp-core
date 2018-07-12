package com.workday.warp.adapters

import com.workday.warp.TrialResult
import com.workday.warp.dsl.using
import com.workday.warp.persistence.Tag
import scala.reflect.runtime.universe._

import scala.util.Try

/**
  * Defines a test that can be invoked and measured.
  *
  * Created by tomas.mccandless on 1/6/17.
  */
abstract class Adapter[T: TypeTag](val testId: String, val tags: List[Tag]) {

  /** @return a [[scala.util.Success]] containing a [[TrialResult]], or a wrapped exception. */
  def invoke(): Try[TrialResult[T]]

  /** @return a [[TrialResult]] containing the result of measuring this test. */
  def measure(): TrialResult[T] = (using testId this.testId tags this.tags measure { this.invoke() }).head
}
