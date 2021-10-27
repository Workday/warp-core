package com.workday.warp.monadic

import com.workday.warp.monadic.WarpAlgebra.{WarpScript, interpretImpure}

import language.experimental.macros
import scala.reflect.macros.blackbox
import scala.util.matching.Regex

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
    // this untypecheck call is crucial to avoid crashing the compiler with mysterious errors like
    // [Error] : Error while emitting MacrosSpec.scala
    // value c
    // https://github.com/scala/bug/issues/11628 is possibly related?
    ctx.Expr[WarpScript[T]](testIdTransformer(ctx).transform(ctx.untypecheck(script.tree)))
  }


  /**
    * Creates a Tree transformer that traverses
    *
    * @param ctx blackbox macro Context
    * @return a (path-dependent) Tree transformer
    */
  def testIdTransformer(ctx: blackbox.Context): ctx.universe.Transformer = {
    import ctx.universe._

    new Transformer {
      val enclosingClass: String = extractEnclosingClass(ctx)
      // TODO try to avoid using a var here
      var capturedArg: Option[Tree] = None

      override def transform(tree: Tree): Tree = tree match {
        // capture arg here, but we dont need to transform
        case Apply(func, args) if show(func).startsWith("com.workday.warp.monadic.WarpAlgebra.measure")
            && (func.symbol.toString == "method map" || func.symbol.toString == "method flatMap") =>
          capturedArg = args.headOption
          super.transform(tree)

        // a `measure` call. use the identifier we previously parsed to expand into a TestId
        case Apply(func, _) if show(func).startsWith("com.workday.warp.monadic.WarpAlgebra.measure")
            && func.symbol.toString == "method measure" =>
          rewriteNode(tree)

        case _ =>
          super.transform(tree)
      }


      def rewriteNode(tree: Tree): Tree = {
        val testId: String = s"""$enclosingClass.${extractSyntheticMethodName(ctx)(capturedArg.get)}"""
        capturedArg = None
        println(s"""macro expansion: inserting testId "$testId" into expression "$tree"""")
        val pattern: Regex = """com\.workday\.warp\.monadic\.WarpAlgebra\.measure\[(.+)\]\((.+)\)""".r
        val pattern(typ, args) = show(tree)
        println(s"""parsed $typ type and $args args from $tree""")
        val newCode = s"""com.workday.warp.monadic.WarpAlgebra.measure[$typ]("$testId", $args)"""
        // quasiquote the transformed and parsed tree https://docs.scala-lang.org/overviews/quasiquotes/intro.html
        q"${ctx.parse(newCode)}"
      }
    }
  }



  /**
    *
    * @param ctx blackbox macro Context
    * @return fully qualified class name of the enclosing runtime class.
    */
  def extractEnclosingClass(ctx: blackbox.Context): String = {
    import ctx.universe._
    ctx.reifyEnclosingRuntimeClass match {
      // 2.13
      case Literal(Constant(className)) => className.toString
      // 2.11, 2.12
      case t@TypeApply(_, _) =>
        val pattern: Regex = """(Predef\.this\.)?classOf\[(.+)\]""".r
        val pattern(_, cls) = t.toString
        cls
      case other => throw new RuntimeException(s"""we can only extract a class name from a reified enclosing runtime class: ${showRaw(other)}""")
    }
  }


  /**
    * Extracts a named argument from a function tree.
    *
    * ie, given a tree representing ((a: Int) => a + 1), returns "a"
    * @param ctx blackbox macro Context
    * @param tree
    * @return
    */
  def extractSyntheticMethodName(ctx: blackbox.Context)(tree: ctx.universe.Tree): String = {
    import ctx.universe._
    // TODO check what happens when _ is used in WarpScript instead of a variable name
    tree match {
      case Function(List(ValDef(Modifiers(_), TermName(name), _, _)), _) => name
      case _ => throw new RuntimeException("super hacky but we can only extract a test id from a function")
    }
  }
}