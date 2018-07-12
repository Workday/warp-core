package com.workday.warp.common.utils

import java.util.concurrent.Executors

import com.workday.warp.common.CoreWarpProperty._
import org.pmw.tinylog.Logger

import scala.concurrent.{Await, Future, ExecutionContext}
import scala.concurrent.duration._
import scala.util.{Try, Failure, Success}

/**
 * Utility functions for dealing with Futures.
 *
 * Created by tomas.mccandless on 12/21/15.
 */
object FutureUtils {

  // threadpool implicitly passed to Future functions
  implicit val executor: ExecutionContext = ExecutionContext.fromExecutor(
    Executors.newFixedThreadPool(this.threadpoolSize(WARP_NUM_COLLECTOR_THREADS.value))
  )


  /**
    * @param threads a String to be parsed as an integer number of threads
    * @return the size of the threadpool we should use. Returns the default if the configured value is nonpositive or
    *         not parseable as an integer
    */
  def threadpoolSize(threads: String): Int = {
    val defaultThreads: Int = WARP_NUM_COLLECTOR_THREADS.defaultValue.toInt

    try {
      val numThreads = threads.toInt
      if (numThreads < 1) defaultThreads else numThreads
    }
    catch {
      case e: NumberFormatException =>
        Logger.error(s"error parsing ${WARP_NUM_COLLECTOR_THREADS.propertyName}=$threads. " +
          s"using default value $defaultThreads", e)
        defaultThreads
    }
  }


  /**
    * Converts a Future to Future[Try]
    *
    * @param future some Future to inject with a Try
    * @tparam T type of the Future
    * @return a Future wrapped around a Try
    */
  def toFutureTry[T](future: Future[T]): Future[Try[T]] = future.map(Success(_)).recover { case x => Failure(x) }


  /**
    * Runs a sequence of tasks synchronously in our threadpool. Blocks until all tasks have completed.
    * Returns a sequence of Try objects containing the results of each task.
    *
    * @param tasks a sequence of tasks to be executed in parallel in our threadpool.
    * @tparam T return type of each task.
    * @return a sequence of Try objects containing the results of each executed task.
    */
  def execute[T](tasks: Seq[Future[T]]): Seq[Try[T]] = {
    val futureTrys: Seq[Future[Try[T]]] = tasks map toFutureTry
    val sequencedTasks: Future[Seq[Try[T]]] = Future.sequence(futureTrys)

    // block until the tasks are completed
    Await.result(sequencedTasks, Duration.Inf)
  }
}
