package com.workday.warp.monadic

import com.workday.warp.TestId
import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import com.workday.warp.monadic.WarpAlgebra._
import com.workday.warp.TestIdImplicits._
import org.junit.jupiter.api.{Disabled, Test, TestInfo}

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


  /**
    * TODO fails in 2.11 and 2.12:
    *

  macro expansion: inserting testId "com.workday.warp.monadic.MacrosSpec.c" into expression "com.workday.warp.monadic.WarpAlgebra.measure[List[String]]({
  <synthetic> <artifact> val x$1 = "defg";
  immutable.this.List.apply[String](b).::[String](x$1)
})"

[Error] /Users/tomas.mccandless/code/warp-core-tomnis/warp-core/src/test/scala/com/workday/warp/monadic/MacrosSpec.scala:36: exception during macro expansion:
scala.reflect.macros.ParseException: in XML literal: in XML content, please use '}}' to express '}'
        at scala.reflect.macros.contexts.Parsers$$anonfun$parse$1.apply(Parsers.scala:18)
        at scala.reflect.macros.contexts.Parsers$$anonfun$parse$1.apply(Parsers.scala:17)





    under 2.13 it works, emitting code like this:

    macro expansion: inserting testId "com.workday.warp.monadic.MacrosSpec.x$1" into expression "com.workday.warp.monadic.WarpAlgebra.measure[List[String]]({
  scala.`package`.List.apply[String](k).::[String]("defg")
})"

    */
  @UnitTest
  def otherTypes(): Unit = {
    val script: WarpScript[Option[String]] = Macros.generateTestIds(for {
      a <- exec(1 + 1)
      b <- measure(a.toString + "abcd")
      c <- measure("defg" :: List(b))
      d <- measure(c.headOption)
    } yield d)

    interpretImpure(script)
  }


  @UnitTest
  def underscore(): Unit = {
    val script: WarpScript[String] = Macros.generateTestIds(for {
      j <- exec(1 + 1)
      k <- measure(j.toString + "abcd")
      _ <- measure("defg" :: List(k))
      m <- measure(k + "hijk")
    } yield m)

    interpretImpure(script)
  }


  // TODO check this test. what should be the behavior? i guess if theres a user-provided test id, that should
  // take priority
//  @UnitTest
//  @Disabled
//  def providedTestId(info: TestInfo): Unit = {
//    val script: WarpScript[Int] = Macros.generateTestIds(for {
//      a <- exec(1 + 1)
//      b <- measure(testInfo2TestId(info), a + 1)
//      c <- measure(b + 1)
//      d <- measure(c + 1)
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
