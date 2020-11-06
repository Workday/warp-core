package com.workday.warp.config.utils

import com.google.gson.JsonObject
import com.workday.warp.utils.Implicits.DecoratedJsonObject
import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}

class DecoratedJsonSpec extends WarpJUnitSpec {

  @UnitTest
  def decoratedJsonSpec(): Unit = {
    val json: JsonObject = new JsonObject()
    json.addProperty("existingKey", "existingValue")

    json.getOrElse("existingKey", "default") should be ("existingValue")
    json.getOrElse("nonExistingKey", "default") should be ("default")
  }
}
