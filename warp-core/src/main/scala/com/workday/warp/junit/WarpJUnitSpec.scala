package com.workday.warp.junit

import com.workday.warp.scalatest.TryMatchers
import org.scalatest.matchers.should.Matchers
import org.scalatest.{Inside, Inspectors, OptionValues}
import org.scalatestplus.junit.{AssertionsForJUnit, JUnitSuite}

/**
  * Base class for WARP framework tests written in JUnit + scalatest.
  *
  * An opinionated starting place.
  *
  * see http://scalatest.org/user_guide/defining_base_classes
  *
  * Created by tomas.mccandless on 6/10/15.
  */
trait WarpJUnitSpec extends JUnitSuite with Matchers
                                       with TryMatchers
                                       with OptionValues
                                       with Inside
                                       with Inspectors
                                       with AssertionsForJUnit
