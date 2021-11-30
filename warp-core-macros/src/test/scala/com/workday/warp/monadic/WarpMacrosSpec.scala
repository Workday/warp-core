package com.workday.warp.monadic

import com.workday.warp.TestIdImplicits._
import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import com.workday.warp.logger.WarpLogging
import com.workday.warp.monadic.WarpMacros.{add, deriveTestIds}
import com.workday.warp.monadic.WarpAlgebra._
import org.junit.jupiter.api.{Test, TestInfo}

/**
 * Created by tomas.mccandless on 8/16/21.
 */
class WarpMacrosSpec extends WarpJUnitSpec with WarpLogging {

  @UnitTest
  def addMacro(): Unit = {
    add(1, 3) should be (4)
  }


  /** A simple example. */
  @UnitTest
  def genTestIds(): Unit = {

    val script: WarpScript[Int] = deriveTestIds {
      for {
        a <- exec(1 + 1)
        b <- measure(a + 1)
        c <- measure(b + 1)
        d <- measure(c + 1)
      } yield d
    }

    interpretImpure(script)
  }


  /** An example of what a de-sugared and transformed [[WarpScript]] looks like. */
  @UnitTest
  def transformed(): Unit = {
    val script: WarpScript[Int] = com.workday.warp.monadic.WarpAlgebra.exec[Int](2)
      .flatMap[Int]((a: Int) => com.workday.warp.monadic.WarpAlgebra.measure[Int]("com.workday.warp.Test.bgenerated", a + 1)
        .flatMap[Int]((b: Int) => com.workday.warp.monadic.WarpAlgebra.measure[Int]("com.workday.warp.Test.cgenerated", b + 1)
          .map[Int]((c: Int) => c)))

    interpretImpure(script)
  }


  /**
    * This test fails in 2.11 and 2.12, emitted code is parsed as an XML literal:
    *
    * macro expansion: inserting testId "com.workday.warp.monadic.MacrosSpec.c" into expression
    * {{{
    *   com.workday.warp.monadic.WarpAlgebra.measure[List[String]]({
    *     <synthetic> <artifact> val x$1 = "defg";
    *     immutable.this.List.apply[String](b).::[String](x$1)
    *   })
    * }}}
    *
    * [Error] MacrosSpec.scala:36: exception during macro expansion:
    * scala.reflect.macros.ParseException: in XML literal: in XML content, please use '}}' to express '}'
    *         at scala.reflect.macros.contexts.Parsers$$anonfun$parse$1.apply(Parsers.scala:18)
    *         at scala.reflect.macros.contexts.Parsers$$anonfun$parse$1.apply(Parsers.scala:17)
    *
    *
    * Under scala 2.13 it works, emitting code like this:
    *
    * macro expansion: inserting testId "com.workday.warp.monadic.MacrosSpec.c" into expression
    * {{{
    *   com.workday.warp.monadic.WarpAlgebra.measure[List[String]]({
    *    scala.`package`.List.apply[String](k).::[String]("defg")
    *   })
    * }}}
    */
  @UnitTest
  def otherTypes(): Unit = {
    val script: WarpScript[Option[String]] = deriveTestIds {
      for {
        a <- exec(1 + 1)
        b <- measure(a.toString + "abcd")
        c <- measure("defg" :: List(b))
        d <- measure(c.headOption)
      } yield d
    }

    interpretImpure(script)
  }


  /** Checks usage with an underscore instead of an identifier (scalac will create a synthetic identifier like "x$1") */
  @UnitTest
  def underscore(): Unit = {
    val script: WarpScript[String] = deriveTestIds {
      for {
        j <- exec(1 + 1)
        k <- measure(j.toString + "abcd")
        _ <- measure("defg" :: List(k))
        m <- measure(k + "hijk")
      } yield m
    }

    interpretImpure(script)
  }


  /** Checks that we don't try to insert a test Id into a measure call that already has a provided test Id. */
  @UnitTest
  def providedTestId(info: TestInfo): Unit = {
    val script: WarpScript[Int] = deriveTestIds {
      for {
        a <- exec(1 + 1)
        _ <- measure(info, a + 1)
        b <- measure(info.id, a + 1)
        c <- measure(b + 1)
        d <- measure(c + 1)
      } yield d
    }

    interpretImpure(script)
  }


  /** Checks measuring some code blocks instead of inline calls. */
  @UnitTest
  def codeBlock(info: TestInfo): Unit = {
    val script: WarpScript[Int] = deriveTestIds {
      for {
        a <- exec(1 + 1)
        _ <- measure(info, a + 1)
        b <- measure(info.id, a + 1)
        c <- measure {
          val x = b + 1
          logger.info("measuring a code block")
          List(x, x + 1, x + 3)
        }
        d <- measure(c.head + 1)
      } yield d
    }

    interpretImpure(script)
  }
}


class MacroExampleSpec {

  @Test
  def macroExample(): Unit = {
    val script: WarpScript[Option[String]] = deriveTestIds {
      for {
        a <- exec(1 + 1)
        b <- measure(a.toString + "abcd")
        c <- measure("defg" :: List(b))
        d <- measure(c.headOption)
      } yield d
    }

    interpretImpure(script)
  }
}