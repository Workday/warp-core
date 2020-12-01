package com.workday.warp.persistence

import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}

/**
  * Created by tomas.mccandless on 6/11/18.
  */
class DriversSpec extends WarpJUnitSpec {

  @UnitTest
  def unsupportedDriver(): Unit = {
    Drivers.unsupportedDriverException("foo").message should startWith ("unsupported persistence driver: foo.")
  }
}
