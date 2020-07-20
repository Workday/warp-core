package com.workday.telemetron.math

/**
  * Trait representing a statistical distribution.
  *
  * Created by leslie.lam on 12/18/17.
  * Based on abstract java class created by tomas.mccandless
  */
trait DistributionLike {

  /**
    * @return a long sampled from the underlying distribution.
    */
  def sample: Long

  /**
    * @return a long sampled from the underlying distribution, or 0 if that value is negative.
    */
  def truncatedSample: Long = Math.max(0, this.sample)
}
