package com.workday.warp.monadic

import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import com.workday.warp.monadic.WarpAlgebra._
import com.workday.warp.TestIdImplicits._

/**
 * Created by tomas.mccandless on 8/16/21.
 */
class MacrosSpec extends WarpJUnitSpec {

  @UnitTest
  def addMacro(): Unit = {
    Macros.add(1, 3) should be (4)
  }


  @UnitTest
  def genTestIds(): Unit = {



    val sPrime = Macros.generateTestIds(for {
      a <- exec(1 + 1)
      b <- measure("com.worday.warp.monadic.b", a + 1)
    } yield b)
  }
}
