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

    val script: WarpScript[Int] = Macros.generateTestIds(for {
      a <- exec(1 + 1)
      b <- measure(a + 1)
      c <- measure(b + 1)
      d <- measure(c + 1)
    } yield d)

    interpretImpure(script)
  }


//  @UnitTest
//  def otherTypes(): Unit = {
//    val script: WarpScript[Option[String]] = Macros.generateTestIds(for {
//
//      a <- exec(1 + 1)
//      b <- measure(a.toString + "abcd")
//      c <- measure("defg" :: List(b))
////      d <- measure(c.headOption)
//    } yield c)
//
//    interpretImpure(script)
//  }


//  @UnitTest
//  def underscore(): Unit = {
//    val script: WarpScript[Option[String]] = Macros.generateTestIds(for {
//
//      a <- exec(1 + 1)
//      b <- measure(a.toString + "abcd")
//      _ <- measure("defg" :: List(b))
//      d <- measure(b + "hijk")
//    } yield d)
//
//    interpretImpure(script)
//  }

  /**
    * Pasted the output of our transformation, we get as far as fully transforming the tree before "error while emitting"
    * compiler crash
    */
  @UnitTest
  def transformed(): Unit = {
    val s = com.workday.warp.monadic.WarpAlgebra.exec[Int](2)
      .flatMap[Int](((a: Int) => com.workday.warp.monadic.WarpAlgebra.measure[Int]("com.workday.warp.Test.bgenerated", a.+(1))
        .flatMap[Int](((b: Int) => com.workday.warp.monadic.WarpAlgebra.measure[Int]("com.workday.warp.Test.cgenerated", b.+(1)).map[Int](((c: Int) => c))))))


    interpretImpure(s)
  }
}
