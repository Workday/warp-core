package com.workday.warp.dsl

import java.util.concurrent.{Callable, Executors, ScheduledExecutorService, ScheduledFuture, TimeUnit}

import com.workday.warp.TrialResult
import com.workday.warp.collectors.{AbstractMeasurementCollectionController, Defaults}
import com.workday.warp.common.utils.Implicits._
import com.workday.warp.inject.WarpGuicer

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionException
import scala.reflect.runtime.universe._
import scala.util.{Failure, Success, Try}

/**
  * Conducts experiments using the provided [[ExecutionConfig]].
  *
  * An experiment consists of 0 or more warmup invocations, and 1 or more measured trials. Warmup invocations will be
  * excluded from measurement. Depending on which measurement [[ModeWord]] is set and the number of specified threads
  * and invocations, we might collect non-intrusive measurements for individual invocations on individual threads.
  *
  * Note: The [[ResultType]] TypeTag represents the measured function's return value, while [[TrialType]] represents the type
  *       parameter of the returned [[TrialResult]]. This second type parameter is necessary because [[ResultType]] might
  *       be a [[TrialResult]], in which case we want to return a [[TrialResult]] containing the same [[TrialType]] parameter.
  *       If [[ResultType]] is some other type, then we will wrap it in a [[TrialResult]] and store the value as `maybeResult`.
  *
  * Created by tomas.mccandless on 12/20/16.
  */
