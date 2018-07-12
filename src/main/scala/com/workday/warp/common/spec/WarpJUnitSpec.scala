package com.workday.warp.common.spec

import com.workday.telemetron.spec.HasTelemetron
import com.workday.warp.common.utils.TryMatchers
import org.scalatest._
import org.scalatest.junit.{AssertionsForJUnit, JUnitSuite}

/**
  * Base class for WARP framework tests written in scalatest.
  *
  * see http://scalatest.org/user_guide/defining_base_classes
  *
  * Created by tomas.mccandless on 6/10/15.
  */
trait WarpJUnitSpec extends JUnitSuite with HasTelemetron
                                       with Matchers
                                       with TryMatchers
                                       with OptionValues
                                       with Inside
                                       with Inspectors
                                       with AssertionsForJUnit {
  override val shouldVerifyResponseTime: Boolean = false
}
