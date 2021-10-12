package com.workday.warp.monadic

import com.workday.warp.logger.WarpLogging
import com.workday.warp.monadic.WarpAlgebra.WarpScript

import language.experimental.macros
import scala.reflect.macros.blackbox

/**
 * Created by tomas.mccandless on 8/16/21.
 */
// TODO this means we need to have logging (and thus property etc in scope at compile time)
object Macros extends {


  def add(a: Int, b: Int): Int = macro addImpl


  def addImpl(c: blackbox.Context)(a: c.Expr[Int], b: c.Expr[Int]): c.Expr[Int] = {
    import c.universe.reify
    // reify lifts a value into scala AST, Int => Expr[Int]
    reify {
      // splice converts from scala AST -> value, ie Expr[Int] => Int
      a.splice + b.splice
    }
  }




  def generateTestIds[T](s: WarpScript[T]): WarpScript[T] = macro generateTestIdsImpl[T]

  def generateTestIdsImpl[T: c.WeakTypeTag](c: blackbox.Context)(s: c.Expr[WarpScript[T]]): c.Expr[WarpScript[T]] = {
    import c.universe.reify

    println(s.tree.toString)

//    s.tree.c

    s
  }
}
