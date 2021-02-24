package com.workday.warp.controllers

import java.time.{Duration, Instant}

import com.workday.warp.TestIdImplicits._
import com.workday.warp.arbiters.{ArbiterLike, Ballot}
import com.workday.warp.collectors.AbstractMeasurementCollector
import com.workday.warp.persistence.Tables.{TestDefinitionMetaTag => _, TestExecutionMetaTag => _, _}
import com.workday.warp.persistence.TablesLike.RowTypeClasses._
import com.workday.warp.persistence.TablesLike._
import com.workday.warp.persistence.{Tag, _}
import com.workday.warp.persistence.exception.WarpFieldPersistenceException
import com.workday.warp.utils.Implicits._
import com.workday.warp.utils.{AnnotationReader, FutureUtils, TimeUtils}
import com.workday.warp.{TestId, TrialResult}
import org.junit.jupiter.api.TestInfo
import org.pmw.tinylog.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * Orchestrates the starting and stopping of MeasurementCollectors configured for this test.
  *
  * The most basic way to use this API is to instantiate [[DefaultMeasurementCollectionController]] directly,
  * however we encourage using either the [[com.workday.warp.junit.Measure]] annotation, or the Scala DSL in
  * [[com.workday.warp.dsl.ExecutionConfig]].
  *
  * Created by tomas.mccandless on 8/18/15.
  * Based on a java class created by michael.ottati on 1/17/14.
  *
  * @param testId fully qualified name of the method being measured.
  * @param tags [[List]] of [[Tag]] that should be persisted during endMeasurementCollection.
  */
abstract class AbstractMeasurementCollectionController(val testId: TestId, val tags: List[Tag]) extends PersistenceAware {


  // boilerplate for java interop
  def this(info: TestInfo, tags: List[Tag]) = this(info.id, tags)
  def this(info: TestInfo) = this(info.id, Nil)

  // scalastyle:off var.field
  /** collectors that will be wrapped around this test. */
  protected var _collectors: List[AbstractMeasurementCollector] = List.empty
  /** @return collectors wrapped around this test. */
  def collectors: List[AbstractMeasurementCollector] = this._collectors

  /** @return all collectors that should be disabled for nested measurements. */
  def intrusiveCollectors: List[AbstractMeasurementCollector] = this._collectors filter { _.isIntrusive }

  /** @return true iff there are any enabled intrusive collectors. */
  def isIntrusive: Boolean = this.intrusiveCollectors.count { _.isEnabled } > 0

  /** arbiters that will impose requirements on this test. */
  protected var _arbiters: List[ArbiterLike] = List.empty
  /** @return arbiters that will impose requirements on this test. */
  def arbiters: List[ArbiterLike] = this._arbiters

  /** time at which this test was invoked */
  protected var timeStarted: Instant = _

  /** whether measurement is currently underway. used to prevent registration of new collectors during measurements */
  protected var _measurementInProgress: Boolean = false
  /** @return whether measurement is currently underway. */
  def measurementInProgress: Boolean = this._measurementInProgress
  // scalastyle:on


  /** @return a List containing only those collectors that are enabled */
  def enabledCollectors: List[AbstractMeasurementCollector] = this._collectors filter { _.isEnabled }

  /** @return a List containing only the arbiters that are enabled */
  def enabledArbiters: List[ArbiterLike] = this._arbiters filter { _.isEnabled }


  /**
    * Determines the schedule of parallel collection.
    *
    * @return a List containing all enabled collectors grouped and sorted by their priority levels
    */
  def collectorSchedule: List[(Int, List[AbstractMeasurementCollector])] = this.enabledCollectors.groupBy(_.priority)
    .toList.sortWith(_._1 > _._1)


  /** Starts all the collectors that are configured. */
  def beginMeasurementCollection(timeStarted: Instant): Unit = {
    this._measurementInProgress = true

    // only perform concurrent measurement collection if collector priorities and concurrent collection are enabled
    this.startCollectors()
    this.timeStarted = timeStarted
  }


  /**
    * Starts all the collectors that are configured.
    *
    * Overloading included purely for java compat.
    */
  def beginMeasurementCollection(): Unit = this.beginMeasurementCollection(Instant.now())


