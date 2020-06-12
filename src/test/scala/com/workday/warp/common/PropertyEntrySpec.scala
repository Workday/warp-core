package com.workday.warp.common

import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.junit.UnitTest

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
