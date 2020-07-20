package com.workday.telemetron.math

/**
  * An implementation of a distribution that always returns 0 when sampled. Used to schedule test invocations without any
  * delays between invocations.
  *
  * Created by leslie.lam on 12/18/17.
  * Based on java class created by tomas.mccandless.
  */
case class NullDistribution() extends DistributionLike {

  def this(parameters: Array[Double]) = this

  override def sample: Long = 0
}
