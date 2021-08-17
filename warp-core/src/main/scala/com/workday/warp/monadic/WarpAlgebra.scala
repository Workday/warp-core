package com.workday.warp.monadic

import com.workday.warp.TestId
import com.workday.warp.inject.WarpGuicer
import scalaz._
import Free._
import org.pmw.tinylog.Logger


/**
 * Algebraic data type (ADT) defining the operations we want to support in WarpScript.
 *
 * Some examples of other familiar constructs represented as ADT:
 *
 * {{{
 *   data List a = Nil | Cons a (List a)
 * }}}
 *
 * {{{
 *   data Tree a = Empty
 *               | Leaf a
 *               | Node Tree Tree
 * }}}
 *
 *
 * ADT are well suited for representing abstract syntax trees:
 *
 * {{{
 *   data Expression = Number Int
 *                   | Add Expression Expression
 *                   | Minus Expression Expression
 *                   | Mult Expression Expression
 *                   | Divide Expression Expression
 * }}}
 *
 * Created by tomas.mccandless on 7/22/21.
 */
sealed abstract class WarpAlgebra[+A]
// a measured expression
case class Measure[A](testId: TestId, f: () => A) extends WarpAlgebra[A]
// an unmeasured expression, useful for setup
case class Exec[A](f: () => A) extends WarpAlgebra[A]





object WarpAlgebra {

  /**
   * the Free monad allows us to build a Monad (~programmable semicolon) from any Functor (mapping between categories).
   *
   * Thus, we need to define a Functor (map) for our algebra
   */
  implicit val func: Functor[WarpAlgebra] = new Functor[WarpAlgebra] {

    override def map[A, B](wm: WarpAlgebra[A])(f: A => B): WarpAlgebra[B] = wm match {
      case Measure(testId, g) => Measure(testId, () => f(g()))
      case Exec(g) => Exec(() => f(g()))
    }
  }

  type WarpScript[A] = Free[WarpAlgebra, A]

  // lifting functions, we lift a call-by-name parameter into a WarpScript with a lambda
  // this is the api that will be used in for-comprehensions
  def measure[A](testId: TestId, f: => A): WarpScript[A] = liftF(Measure(testId, () => f))
  def exec[A](f: => A): WarpScript[A] = liftF(Exec(() => f))


  /**
   * An impure interpreter for WarpScript.
   *
   * Runs the script to completion.
   *
   * @param s
   * @tparam A
   * @return
   */
  def interpretImpure[A](s: WarpScript[A]): A = s.go {
    case Exec(f) =>
      Logger.info("impure monadic exec")
      f()
    case Measure(testId, f) =>
      Logger.info(s"impure monadic measuring ${testId.id}")
      val mcc = WarpGuicer.getController(testId)
      mcc.beginMeasurementCollection()
      val r: WarpScript[A] = f()
      mcc.endMeasurementCollection()
      r
  }
}



//sealed abstract class Expr[+A]
//
//case class Number[A](i: A) extends Expr[A]
//case class Add[A](e1: Expr[A], e2: Expr[A]) extends Expr[A]
//case class Diff[A](e1: Expr[A], e2: Expr[A]) extends Expr[A]
//case class Mult[A](e1: Expr[A], e2: Expr[A]) extends Expr[A]
//case class Div[A](e1: Expr[A], e2: Expr[A]) extends Expr[A]
//case class Bool[A](b: A) extends Expr[A]
//case class And[A](e1: Expr[A], e2: Expr[A]) extends Expr[A]
//case class Or[A](e1: Expr[A], e2: Expr[A]) extends Expr[A]
//case class Not[A](e1: Expr[A]) extends Expr[A]
//
//object Expr {
//
//  implicit val func: Functor[Expr] = new Functor[Expr] {
//    def map[A, B](fa: Expr[A])(f: A => B): Expr[B] = fa match {
//      case Number(i) => Number(f(i))
//      case Add(e1, e2) => Add(map(e1)(f), map(e2)(f))
//      case Diff(e1, e2) => Diff(map(e1)(f), map(e2)(f))
//      case Mult(e1, e2) => Mult(map(e1)(f), map(e2)(f))
//      case Div(e1, e2) => Div(map(e1)(f), map(e2)(f))
//      case Bool(b) => Bool(f(b))
//      case And(e1, e2) => And(map(e1)(f), map(e2)(f))
//      case Or(e1, e2) => Or(map(e1)(f), map(e2)(f))
//      case Not(b) => Not(map(b)(f))
//    }
//  }
//
//  type ExprScript[A] = Free[Expr, A]
//
//  def number[A](i: A): ExprScript[A] = liftF(Number(i))
//  def add[A](e1: Expr[A], e2: Expr[A]): ExprScript[A] = liftF(Add(e1, e2))
//  def diff[A](e1: Expr[A], e2: Expr[A]): ExprScript[A] = liftF(Diff(e1, e2))
//  def mult[A](e1: Expr[A], e2: Expr[A]): ExprScript[A] = liftF(Mult(e1, e2))
//  def div[A](e1: Expr[A], e2: Expr[A]): ExprScript[A] = liftF(Div(e1, e2))
//  def bool[A](i: A): ExprScript[A] = liftF(Bool(i))
//  def and[A](e1: Expr[A], e2: Expr[A]): ExprScript[A] = liftF(And(e1, e2))
//  def or[A](e1: Expr[A], e2: Expr[A]): ExprScript[A] = liftF(Or(e1, e2))
//  def not[A](e1: Expr[A]): ExprScript[A] = liftF(Not(e1))
//
//  def interpretExprScript[A: Numeric](s: ExprScript[A]): A = s.go {
//    case Number(i) => i
//    case Add(e1, e2) => implicitly[Numeric[A]](interpretExpr(Add(e1, e2)))
//  }
//
//
//  def interpretExpr[A: Numeric](e: Expr[A])(implicit ev: Numeric[A]): A = e match {
//    case Number(i) => i
//    case Add(e1, e2) => implicitly[Numeric[A]].plus(interpretExpr(e1), interpretExpr(e2))
//  }
//}