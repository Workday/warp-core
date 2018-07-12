package com.workday.telemetron.junit

import java.time.Instant
import java.util.concurrent.{ExecutionException, Executors, ScheduledExecutorService, TimeUnit}

import com.workday.telemetron.ResultReporter
import com.workday.telemetron.annotation.{Distribution, Schedule}
import com.workday.telemetron.math.{DistributionLike, DistributionLikeFactory, NullDistribution}
import com.workday.telemetron.utils.Implicits._
import org.junit.runner.Description
import org.junit.runners.model.{MultipleFailureException, Statement}
import org.pmw.tinylog.Logger

import scala.collection.mutable
import scala.collection.JavaConverters._

/**
  * Allows scheduling repeated invocations of a [[Statement]]
  *
  * Multiple invocations, potentially including multiple unmeasured warmup invocations, can be scheduled for execution
  * in a thread pool of customizable size. It is also possible to conduct load/stress tests by using a [[DistributionLike]]
  * to specify a statistical distribution that governs an expected delay between each test invocation.
  *
  * Created by leslie.lam on 12/18/17.
  * Based on java class created by michael.ottati.
  */
class ScheduledStatement(statement: Statement,
                         description: Description,
                         resultReporter: ResultReporter,
                         context: TelemetronContext,
                         invocations: Int = Schedule.INVOCATIONS_DEFAULT,
                         warmups: Int = Schedule.WARMUP_INVOCATIONS_DEFAULT,
                         threads: Int = Schedule.THREADS_DEFAULT,
                         distribution: DistributionLike = NullDistribution(),
                         val errors: List[Throwable] = List.empty) extends Statement {

  override def evaluate(): Unit = {
    // TODO Figure out a way to write an automated test that can be used verify these annotation exceptions

    // Fail immediately if there are annotation errors
    MultipleFailureException.assertEmpty(this.errors.asJava)

    val executorService: ScheduledExecutorService = Executors.newScheduledThreadPool(this.threads)

    val scheduleBeginTime: Instant = Instant.now

    // all invocations including warmup and measured invocations
    for (i <- 0 until this.warmups + this.invocations) {
      // First measured statement has sequence number 1. Warmups will have nonpositive sequence numbers.
      val sequenceNumber: Int = i - this.warmups + 1
      val delayTime: Long = i * this.distribution.truncatedSample

      val measuredStatement: MeasuredStatement = new MeasuredStatement(this.statement, this.description, this.resultReporter,
                                                                       this.context, sequenceNumber)
      executorService.schedule(new Runnable() {
        def run(): Unit = {
          try {
            measuredStatement.evaluate()
          }
          catch {
            case throwable: Throwable =>
              executorService.shutdownNow
              Logger.error(throwable)
              throw new ExecutionException(throwable)
          }
          Thread.currentThread.getName
        }
      }, delayTime, TimeUnit.MILLISECONDS)
    }

    Logger.debug("Milliseconds " + (Instant.now - scheduleBeginTime).toMillis)
    executorService.shutdown()
    /* Blocking wait call, waits for service process its work and fully shutdown */
    executorService.awaitTermination(Long.MaxValue, TimeUnit.DAYS)
  }
}

object ScheduledStatement {
  def apply(statement: Statement,
            description: Description,
            resultReporter: ResultReporter,
            context: TelemetronContext): ScheduledStatement = {
    val maybeSchedule: Option[Schedule] = Option(description.getAnnotation(classOf[Schedule]))

    maybeSchedule match {
      case None => new ScheduledStatement(statement, description, resultReporter, context)
      case Some(schedule) =>
        // Process the schedule annotation.
        val invocations: Int = schedule.invocations
        val warmups: Int = schedule.warmupInvocations
        val threads: Int = schedule.threads

        val annotatedDistribution: Distribution = schedule.distribution
        val distribution: DistributionLike = DistributionLikeFactory.getDistribution(annotatedDistribution.clazz,
                                                                                     annotatedDistribution.parameters)

        val errors: mutable.ListBuffer[Throwable] = mutable.ListBuffer()

        if (invocations < 0) {
          errors += new IllegalArgumentException("Invocations (" + invocations + ") must be >= 0")
        }

        if (warmups < 0) {
          errors += new IllegalArgumentException("Warmups (" + warmups + ") must be >= 0")
        }

        if (threads < 1) {
          errors += new IllegalArgumentException("Threads (" + threads + ") must be >= 0")
        }

        new ScheduledStatement(statement, description, resultReporter, context,
                               invocations, warmups, threads, distribution, errors.toList)
    }
  }
}
