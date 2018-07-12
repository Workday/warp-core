package com.workday.warp.persistence.exception

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Created by tomas.mccandless on 6/11/18.
  */
class ExceptionSpec extends WarpJUnitSpec {

  @Test
  @Category(Array(classOf[UnitTest]))
  def exceptionMessages(): Unit = {
    val message: String = "error persisting field"
    new WarpFieldPersistenceException(message).getMessage should be (message)
    new InvalidTypeClassException(message).getMessage should be (message)
  }
}