case class Researcher[ResultType: TypeTag, TrialType](config: ExecutionConfig) {

  type FutureTrials = List[ScheduledFuture[TrialResult[TrialType]]]

  /**
    * Creates a [[Callable]] that invokes an unmeasured warmup invocation of `measuredFunction`.
    *
    * Recommended to call this once and then schedule the resulting [[Callable]] multiple times as needed.
    *
    * @param measuredFunction function to be invoked.
    * @return a [[Callable]] that can be scheduled on a threadpool.
    */
  private[this] def warmup(measuredFunction: => ResultType): Callable[TrialResult[TrialType]] = new Callable[TrialResult[TrialType]] {
    override def call(): TrialResult[TrialType] = {

      (measuredFunction, typeOf[ResultType]) match {
        // return type of measuredFunction was TrialResult[_]
        case (result, typ) if typ.erasure =:= typeOf[TrialResult[Any]] =>
          result.asInstanceOf[TrialResult[TrialType]]

        // return type of measuredFunction was Try[TrialResult], unbox the Try
        case (Success(result), typ) if isNestedTryTrialResult(typ) =>
          result.asInstanceOf[TrialResult[TrialType]]

        // some other generic return type
        case (resultValue, _) =>
          TrialResult[TrialType](maybeResult = Some(resultValue).asInstanceOf[Option[TrialType]])
      }
    }
  }


  /**
    * Creates a [[Callable]] that invokes and measures an invocation of `measuredFunction`.
    *
    * Recommended to call this once and then schedule the resulting [[Callable]] multiple times as needed.
    *
    * @param measuredFunction function to be invoked and measured.
    * @return a [[Callable]] that can be scheduled on a threadpool.
    */
  private[this] def trial(measuredFunction: => ResultType): Callable[TrialResult[TrialType]] = new Callable[TrialResult[TrialType]] {
    override def call(): TrialResult[TrialType] = {
      Researcher.this.runTrial(measuredFunction)
    }
  }


  /**
    * Submits multiple invocations of `function` to `pool` and waits for all invocations to complete.
    *
    * @param pool threadpool on which we'll be scheduling invocations.
    * @param function [[Callable]] that will be scheduled.
    * @param invocations number of times that `function` will be scheduled.
    * @return a [[List]] of results.
    */
  private[this] def scheduleAndWait(pool: ScheduledExecutorService,
                                    function: Callable[TrialResult[TrialType]],
                                    invocations: Int): List[Try[TrialResult[TrialType]]] = {
    var delay: Long = 0

    val futures: FutureTrials = invocations times {
      delay += this.config.distribution.truncatedSample
      pool.schedule(function, delay, TimeUnit.MILLISECONDS)
    }

    futures map { this.getAndRecover }
  }


  /**
    * Wraps `future` in a [[Try]], waits for it to complete, and recovers by creating a new [[Failure]] with the underlying
    * cause of failure.
    *
    * A [[ScheduledFuture]] whose computation throws an exception will wrap that exception in a generic [[ExecutionException]],
    * however we are more interested in the underlying cause of that exception.
    *
    * @param future [[ScheduledFuture]] to wait on for completion.
    * @return a [[Try]] containing the result of `future`.
    */
  private[this] def getAndRecover(future: ScheduledFuture[TrialResult[TrialType]]): Try[TrialResult[TrialType]] = {
    Try { future.get() } recoverWith { case exception: ExecutionException => Failure(exception.getCause) }
  }


  /**
    * Runs an experiment, which can consist of multiple warmups, measured trials, etc.
    *
    * Warmups are excluded from measurement. If there are multiple threads and multiple invocations, we'll create an outer
    * [[AbstractMeasurementCollectionController]] to measure the entire invocation schedule. If we're running with [[multi]] mode,
    * we'll additionally create a controller to measure each individual invocation, with intrusive measurements disabled.
    *
    * Depending on the [[ModeWord]] that we are using, we'll either return the result from the outer controller, or each of the
    * results from measuring the individual invocations.
    *
    * @param measuredFunction a function to measure.
    * @return a [[List]] containing measurement results.
    */
  @throws[RuntimeException]("when a warmup or measured trial fails.")
  private[dsl] def runExperiment(measuredFunction: => ResultType): List[TrialResult[TrialType]] = {
    // make the configuration available if its needed
    ConfigStore.put(this.config.testId, this.config)
    val pool: ScheduledExecutorService = Executors.newScheduledThreadPool(this.config.threads)
    // if we are multithreaded, and have more than 1 invocation, we'll handle things a bit differently.
    val threaded: Boolean = this.config.isThreaded

    // we'll schedule these Callables for warmups or measured trials
    val warmup: Callable[TrialResult[TrialType]] = this.warmup(measuredFunction)
    // don't measure individual trials if we are running in "single" mode
    val trial: Callable[TrialResult[TrialType]] = if (threaded && this.config.mode == single) warmup else this.trial(measuredFunction)

    // schedule the warmups and wait for them to complete
    val completedWarmups: List[Try[TrialResult[TrialType]]] = this.scheduleAndWait(pool, warmup, this.config.warmups)
    // unbox all the warmup results, might throw an exception. its important that we do this when no collectors are in progress
    completedWarmups map { _.get }

    // create outer controller for intrusive measurements -- we dont want to include warmups in this
    val maybeController: Option[AbstractMeasurementCollectionController] = if (threaded) Option(this.collectionController()) else None
    maybeController foreach { _.beginMeasurementCollection() }

    // schedule the measured trials and wait for them to complete
    val completedTrials: List[Try[TrialResult[TrialType]]] = this.scheduleAndWait(pool, trial, this.config.invocations)

    // stop the outer controller and shutdown threadpool
    val maybeOuterResult: Option[TrialResult[TrialType]] = maybeController map { _.endMeasurementCollection[TrialType]() }
    // simple sanity check -- there shouldn't be anything running on the pool now
    val incompleteTasks: List[Runnable] = pool.shutdownNow().asScala.toList
    if (incompleteTasks.nonEmpty) throw new RuntimeException(s"the following tasks were unexpectedly scheduled: $incompleteTasks")

    // return either the single outer result, or a list of all the measured trials
    // throw exception from a completed trial here
    val individualResults: List[TrialResult[TrialType]] = completedTrials map { _.get }
    if (threaded && this.config.mode == single) List(maybeOuterResult.get)
    else individualResults
  }


  /**
    * Runs a single experimental trial.
    *
    * Creates a [[AbstractMeasurementCollectionController]], invokes `measuredFunction` and collects measurements. If multiple
    * invocations are being scheduled in a threadpool, all intrusive [[com.workday.warp.collectors.abstracts.AbstractMeasurementCollector]]
    * will be disabled.
    *
    * @param measuredFunction
    * @return
    */
  private[dsl] def runTrial(measuredFunction: => ResultType): TrialResult[TrialType] = {
    val controller: AbstractMeasurementCollectionController = this.collectionController()
    // if we are in multithreaded mode, the inner mcc should have its intrusive measurements disabled
    if (this.config.isThreaded) controller.disableIntrusiveCollectors()

    controller.beginMeasurementCollection()

    // invoke the measured function. wrap in a Try, we can't throw an exception here which would not allow
    // controller to clean up after itself
    (Try { measuredFunction }, typeOf[ResultType]) match {
      // the measured function threw an exception, we don't want to record any results.
      case (Failure(resultValue), _) =>
        controller.endMeasurementCollection[TrialType](Failure(resultValue))

      // use the notion of elapsed time computed by the measured code block itself if we can
      // return type of measuredFunction was TrialResult[_]
      case (maybeResult, typ) if typ.erasure =:= typeOf[TrialResult[Any]] =>
        controller.endMeasurementCollection(maybeResult.asInstanceOf[Try[TrialResult[TrialType]]])

      // return type of measuredFunction was Try[TrialResult], so now we have a nested Try
      case (maybeResult, typ) if isNestedTryTrialResult(typ) =>
        // use flatten to unbox the nested Try[Try[_]]
        controller.endMeasurementCollection(maybeResult.asInstanceOf[Try[Try[TrialResult[TrialType]]]].flatten)

      // otherwise use elapsed wall clock time
      case (Success(resultValue), _) =>
        val trialResult: TrialResult[ResultType] = TrialResult(maybeResult = Some(resultValue))
        controller.endMeasurementCollection(Success(trialResult.asInstanceOf[TrialResult[TrialType]]))
    }
  }

  /**
    * Given a scala Type, check that it is a TrialResult nested in a Try
    *
    * @param typ scala.reflect.Type to check
    * @return true if typ is Try[TrialResult]
    */
  private def isNestedTryTrialResult(typ: Type): Boolean = {
    // First check that it wrapped in a Try.
    (typ.erasure =:= typeOf[Try[Any]]) &&
    // There should only be one type argument.
    (typ.typeArgs.length == 1) &&
    // Check that the type is TrialResult. We don't care about the type parameter at this point.
    (typ.typeArgs.head.erasure =:= typeOf[TrialResult[Any]])
  }

  /** @return a configured [[AbstractMeasurementCollectionController]] ready to instrument a measured function. */
  def collectionController(): AbstractMeasurementCollectionController = {
    val testId: String = if (this.config.testId.nonEmpty) this.config.testId else Defaults.testId
    val controller: AbstractMeasurementCollectionController = WarpGuicer.getController(testId, this.config.additionalTags)

    // configure measurement collectors
    if (this.config.disableExistingCollectors) controller.disableCollectors()
    controller.registerCollectors(this.config.additionalCollectors)

    // configure arbiters
    if (this.config.disableExistingArbiters) controller.disableArbiters()
    controller.registerArbiters(this.config.additionalArbiters)

    controller
  }
}
