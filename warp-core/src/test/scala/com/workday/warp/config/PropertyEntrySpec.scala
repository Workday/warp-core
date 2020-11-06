package com.workday.warp.config

import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}

/**
  * Created by tomas.mccandless on 6/11/18.
  */
class PropertyEntrySpec extends WarpJUnitSpec {

  @UnitTest
  def constructor(): Unit = {
    new PropertyEntry("com.workday.warp.foo").propertyName should be ("com.workday.warp.foo")
    PropertyEntry("com.workday.warp.foo").propertyName should be ("com.workday.warp.foo")
    PropertyEntry("com.workday.warp.foo").envVarName should be ("COM_WORKDAY_WARP_FOO")
  }
}
