package com.workday.telemetron.junit

import org.junit.runners.model.{FrameworkMethod, Statement}
import scala.collection.mutable

/**
  * Augments a [[Statement]] with additional setup/teardown methods.
  *
  * These additional methods are invoked only once even if multiple test class instances are created using a [[ScheduledStatement]].
  *
  * Created by vignesh.kalidas on 2/9/17.
  */
class SurroundOnceStatement(val next: Statement, val beforeMethods: Array[FrameworkMethod],
                            val afterMethods: Array[FrameworkMethod], val target: Any) extends Statement {
  /**
    * The process for evaluating the before and after methods
    */
  override def evaluate(): Unit = {
    beforeMethods foreach invokeOnce
    SurroundOnceStatement.ranMethods.clear()

    next.evaluate()

    afterMethods foreach invokeOnce
    SurroundOnceStatement.ranMethods.clear()
  }

  /**
    * Synchronously invokes the given method if it hasn't been invoked yet and updates the set in the companion object.
    *
    * @param method method to be invoked once
    */
  def invokeOnce(method: FrameworkMethod): Unit = {
    SurroundOnceStatement.ranMethods.synchronized {
      if (!SurroundOnceStatement.ranMethods.contains(method)) {
        SurroundOnceStatement.ranMethods += method
        method.invokeExplosively(target)
      }
    }
  }
}

/**
  * Statement's companion object.
  *
  * Tracks which setup/teardown methods have already been invoked.
  */
object SurroundOnceStatement {
  val ranMethods: mutable.Set[FrameworkMethod] = mutable.Set.empty
}