  /**
    * Starts parallel measurement collection.
    *
    * Enabled collectors are grouped by their priorities, and distinct collectors running at the same priority level
    * will be started in parallel.
    */
  def startCollectors(): Unit = {
    Logger.debug(s"starting concurrent measurement collection for ${this.testId}")

    // create a list of tuples of (priority, collector list) for each enabled priority level
    this.collectorSchedule foreach { case (priority: Int, scheduledCollectors: List[AbstractMeasurementCollector]) =>
      Logger.debug(s"starting concurrent collectors for priority level $priority: $scheduledCollectors")

      val measurementTasks: Seq[Future[Unit]] = for (collector <- scheduledCollectors) yield Future {
        collector.tryStartMeasurement()
      }

      if (measurementTasks.nonEmpty) {
        FutureUtils.execute(measurementTasks)
      }
    }
  }


  /**
    * Ends measurement collection. Should be invoked as part of warp service.
    *
    * @param elapsedTime measured response time of the test.
    * @param threshold response time threshold of the measured test.
    * @return a [[TrialResult]] with information about the measured test.
    */
  def endMeasurementCollection(elapsedTime: Duration, threshold: Duration): TrialResult[_] = {
    this.endMeasurementCollection(Try(TrialResult(elapsedTime, threshold)))
  }


  /**
    * Ends measurement collection.
    *
    * Collectors will be stopped in the reverse order they were started. elapsedTime will be recorded as the response
    * time in milliseconds for this test.
    *
    * @param elapsedTime measured response time of the test.
    * @return a [[TrialResult]] with information about the measured test.
    */
  def endMeasurementCollection(elapsedTime: Duration): TrialResult[_] = {
    this.endMeasurementCollection(Try(TrialResult(elapsedTime)))
  }


  /** Ends measurement collection.
    *
    * Collectors will be stopped in the reverse order they were started. response time will be recorded as the Duration
    * for this test and maybeThreshold will be the expected max Duration of the test if Some value.
    *
    * @param responseTime the response time to record for this test.
    * @param maybeThreshold the response time threshold for the measured test.
    * @return a [[TrialResult]] with information about the measured test.
    */
  def endMeasurementCollection(responseTime: Duration, maybeThreshold: Option[Duration]): TrialResult[_] = {
    this.endMeasurementCollection(Try(new TrialResult(
      maybeResponseTime = Option(responseTime),
      maybeThreshold = maybeThreshold
    )))
  }


  /** Ends measurement collection.
    *
    * Collectors will be stopped in the reverse order they were started. The [[TrialResult]] will be created with the default
    * parameters for this test.
    *
    * @return a [[TrialResult]] with information about the measured test.
    */
  def endMeasurementCollection[TrialType](): TrialResult[TrialType] = {
    this.endMeasurementCollection(Try(TrialResult.empty))
  }



  /**
    * Ends measurement collection.
    *
    * Collectors will be stopped in the reverse order they were started. elapsedTime will be recorded as the response
    * time for this test. Results will be recorded only if `tryTrial` is a [[Success]].
    *
    * @param maybeTrial a [[Try]] of type [[TrialResult]] with information about a measured test
    * @return a [[TrialResult]] with information about the measured test or an exception if test failed functionally.
    */
  @throws[RuntimeException]
  @throws[WarpFieldPersistenceException]
  def endMeasurementCollection[TrialType](maybeTrial: Try[TrialResult[TrialType]]): TrialResult[TrialType] = {

    if (!this._measurementInProgress) {
      TrialResult.empty
    }
    else {
      this._measurementInProgress = false

      maybeTrial match {
        case Success(trial) => recordTrial(trial)
        case Failure(exception) =>
          // collectors shouldn't write to the db
          stopCollectors(maybeTestExecution = None)
          throw exception
      }
    }
  }


  /**
    * Finalizes persistence before arbiter votes are evaluated (before we throw any exceptions).
    *
    * Last chance to add any tags, mirror results in a secondary db, etc.
    *
    * Override this in your controllers.
    *
    * @param idTestExecution id of the current test execution.
    */
  def finalizePersistence(idTestExecution: Int): Unit = { }


