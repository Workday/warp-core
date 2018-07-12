package com.workday.warp.common

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.exception.WarpConfigurationException
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Created by tomas.mccandless on 3/21/18.
  */
class WarpPropertyManagerSpec extends WarpJUnitSpec {

  @Test
  @Category(Array(classOf[UnitTest]))
  def dbUrl(): Unit = {
    CoreWarpProperty.WARP_DATABASE_URL.value should not be empty
  }


  /**
    * Checks behavior for non-existing properties.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def requiredProperty(): Unit = {
    // when a non-existing property is not required, should return null
    WarpPropertyManager.valueOf("com.workday.warp.foo", required = false) should be (None.orNull)

    // should throw an exception when a non-existing property is required
    val entry: PropertyEntry = PropertyEntry("com.workday.warp.foo", isRequired = true)
    intercept[WarpConfigurationException] {
      WarpPropertyManager.valueOf(entry)
    }
  }
}
