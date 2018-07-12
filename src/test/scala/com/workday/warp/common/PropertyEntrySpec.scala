package com.workday.warp.common

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Created by tomas.mccandless on 6/11/18.
  */
class PropertyEntrySpec extends WarpJUnitSpec {

  @Test
  @Category(Array(classOf[UnitTest]))
  def constructor(): Unit = {
    new PropertyEntry("com.workday.warp.foo").propertyName should be ("com.workday.warp.foo")
    PropertyEntry("com.workday.warp.foo").propertyName should be ("com.workday.warp.foo")
  }
}
