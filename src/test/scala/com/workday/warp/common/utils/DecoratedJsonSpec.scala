package com.workday.warp.common.utils

import com.google.gson.JsonObject
import com.workday.warp.common.utils.Implicits.DecoratedJsonObject
import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.experimental.categories.Category
import org.junit.Test

class DecoratedJsonSpec extends WarpJUnitSpec {
  @Test
  @Category(Array(classOf[UnitTest]))
  def decoratedJsonSpec(): Unit = {
    val json: JsonObject = new JsonObject()
    json.addProperty("existingKey", "existingValue")

    json.getOrElse("existingKey", "default") should be ("existingValue")
    json.getOrElse("nonExistingKey", "default") should be ("default")
  }
}
