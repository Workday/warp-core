package com.workday.warp.monadic

import com.workday.warp.monadic.WarpAlgebra.WarpScript

import language.experimental.macros
import scala.reflect.macros.blackbox

/**
 * Created by tomas.mccandless on 8/16/21.
 */
object Macros {


  /**
    * A simple toy macro that adds 2 integers
    *
    * @param a
    * @param b
    * @return
    */
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
    import c.universe._

    val t: c.universe.Tree = s.tree
    val tPrime: c.universe.Tree = transformTree(c)(t, None)
//    val r: c.Tree = c.parse(show(tPrime))
    val res = c.Expr[WarpScript[T]](tPrime)

    println("fully transformed: " + res.tree)
    res
  }



  def extractTestId(c: blackbox.Context)(tree: c.universe.Tree): String = {
    import c.universe._
    tree match {
      case Function(List(ValDef(Modifiers(_), TermName(name), _, _)), _) => name
      case _ => "unknown"
    }
  }

  /**
    * Recursively traverses `tree`, rewriting calls to `measure` to include
    * @param c
    * @param tree
    * @param capturedArg
    * @return
    */
  def transformTree(c: blackbox.Context)(tree: c.universe.Tree, capturedArg: Option[c.universe.Tree]): c.universe.Tree = {
    import c.universe._
    println("\n\r\n\r\ntransforming: " + show(tree))
    println("transforming (raw): " + showRaw(tree))

    tree match {
      // capture user-provided identifier and recursively pass that to the `measure` AST node to be rewritten
      case Apply(func: Tree, args: List[Tree]) if show(func).startsWith("com.workday.warp.monadic.WarpAlgebra.measure")
        && (func.symbol.toString == "method map" || func.symbol.toString == "method flatMap") =>
        println("need to capture arg")
        println("func:" + show(func))
        println("typ: " + func.tpe)
        println("symbol: " + func.symbol)
        println("args: " + args)
        println(showRaw(args.head))
        val newArgs: List[Tree] = args.map(transformTree(c)(_, capturedArg))
        val newFunc: Tree = transformTree(c)(func, newArgs.headOption)
        Apply(newFunc, newArgs)

      // a `measure` call. use the identifier we previously parsed to expand into a TestId
      case Apply(func: Tree, args: List[Tree]) if show(func).startsWith("com.workday.warp.monadic.WarpAlgebra.measure")
        && func.symbol.toString == "method measure" =>
        println("need to use captured arg")
        println("func:" + show(func))
        println("func (raw):" + showRaw(func))
        println("typ: " + func.tpe)
        println("symbol: " + func.symbol)
        println("args: " + args)
        println("captured arg: " + capturedArg)
        val testId: String = extractTestId(c)(capturedArg.get)
        println("testId: " + testId)
        val newFunc: Tree = transformTree(c)(func, None)
        val newArgs: List[Tree] = args.map(transformTree(c)(_, None))
        Apply(newFunc, Literal(Constant(s"com.workday.warp.Test.$testId")) :: newArgs)

      case Apply(func: Tree, args: List[Tree]) =>
        Apply(transformTree(c)(func, capturedArg), args.map(transformTree(c)(_, capturedArg)))
      case s@Select(Ident(TermName(_)), _) => s
      case Select(t, name) =>
        Select(transformTree(c)(t, capturedArg), name)
        // TODO this case appears to be needed and avoids generating a compiler error
      case f@Function(params, Ident(TermName(s))) =>
        f
      case f@Function(params, body) =>
        Function(params, transformTree(c)(body, capturedArg))
      case TypeApply(t, ts) =>
        TypeApply(transformTree(c)(t, capturedArg), ts.map(transformTree(c)(_, capturedArg)))
      case other =>
        println("other catchall case")
        println("typ: " + other.tpe)
        println("symbol: " + other.symbol)
        other
    }
  }
}