package com.workday.warp.utils

import java.util.concurrent.Executor

import slick.util.AsyncExecutor

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

/**
  * This is a synchronous version of [[AsyncExecutor]]. Because we previously would block on the calling thread anyway,
  * there was no benefit to using the AsyncExecutor.
  *
  * Created by vignesh.kalidas on 4/18/18
  */
class SynchronousExecutor extends AsyncExecutor {
  /**
    * This is a no-op
    */
  override def close(): Unit = {}
  override def executionContext: ExecutionContext = SynchronousExecutor.synchronousExecutionContext
}

object SynchronousExecutor {
  /**
    * An ExecutionContext for running Futures.
    *
    * This runs tasks in the calling thread, rather than requesting a new thread like its asynchronous counterpart
    */
  lazy val synchronousExecutionContext: ExecutionContextExecutor = {
    // seems Single Abstract Method only works in 2.12
    val executor: Executor = new Executor {
      override def execute(command: Runnable): Unit = command.run()
    }
    ExecutionContext.fromExecutor(executor)
  }
}
