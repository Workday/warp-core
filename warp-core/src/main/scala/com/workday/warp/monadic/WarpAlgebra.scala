package com.workday.warp.monadic

import com.workday.warp.TestId
import com.workday.warp.inject.WarpGuicer
import scalaz._
import Free._
import com.workday.warp.logger.WarpLogging


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





object WarpAlgebra extends WarpLogging {

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
      logger.info("impure monadic exec")
      f()
    case Measure(testId, f) =>
      logger.info(s"impure monadic measuring ${testId.id}")
      val mcc = WarpGuicer.getController(testId)
      mcc.beginMeasurementCollection()
      val r: WarpScript[A] = f()
      mcc.endMeasurementCollection()
      r
  }
}