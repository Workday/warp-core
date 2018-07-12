package com.workday.warp.persistence

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.Test
import org.junit.experimental.categories.Category

import scala.util.Try

/**
  * Created by tomas.mccandless on 1/30/17.
  */
class BuildNumberSpec extends WarpJUnitSpec {

  private val build: String = "2016.5.304"

  /** Checks that we can deconstruct a fully qualified build identifier. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def buildNumber(): Unit = {
    val expected: BuildNumber = BuildNumber(major = 2016, minor = 5, patch = 304)
    BuildNumber(this.build) should be (expected)

    // we require exactly 3 components
    Try(BuildNumber("2016.5")) should die
    Try(BuildNumber("2016.5.6.7")) should die
  }
}
