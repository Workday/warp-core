package com.workday.warp.monadic

import com.workday.warp.monadic.WarpAlgebra.{WarpScript, interpretImpure}

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

  def addImpl(ctx: blackbox.Context)(a: ctx.Expr[Int], b: ctx.Expr[Int]): ctx.Expr[Int] = {
    import ctx.universe.reify
    // reify lifts a value into scala AST, Int => Expr[Int]
    // splice (in the context of reify) converts from scala AST -> value, ie Expr[Int] => Int
    reify {
      a.splice + b.splice
    }
  }

  def generateTestIds[T](script: WarpScript[T]): WarpScript[T] = macro generateTestIdsImpl[T]

  def generateTestIdsImpl[T: ctx.WeakTypeTag](ctx: blackbox.Context)(script: ctx.Expr[WarpScript[T]]): ctx.Expr[WarpScript[T]] = {
    import ctx.universe._

    object TestIdTransformer extends Transformer {
      // TODO try to avoid using a var here
      var capturedArg: Option[Tree] = None
      println(ctx.reifyEnclosingRuntimeClass.toString)
      println(ctx.reifyEnclosingRuntimeClass.tpe)
      println(ctx.reifyEnclosingRuntimeClass.symbol)
      println(showRaw(ctx.reifyEnclosingRuntimeClass))

      override def transform(tree: Tree): Tree = tree match {
        // capture arg here, but we dont need to transform
        case Apply(func, args) if show(func).startsWith("com.workday.warp.monadic.WarpAlgebra.measure")
          && (func.symbol.toString == "method map" || func.symbol.toString == "method flatMap") =>
          capturedArg = args.headOption
          super.transform(tree)

        // a `measure` call. use the identifier we previously parsed to expand into a TestId
        case Apply(func, _) if show(func).startsWith("com.workday.warp.monadic.WarpAlgebra.measure")
          && func.symbol.toString == "method measure" =>

          println(extractEnclosingClass(ctx))
          val testId: String = extractSyntheticMethodName(ctx)(capturedArg.get)
          capturedArg = None
          println(s"""macro expansion: inserting testId "$testId" into expression "$tree"""")
          // TODO needs to work for any type
          val lit = show(tree).replace(
            "com.workday.warp.monadic.WarpAlgebra.measure[Int](",
            s"""com.workday.warp.monadic.WarpAlgebra.measure[Int]("$testId", """
          )
          // println(s"lit: $lit")
          // quasiquote the transformed and parsed tree https://docs.scala-lang.org/overviews/quasiquotes/intro.html
          q"${ctx.parse(lit)}"

        case _ =>
          super.transform(tree)
      }
    }
    // this untypecheck call is crucial to avoid crashing the compiler with mysterious errors like
    // [Error] : Error while emitting MacrosSpec.scala
    // value c
    // https://github.com/scala/bug/issues/11628 is possibly related?
    ctx.Expr[WarpScript[T]](TestIdTransformer.transform(ctx.untypecheck(script.tree)))
  }


  def extractEnclosingClass(ctx: blackbox.Context): String = {
    import ctx.universe._
    ctx.reifyEnclosingRuntimeClass match {
      case Literal(Constant(className)) => className.toString
      case t@TypeApply(_, _) =>
        val pattern = """(Predef\.this\.)?classOf\[(.+)\]""".r
        val pattern(_, cls) = t.toString
        cls
      case other => throw new RuntimeException(s"""we can only extract a class name from a reified enclosing runtime class: ${showRaw(other)}""")
    }
  }


  /**
    * Extracts a named argument from a function tree.
    *
    * ie, given a tree representing ((a: IU
    * @param ctx
    * @param tree
    * @return
    */
  def extractSyntheticMethodName(ctx: blackbox.Context)(tree: ctx.universe.Tree): String = {
    import ctx.universe._
    // TODO check what happens when _ is used in WarpScript instead of a variable name
    tree match {
      case Function(List(ValDef(Modifiers(_), TermName(name), _, _)), _) => s"""${extractEnclosingClass(ctx)}.$name"""
      case _ => throw new RuntimeException("super hacky but we can only extract a test id from a function")
    }
  }
}