  /**
    * Handles post-measurement processing (i.e. finalize response time, create TestExecution, stop collectors, collect votes
    * on enabledArbiters).
    *
    * @param trial a [[TrialResult]] with information about a measured test
    * @return a [[TrialResult]] containing the final results from the measured test
    */
  @throws[WarpFieldPersistenceException]
  private def recordTrial[TrialType](trial: TrialResult[TrialType]): TrialResult[TrialType] = {
    val responseTime: Duration = TimeUtils.max(
      // fall back on elapsed wall clock time
      trial.maybeResponseTime.getOrElse(TimeUtils.elapsedTimeSince(this.timeStarted)),
      // verify measurement is non-negative value greater than zero, else default to 1 millisecond
      1.milli
    )

    // if there isn't a threshold set on the trial result already, use what is set on the required or junit5 timeout annotation
    val threshold: Duration = List(
      trial.maybeThreshold,
      AnnotationReader.getRequiredMaxValue(this.testId),
      AnnotationReader.getTimeoutValue(this.testId)
    ).flatten.find(_.isPositive).getOrElse(Duration.ofMillis(-1))

    val maybeTestExecution: Option[TestExecutionRowLike] = Option(
      this.persistenceUtils.createTestExecution(
        this.testId,
        this.timeStarted,
        responseTime.doubleSeconds,
        threshold.doubleSeconds,
        trial.maybeDocumentation
      )
    )

    this.stopCollectors(maybeTestExecution)

    maybeTestExecution match {
      case Some(testExecution) =>
        val ballotBox: Ballot = new Ballot(this.testId)

        // read all tried outer tags
        this.logTagErrors(this.recordTags(this.tags, testExecution))

        this.enabledArbiters foreach {
          _.collectVote(ballotBox, testExecution)
        }

        // finalize any persistence. note that we do this before arbiter votes are evaluated
        this.finalizePersistence(testExecution.idTestExecution)

        // throw an error if we need to
        ballotBox.checkAndThrow()

        trial.copy(maybeResponseTime = Option(responseTime), maybeTestExecution = maybeTestExecution)
      case None =>
        trial.copy(maybeResponseTime = Option(responseTime))
    }
  }

  /**
    * Persist list of tags to database
    *
    * @param tags The list of tags to persist
    * @param testExecution The associated TestExecution
    * @return A list of nested tuples correlating a metatag and outer tag with a Try indicating if it was successfully persisted
    *         List (outerTag, (try OuterTag, List(MetaTag, Try[MetaTag]))
    */
  def recordTags[T: TestExecutionRowLikeType](tags: List[Tag], testExecution: T): List[PersistTagResult] = {
    tags map {
      outerTag =>
        val rowId: Try[Int] = outerTag match {
        case info: DefinitionTag =>
          Try(persistenceUtils.recordTestDefinitionTag(testExecution.idTestDefinition, info.key, info.value,
                                                     info.isUserGenerated).idTestDefinitionTag)
        case trial: ExecutionTag =>
          Try(persistenceUtils.recordTestExecutionTag(testExecution.idTestExecution, trial.key, trial.value,
                                                 trial.isUserGenerated).idTestExecutionTag)
        }

        // if rowId succeeds, record success and begin reading metatags. If fail, log that all the metatags will have
        // also failed since there won't be a matching outerTag rowID.
        val tryOuterTag: (Try[Tag], List[PersistMetaTagResult]) = rowId match {
          case Success(id) => (Success(outerTag), recordMetaTags(outerTag.metaTags, id))
          case Failure(exception) =>
            (Failure(exception), outerTag.metaTags map {
              metaTag => PersistMetaTagResult(metaTag, Failure(new WarpFieldPersistenceException(
                                                s"$metaTag failed to persist since accompanying $outerTag insertion failed",
                                                exception)))
            })
        }

        PersistTagResult(outerTag, tryOuterTag)
    }
  }

  /**
    * Persist list of meta tags to database
    *
    * @param metaTags The list of metatags to persist
    * @param testExecutionTagId The ID of the associated TestExecutionRow
    * @return A list of tuples correlating a tag with a Try indicating if it was successfully persisted
    */
  def recordMetaTags(metaTags: List[MetaTag], testExecutionTagId: Int): List[PersistMetaTagResult] = {
    metaTags map {
      metaTag =>
        val triedMetaTag = Try {
        metaTag match {
          case info: DefinitionMetaTag => persistenceUtils.recordTestDefinitionMetaTag(testExecutionTagId, info.key, info.value,
                                                                         info.isUserGenerated)
          case trial: ExecutionMetaTag => persistenceUtils.recordTestExecutionMetaTag(testExecutionTagId, trial.key, trial.value,
                                                                            trial.isUserGenerated)
        }
      } flatMap { _ => Success(metaTag) }
        PersistMetaTagResult(metaTag, triedMetaTag)
    }
  }


