package com.workday.warp.config

import com.workday.warp.math.{DistributionLike, NullDistribution}

/**
  * Some commonly used constants.
  *
  * Created by tomas.mccandless on 3/29/18.
  */
trait CoreConstants {

  @deprecated("use TestId.undefined", "5.0.0")
  lazy val UNDEFINED_TEST_ID = "com.workday.warp.Undefined.undefined"
  lazy val DISABLE_WARP_USAGE_STRING = "DisableWarpUsage"
  lazy val SMART_THRESHOLD_BASELINE_DATE_STRING = "SmartThresholdBaselineDate"
  lazy val SMART_THRESHOLD_SLIDING_WINDOW_STRING = "SmartThresholdSlidingWindow"
  lazy val SMART_THRESHOLD_TOLERANCE_STRING = "SmartThresholdTolerance"
  lazy val SMART_SCALAR_STRING = "SmartScalarNumber"
  lazy val SMART_THRESHOLD_STRING = "SmartThreshold"
  lazy val USE_DOUBLE_RPCA_STRING = "UseDoubleRpca"
  lazy val WARP_SPECIFICATION_FIELDS_STRING = "WarpSpecificationFields"
  lazy val USE_SMART_THRESHOLD_STRING = "UseSmartThreshold"
  // default distribution with 0 delay between test invocations
  lazy val DISTRIBUTION: DistributionLike = new NullDistribution
}


// can be imported or mixed in
object CoreConstants extends CoreConstants
