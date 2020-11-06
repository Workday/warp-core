package com.workday.warp.persistence.exception

import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}

/**
  * Created by tomas.mccandless on 6/11/18.
  */
class ExceptionSpec extends WarpJUnitSpec {

  @UnitTest
  def exceptionMessages(): Unit = {
    val message: String = "error persisting field"
    new WarpFieldPersistenceException(message).getMessage should be (message)
    new InvalidTypeClassException(message).getMessage should be (message)
  }
}
