package com.workday.warp.monadic

import com.workday.warp.monadic.WarpAlgebra.WarpScript

import language.experimental.macros
import scala.reflect.macros.blackbox

/**
 * Created by tomas.mccandless on 8/16/21.
 */
object WarpMacros {

  /**
    * A simple toy macro that adds 2 integers
    *
    * @param a
    * @param b
    * @return the sum of `a` and `b`.
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


  /**
    * Rewrites a WarpScript for-comprehension AST to automatically insert TestIds into the `measure` calls.
    *
    * Constructs a Test Id as the concatenation of the fully qualified enclosing class name with the name of the user-provided
    * variable for that stage of the [[WarpScript]].
    *
    * For example, the following code snippet (assuming it is defined within a class named `com.workday.warp.Test`):
    *
    * {{{
    *   for {
    *     a <- exec(1 + 1)
    *     b <- measure(a + 1)
    *     c <- measure(b + 1)
    *     d <- measure(c + 1)
    *    } yield d
    * }}}
    *
    * would be transformed to this snippet:
    *
    * {{{
    *   for {
    *     a <- exec(1 + 1)
    *     b <- measure("com.workday.warp.Test.b", a + 1)
    *     c <- measure("com.workday.warp.Test.c", b + 1)
    *     d <- measure("com.workday.warp.Test.d", c + 1)
    *    } yield d
    * }}}
    *
    * This macro should be considered experimental.
    *
    * @param script [[WarpScript]] that will be rewritten.
    * @tparam T underlying return type of `script`.
    * @return
    */
  def deriveTestIds[T](script: WarpScript[T]): WarpScript[T] = macro deriveTestIdsImpl[T]

  def deriveTestIdsImpl[T: ctx.WeakTypeTag](ctx: blackbox.Context)(script: ctx.Expr[WarpScript[T]]): ctx.Expr[WarpScript[T]] = {
    // this untypecheck call is crucial to avoid crashing the compiler with mysterious errors like
    // [Error] : Error while emitting MacrosSpec.scala
    // value c
    // https://github.com/scala/bug/issues/11628 is possibly related?
    ctx.Expr[WarpScript[T]](testIdTransformer(ctx).transform(ctx.untypecheck(script.tree)))
  }


  /**
    * Creates a Tree transformer that traverses a [[WarpScript]] AST and inserts test Id into `measure` calls.
    *
    * @param ctx blackbox macro Context.
    * @return a (path-dependent) Tree transformer.
    */
  def testIdTransformer(ctx: blackbox.Context): ctx.universe.Transformer = {
    import ctx.universe._

    val measure: String = "com.workday.warp.monadic.WarpAlgebra.measure"

    new Transformer {
      val enclosingClass: String = extractEnclosingClass(ctx)
      // TODO try to avoid using a var here
      var capturedArg: Option[Tree] = None

      override def transform(tree: Tree): Tree = tree match {
        // capture arg here, but we dont need to transform
        case Apply(func, args) if show(func).startsWith(measure)
            && (func.symbol.toString == "method map" || func.symbol.toString == "method flatMap") =>
          capturedArg = args.headOption
          super.transform(tree)

        // a `measure` call. use the identifier we previously parsed to expand into a TestId
        case Apply(func, args) if show(func).startsWith(measure)
            // TODO checking args length here is a suboptimal way to check whether a testId is already provided
            && func.symbol.toString == "method measure" && args.length == 1 =>
          val testId: String = s"""$enclosingClass.${extractSyntheticMethodName(ctx)(capturedArg.get)}"""
          capturedArg = None
          println(s"""macro expansion: inserting testId "$testId" into expression "$tree"""")
          val newNode: Tree = Apply(func, Literal(Constant(testId)) :: args)
          // quasiquote the transformed and parsed tree https://docs.scala-lang.org/overviews/quasiquotes/intro.html
          q"${ctx.parse(show(newNode))}"

        case _ =>
          super.transform(tree)
      }
    }
  }



  /**
    * Reifies the enclosing runtime class and returns the name of that class.
    *
    * @param ctx blackbox macro Context.
    * @return fully qualified name of the enclosing runtime class.
    */
  @throws[MacroExpansionException]("when reifyEnclosingRuntimeClass does not return a Literal(Constant(_)) Tree node")
  def extractEnclosingClass(ctx: blackbox.Context): String = {
    import ctx.universe._
    ctx.reifyEnclosingRuntimeClass match {
      case Literal(Constant(className)) => className.toString
      case other => throw new MacroExpansionException(s"""unable to extract class name from Tree node: ${showRaw(other)}""")
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
  @throws[MacroExpansionException]("when called with a Tree that is not a Function node.")
  def extractSyntheticMethodName(ctx: blackbox.Context)(tree: ctx.universe.Tree): String = {
    import ctx.universe._
    tree match {
      case Function(List(ValDef(Modifiers(_), TermName(name), _, _)), _) => name
      case other => throw new MacroExpansionException(s"""unable to extract variable name from Tree node: ${showRaw(other)}""")
    }
  }
}