package com.workday.warp.dsl

import com.workday.telemetron.math.DistributionLike
import com.workday.warp.TrialResult
import com.workday.warp.arbiters.traits.ArbiterLike
import com.workday.warp.collectors.AbstractMeasurementCollectionController
import com.workday.warp.collectors.abstracts.AbstractMeasurementCollector
import com.workday.warp.common.CoreConstants
import com.workday.warp.persistence.Tag

import scala.reflect.runtime.universe._

/**
  * Holds configuration options for measured tests.
  *
  * Each configuration method invokes the `copy()` method to create a modified version of the config.
  *
  * The execution and measuring of tests is handled by [[Researcher]].
  *
  * Created by tomas.mccandless on 3/25/16.
  */
case class ExecutionConfig(invocations: Int = 1,
                           threads: Int = 1,
                           warmups: Int = 0,
                           distribution: DistributionLike = CoreConstants.DISTRIBUTION,
                           testId: String = CoreConstants.UNDEFINED_TEST_ID,
                           mode: ModeWord = multi,
                           disableExistingArbiters: Boolean = false,
                           additionalArbiters: List[ArbiterLike] = List.empty,
                           disableExistingCollectors: Boolean = false,
                           additionalCollectors: List[AbstractMeasurementCollector] = List.empty,
                           additionalTags: List[Tag] = List.empty) {


  /**
    * If this is true, we'll disable intrusive measurements on controllers for individual threads.
    *
    * @return true iff we have more than 1 thread and more than 1 invocation.
    */
  private[dsl] def isThreaded: Boolean = this.threads > 1 && this.invocations > 1


  /**
    * Part of the dsl. Sets test id for the measured function.
    *
    * @param newTestId test id under which to record measurements for the measured function.
    * @return a new [[ExecutionConfig]] with the specified test id.
    */
  @DslApi
  def testId(newTestId: String): ExecutionConfig = this.copy(testId = newTestId)


  /**
    * Part of the dsl. Sets number of invocations for the measured function. Enables syntax like 'using iterations 5'
    * Equivalent to `invocations`.
    *
    * @param newIterations [[Int]] specifying the number of times to invoke the measured function.
    * @return a new [[ExecutionConfig]] with the specified number of invocations.
    */
  @DslApi
  def iterations(newIterations: Int): ExecutionConfig = this.copy(invocations = newIterations)


  /**
    * Part of the dsl. Sets number of invocations for the measured function. Enables syntax like 'using invocations 5'.
    * Equivalent to `iterations`.
    *
    * @param newInvocations [[Int]] specifying the number of times to invoke the measured function.
    * @return a new [[ExecutionConfig]] with the specified number of invocations.
    */
  @DslApi
  def invocations(newInvocations: Int): ExecutionConfig = this.copy(invocations = newInvocations)


  /**
    * Part of the dsl. Sets number of warmup invocations for the measured function. Enables syntax like 'using warmups 5'.
    *
    * Warmup invocations will be excluded from measurement, will not be recorded in our db, and will not have thresholds checked.
    *
    * @param newWarmups [[Int]] specifying the number of warmup invocations for the measured function.
    * @return a new [[ExecutionConfig]] with the specified number of warmup invocations.
    */
  @DslApi
  def warmups(newWarmups: Int): ExecutionConfig = this.copy(warmups = newWarmups)


  /**
    * Part of the dsl. Sets number of threads for the measured function. Enables syntax like 'using threads 5'.
    *
    * @param newThreads [[Int]] specifying the size of a threadpool to create and schedule invocations of the measured function.
    * @return a new [[ExecutionConfig]] with the specified number of threads.
    */
  @DslApi
  def threads(newThreads: Int): ExecutionConfig = this.copy(threads = newThreads)


  /**
    * Part of the dsl. Sets a statistical distribution that specifies an expected delay between scheduling invocations.
    *
    * Note that this delay is between successive submissions, not between successive completed invocations.
    *
    * @param newDistribution [[DistributionLike]] specifying an expected delay between submitted invocations.
    * @return a new [[ExecutionConfig]] with the specified statistical distribution.
    */
  @DslApi
  def distribution(newDistribution: DistributionLike): ExecutionConfig = this.copy(distribution = newDistribution)


  /**
    * Part of the dsl. Sets measurement mode. Only applies to multithreaded tests. See [[ModeWord]].
    *
    * When running in [[single]] mode, we treat the entire collection of invocations as a single logical test. A single instance
    * of [[AbstractMeasurementCollectionController]] will be created to measure the entire invocation schedule, and individual invocations
    * will not be measured.
    *
    * When running in [[multi]] mode, we will also create an outer [[AbstractMeasurementCollectionController]] to measure the entire
    * schedule. However, we will additionally create a [[AbstractMeasurementCollectionController]] for each individual invocation,
    * and each of these inner controllers will have intrusive measurements disabled. The collection of [[TrialResult]]
    * returned by `measuring` will only include the inner measurements.
    *
    * The default mode is [[multi]].
    *
    * @param newMode [[ModeWord]] specifying the size of a threadpool to create and schedule invocations of the measured function.
    * @return a new [[ExecutionConfig]] with the specified measurement mode.
    */
  @DslApi
  def mode(newMode: ModeWord): ExecutionConfig = this.copy(mode = newMode)


  /**
    * Part of the dsl. Allows all arbiters or collectors to be disabled. Enables syntax like
    * `using no arbiters`
    *
    * @param configurable a [[Configurable]] indicating whether arbiters or collectors should be disabled.
    * @return a new [[ExecutionConfig]] with all arbitration or measurement collection disabled.
    */
  @DslApi
  def no(configurable: Configurable): ExecutionConfig = configurable.disable(this)


  /**
    * Part of the dsl. Enables syntax like `using only these collectors { ... }` or `using only these arbiters { ... }`.
    * See [[ResultOfOnlyThese.arbiters]] and [[ResultOfOnlyThese.collectors]], which allow creating a new [[ExecutionConfig]]
    * with existing arbiters/collectors disabled and additional arbiters/collectors registered.
    *
    * @param theseWord a [[TheseWord]], the only implementing instance of which should be [[these]].
    * @return this [[ExecutionConfig]] wrapped in a [[ResultOfOnlyThese]]. [[ResultOfOnlyThese.arbiters]] or
    *         [[ResultOfOnlyThese.collectors]] should probably be the next chained function call.
    */
  @DslApi
  def only(theseWord: TheseWord): ResultOfOnlyThese = new ResultOfOnlyThese(this)


  /**
    * Part of the dsl. Enables syntax like `using only defaults measuring { ... }`. Creates a new [[ExecutionConfig]]
    * with all default values.
    *
    * @param defaultsWord a [[DefaultsWord]], the only implementing instance of which should be [[defaults]].
    * @return a new [[ExecutionConfig]] with all default values.
    */
  @DslApi
  def only(defaultsWord: DefaultsWord): ExecutionConfig = new ExecutionConfig


  /**
    * Part of the dsl. Creates a new [[ExecutionConfig]] with the additional provided arbiters configured.
    *
    * @param arbiters a function that returns an iterable of [[ArbiterLike]] to act on the measured test result.
    * @return a new [[ExecutionConfig]] with additional arbiters configured.
    */
  @DslApi
  def arbiters(arbiters: => Iterable[ArbiterLike]): ExecutionConfig = {
    this.copy(additionalArbiters = this.additionalArbiters ++ arbiters)
  }


  /**
    * Part of the dsl. Creates a new [[ExecutionConfig]] with the additional provided arbiters, and existing arbiters
    * disabled. Equivalent to `using only these arbiters`
    *
    * @param arbiters a function that returns an iterable of [[ArbiterLike]] to act on the measured test result.
    * @return a new [[ExecutionConfig]] with additional arbiters configured.
    */
  @DslApi
  def onlyArbiters(arbiters: => Iterable[ArbiterLike]): ExecutionConfig = {
    this.copy(disableExistingArbiters = true, additionalArbiters = arbiters.toList)
  }


  /**
    * Part of the dsl. Creates a new [[ExecutionConfig]] with the additional provided collectors configured.
    *
    * @param collectors a function that returns an iterable of [[AbstractMeasurementCollector]] to measure the test.
    * @return a new [[ExecutionConfig]] with additional measurement collectors configured.
    */
  @DslApi
  def collectors(collectors: => Iterable[AbstractMeasurementCollector]): ExecutionConfig = {
    this.copy(additionalCollectors = this.additionalCollectors ++ collectors)
  }


  /** Part of the dsl. Creates a new [[ExecutionConfig]] with the additional provided collectors configured, and existing
    * collectors disabled. Equivalent to `using only these collectors`
    *
    * @param collectors a function returning an iterable of [[AbstractMeasurementCollector]] to measure the test.
    * @return a new [[ExecutionConfig]] with additional measurement collectors configured.
    */
  @DslApi
  def onlyCollectors(collectors: => Iterable[AbstractMeasurementCollector]): ExecutionConfig = {
    this.copy(disableExistingCollectors = true, additionalCollectors = collectors.toList)
  }


  /**
    * Part of the dsl. Measures `measuredFunction`, bracketed by a [[AbstractMeasurementCollectionController]]. Equivalent to
    * `measuring`.
    *
    * Based on the return type of `measuredFunction`, we either try to obtain a response time computed by `measuredFunction`
    * itself, or we default to using elapsed wall clock time
    *
    * [[ResultType]] corresponds to the return type of `measuredFunction`, while [[TrialType]] corresponds to the true value
    * of the result. [[ResultType]] might be some [[TrialResult]] with the type parameter `A`, in which case, `measure`
    * will return some `List[TrialResult[A]]`, where [[TrialType]] = `A`.
    *
    * @param measuredFunction function to measure. If the return type is [[TrialResult]], we record the response time
    *                         contained therein. Otherwise we default to measuring elapsed wall clock time.
    * @return a [[List]] of type [[TrialResult]] containing the measured response time.
    */
  @DslApi
  def measure[ResultType: TypeTag, TrialType](measuredFunction: => ResultType): List[TrialResult[TrialType]] =
    this.measuring[ResultType, TrialType](measuredFunction)


  /**
    * Part of the dsl. Measures `measuredFunction`, bracketed by a [[AbstractMeasurementCollectionController]]. Equivalent to
    * `measure`.
    *
    * Based on the return type of `measuredFunction`, we either try to obtain a response time computed by `measuredFunction`
    * itself, or we default to using elapsed wall clock time
    *
    * [[ResultType]] corresponds to the return type of `measuredFunction`, while [[TrialType]] corresponds to the true value
    * of the result. [[ResultType]] might be some [[TrialResult]] with the type parameter `A`, in which case, `measuring`
    * will return some `List[TrialResult[A]]`, where [[TrialType]] = `A`.
    *
    * @param measuredFunction function to measure. If the return type is [[TrialResult]], we record the response time
    *                         contained therein. Otherwise we default to measuring elapsed wall clock time.
    * @return a [[List]] of type [[TrialResult]] containing the measured response time.
    */
  @DslApi
  def measuring[ResultType: TypeTag, TrialType](measuredFunction: => ResultType): List[TrialResult[TrialType]] = {
    val researcher: Researcher[ResultType, TrialType] = Researcher(this)
    researcher.runExperiment(measuredFunction)
  }

  /**
    * Part of the dsl. Creates a new [[ExecutionConfig]] with the additional provided tags configured.
    *
    * @param tags a List of [[Tag]] that pertain to the test.
    * @return a new [[ExecutionConfig]] with additional tags configured.
    */
  @DslApi
  def tags(tags: => Iterable[Tag]): ExecutionConfig = {
    this.copy(additionalTags = this.additionalTags ++ tags)
  }
}


/**
  * Part of the dsl. Default configuration with no additional arbiters or collectors.
  *
  * We recommend starting with this configuration and amending it to fit the needs of your experiment.
  */
@DslApi
object using extends ExecutionConfig
