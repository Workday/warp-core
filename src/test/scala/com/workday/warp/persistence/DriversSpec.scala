package com.workday.warp.persistence

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Created by tomas.mccandless on 6/11/18.
  */
class DriversSpec extends WarpJUnitSpec {

  @Test
  @Category(Array(classOf[UnitTest]))
  def unsupportedDriver(): Unit = {
    Drivers.unsupportedDriverException("foo").message should startWith ("unsupported persistence driver: foo.")
  }
}
