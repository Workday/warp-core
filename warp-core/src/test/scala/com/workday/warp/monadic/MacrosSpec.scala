package com.workday.warp.monadic

import com.workday.warp.TestId
import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import com.workday.warp.monadic.WarpAlgebra._
import com.workday.warp.TestIdImplicits._
import org.junit.jupiter.api.Test

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

//    val s: WarpScript[Int] =


    val sPrime: WarpScript[Int] = Macros.generateTestIds(for {
      a <- exec(1 + 1)
      b <- measure(a + 1)
      c <- measure(b + 1)
    } yield c)

    interpretImpure(sPrime)
  }


  @UnitTest
  def transformed(): Unit = {
    val s = com.workday.warp.monadic.WarpAlgebra.exec[Int](2)
      .flatMap[Int](((a: Int) => com.workday.warp.monadic.WarpAlgebra.measure[Int]("com.workday.warp.Test.bgenerated", a.+(1))
        .flatMap[Int](((b: Int) => com.workday.warp.monadic.WarpAlgebra.measure[Int]("com.workday.warp.Test.cgenerated", b.+(1)).map[Int](((c: Int) => c))))))


    interpretImpure(s)
  }
}