  /**
    * Logs any error messages resulting from attempted tag persistence.
    *
    * @param results
    */
  private def logTagErrors(results: Seq[PersistTagResult]): Unit = {
    results foreach {
      case PersistTagResult(outerTag, tryOuterTag) =>
        tryOuterTag match {
          case (Success(_), _) =>
            Logger.debug(s"OuterTag $outerTag persisted... testing MetaTags")
          case (Failure(pte: WarpFieldPersistenceException), _) =>
            throw pte
          case (Failure(exception), _) =>
            Logger.error(s"OuterTag $outerTag failed to persist with exception: $exception")
        }

        // loop on metatags
        tryOuterTag._2 foreach {
          case PersistMetaTagResult(metaTag, triedMetaTag) =>
            triedMetaTag match {
              case Success(_) => Logger.debug(s"MetaTag $metaTag persisted")
              case Failure(wfpe: WarpFieldPersistenceException) => throw wfpe
              case Failure(exception) => Logger.error(s"MetaTag $metaTag failed to persist with exception: $exception")
            }
        }
    }
  }


  /**
    * Stops currently active collectors.
    *
    * @param maybeTestExecution an [[Option]] of type [[TestExecutionRowLike]]
    */
  private def stopCollectors(maybeTestExecution: Option[TestExecutionRowLike]): Unit = {
    Logger.debug(s"stopping concurrent measurement collection for ${this.testId}")

    // create a list of tuples of (priority, collector list) for each enabled priority level
    this.collectorSchedule.reverse foreach { case (priority: Int, scheduledCollectors: List[AbstractMeasurementCollector]) =>
      Logger.debug(s"stopping concurrent collectors for priority level $priority: $scheduledCollectors")

      val measurementTasks: Seq[Future[Unit]] = for (collector <- scheduledCollectors) yield Future {
        collector.tryStopMeasurement(maybeTestExecution)
      }

      if (measurementTasks.nonEmpty) {
        FutureUtils.execute(measurementTasks)
      }
    }
  }


  /** Disables all arbiters used by this controller */
  def disableArbiters(): Unit = this._arbiters foreach { _.isEnabled = false }


  /** Disables all collectors used by this controller. */
  def disableCollectors(): Unit = this._collectors foreach { _.isEnabled = false }


  /** Disables all intrusive collectors. should be called before starting any nested measurements. */
  def disableIntrusiveCollectors(): Unit = this.intrusiveCollectors foreach { _.isEnabled = false }


  /**
    * Registers a new [[ArbiterLike]] for this test.
    *
    * If measurement has already begun, has no effect. Should only be called before calling beginMeasurementCollection
    *
    * @param arbiter the [[ArbiterLike]] to register.
    * @return true iff registration was successful.
    */
  def registerArbiter(arbiter: ArbiterLike): Boolean = {
    this.synchronized {
      if (this._measurementInProgress) {
        Logger.warn(s"measurement in progress. arbiter ${arbiter.getClass.getCanonicalName} will not be registered.")
        false
      }
      else {
        this._arbiters = arbiter :: this._arbiters
        true
      }
    }
  }


  /**
    * Registers each arbiter in `arbiters`.
    *
    * @param arbiters instances of [[ArbiterLike]] to register.
    * @return [[Iterable]] containing the result of registering each [[ArbiterLike]]
    */
  def registerArbiters(arbiters: Iterable[ArbiterLike]): Iterable[Boolean] = {
    this.synchronized {
      arbiters map this.registerArbiter
    }
  }


  /** Registers a new collector to measure this test.
    *
    * If measurement has already begun, has no effect. Should only be called before calling beginMeasurementCollection
    *
    * @param collector the [[AbstractMeasurementCollector]] to register.
    * @return true iff collector registration was successful.
    */
  // TODO consider changing this return type to unit
  def registerCollector(collector: AbstractMeasurementCollector): Boolean = {
    if (this._measurementInProgress) {
      Logger.warn(s"measurement in progress. collector ${collector.getClass.getCanonicalName} will not be registered.")
      false
    }
    else {
      Try(this._collectors = collector :: this._collectors).isSuccess
    }
  }


  /**
    * Registers each collector in `collectors`.
    *
    * @param collectors instances of [[AbstractMeasurementCollector]] to register.
    * @return [[Iterable]] containing the result of registering each [[AbstractMeasurementCollector]].
    */
  def registerCollectors(collectors: Iterable[AbstractMeasurementCollector]): Iterable[Boolean] = {
    collectors map this.registerCollector
  }
}
