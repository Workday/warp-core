package com.workday.warp.monadic

import com.workday.warp.TestId
import com.workday.warp.inject.WarpGuicer

import scalaz._
import Free._
import org.pmw.tinylog.Logger

/**
 * Created by tomas.mccandless on 7/22/21.
 */
sealed abstract class MMM[+A]
case class Measure[A](testId: TestId, f: () => A) extends MMM[A]
case class Exec[A](f: () => A) extends MMM[A]

object MMM {
  type Script[A] = Free[MMM, A]

  implicit val functor: Functor[MMM] = new Functor[MMM] {
    def map[A, B](kvs: MMM[A])(f: A => B): MMM[B] = kvs match {
      case Measure(testId, g) => Measure(testId, () => f(g()))
      case Exec(g) => Exec(() => f(g()))
    }
  }
  // lifting functions

  def measure[A](testId: TestId, f: => A): Script[A] = liftF(Measure(testId, () => f))

  def exec[A](f: => A): Script[A] = liftF(Exec(() => f))


  def interpretImpure[A](s: Script[A]): A = s.go {
    case Exec(f) =>
      Logger.info("exec")
      f()
    case Measure(testId, f) =>
      Logger.info(s"measuring ${testId.id}")
      val mcc = WarpGuicer.getController(testId)
      mcc.beginMeasurementCollection()
      val r: Script[A] = f()
      mcc.endMeasurementCollection()
      r
  }
}
