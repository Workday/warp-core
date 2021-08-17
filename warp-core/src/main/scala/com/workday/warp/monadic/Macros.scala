package com.workday.warp.monadic

import com.workday.warp.monadic.WarpAlgebra.WarpScript
import org.pmw.tinylog.Logger

import language.experimental.macros
import scala.reflect.macros.blackbox

/**
 * Created by tomas.mccandless on 8/16/21.
 */
object Macros {


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

    Logger.info(s.tree)

    s.tree.c

    s
  }
}
