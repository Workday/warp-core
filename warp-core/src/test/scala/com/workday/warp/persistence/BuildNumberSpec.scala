package com.workday.warp.persistence

import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}

import scala.util.Try

/**
  * Created by tomas.mccandless on 1/30/17.
  */
class BuildNumberSpec extends WarpJUnitSpec {

  private val build: String = "2016.5.304"

  /** Checks that we can deconstruct a fully qualified build identifier. */
  @UnitTest
  def buildNumber(): Unit = {
    val expected: BuildNumber = BuildNumber(major = 2016, minor = 5, patch = 304)
    BuildNumber(this.build) should be (expected)

    // we require exactly 3 components
    Try(BuildNumber("2016.5")) should die
    Try(BuildNumber("2016.5.6.7")) should die
  }
}
