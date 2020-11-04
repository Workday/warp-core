package com.workday.warp.arbiters

import java.io.InputStream
import java.sql.Timestamp
import java.time.{Instant, LocalDate}
import java.util.UUID

import com.workday.telemetron.RequirementViolationException
import com.workday.warp.persistence.exception.WarpFieldPersistenceException
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.common.CoreWarpProperty._
import com.workday.warp.common.CoreConstants
import com.workday.warp.junit.UnitTest
import com.workday.warp.persistence.CorePersistenceAware
import com.workday.warp.persistence.TablesLike._
import com.workday.warp.persistence.TablesLike.RowTypeClasses._
import com.workday.warp.utils.Ballot
import org.junit.jupiter.api.parallel.Isolated
import org.pmw.tinylog.Logger

import scala.io.Source
import scala.util.Random

/**
  * Created by tomas.mccandless on 9/13/16.
  */
@Isolated
class SmartNumberArbiterSpec extends WarpJUnitSpec with CorePersistenceAware {

  import SmartNumberArbiterSpec.{createDummyTestExecutions, persistDummyTestExecution}

  /** test behavior of smartNumber calculation, and also the effect of tolerance factor on the resultant threshold. */
  @UnitTest
  def testSmartNumberBehavior(): Unit = {
    val arbiter: SmartNumberArbiter = new SmartNumberArbiter
    // create 1000 gaussian numbers with mean 50
    val random: Random = new Random(seed = 123456)
    val responseTimes: List[Double] = List.fill(1000)(50 + (random.nextGaussian * 4))
    val smartNumber: Double = arbiter.smartNumber(responseTimes)
    Logger.debug(s"detected smart number: $smartNumber")

    arbiter.isAnomaly(responseTimes, smartNumber) should be (true)
    arbiter.isAnomaly(responseTimes, smartNumber * .95) should be (false)

    // with high tolerance factor
    val arbiterWithHighToleranceFactor: SmartNumberArbiter = new SmartNumberArbiter(toleranceFactor = 4.0)
    val smartNumberWithHighTolerance: Double = arbiterWithHighToleranceFactor.smartNumber(responseTimes)
    val incomingResponseTime: Double = 65.0
    Logger.debug(s"smart number with high tolerance: $smartNumberWithHighTolerance")

    arbiter.isAnomaly(responseTimes, incomingResponseTime) should be (true)
    arbiterWithHighToleranceFactor.isAnomaly(responseTimes, incomingResponseTime) should be (false)
  }

  /** test behavior of SmartNumberArbiter on test executions with response time pattern as such:
    *                              --------- ~1000.0
    * ~50.0 ----------------------
    *
    * given some baseline date denoting the new behavior, we expect the threshold to adjust to the new plateau, such that
    * a test execution of around 1000 ms would be flagged as odd when including all the data, but then not be flagged when we
    * exclude the response times ~52ms.
    */
  @UnitTest
  def usesStartDateLowerBound(): Unit = {
    // We need a unique testID so that we don't generate more than 30 datapoints after the cutoff date
    // (if this test is run multiple times in a row without clearing the database)
    val testID: String = "a.b.c.d.e." + UUID.randomUUID().toString
    val baselineDate: LocalDate = LocalDate.now().minusWeeks(1)
    val someDateBeforeCutoff: Instant = Timestamp.valueOf("1980-01-01 00:00:00").toInstant
    val someDateAfterCutoff: Instant = Instant.now()

    // create 90 test executions before date cutoff at ~50ms response time, then 10 test executions after cutoff at ~1000 ms
    for(_ <- 1 to 90) {
      this.persistenceUtils.createTestExecution(testID, someDateBeforeCutoff, 50 + (Random.nextGaussian * 4), 10000.0)
    }
    for(_ <- 1 to 10) {
      this.persistenceUtils.createTestExecution(testID, someDateAfterCutoff, 1000 + (Random.nextGaussian * 4), 10000.0)
    }

    // Create a test execution that occurs after the baseline date with some large, anomalous response time
    val incomingTestExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(
      testID,
      someDateAfterCutoff,
      1000.0,
      10000.0
    )
    this.persistenceUtils.recordTestExecutionTag(
      incomingTestExecution.idTestExecution,
      CoreConstants.WARP_SPECIFICATION_FIELDS_STRING,
      value = "",
      isUserGenerated = false
    )

    // This arbiter uses all historical data (all 100 total points). The newest test execution will be flagged as anomalous.
    val allResponseTimesArbiter: SmartNumberArbiter = new SmartNumberArbiter
    allResponseTimesArbiter.vote(new Ballot(testID), incomingTestExecution).get shouldBe a[RequirementViolationException]

    // This arbiter only uses data after the baseline date (only 10 points). There is not enough data to vote,
    // so it should not throw any exceptions.
    val arbiterWithDateBoundary: SmartNumberArbiter = new SmartNumberArbiter(startDateLowerBound = baselineDate)
    arbiterWithDateBoundary.vote(new Ballot(testID), incomingTestExecution) should be (None)
  }

  /**
    * Create 100 data points. 70 with 500ms response time and 30 with 100ms
    * Uses a sliding window size of 100, so the latest test execution with a
    * response time of 600ms should NOT be flagged as an anomaly
    */
  @UnitTest
  def usesLongSlidingWindow(): Unit = {
    val testID: String = "f.g.h.i.j." + UUID.randomUUID().toString
    val allResponseTimes: Iterable[Double] =
      createDummyTestExecutions(testID, 70, 500) ++ createDummyTestExecutions(testID, 30, 100)

    val incomingTestExecution: TestExecutionRowLike = persistDummyTestExecution(testID, 600)
    val slidingWindowArbiter: SmartNumberArbiter = new SmartNumberArbiter(useSlidingWindow = true, slidingWindowSize = 100)
    val windowSmartNumber: Double = slidingWindowArbiter.smartNumber(allResponseTimes takeRight slidingWindowArbiter.slidingWindowSize)
    val allResponseTimesSmartNumber: Double = slidingWindowArbiter.smartNumber(allResponseTimes)

    Logger.debug(s"detected sliding window size: ${slidingWindowArbiter.slidingWindowSize}")
    Logger.debug(s"sliding window smart number: $windowSmartNumber")
    Logger.debug(s"all response times smart number: $allResponseTimesSmartNumber")

    // 600ms is not anomalous if considering all 100 data points
    slidingWindowArbiter.vote(new Ballot(testID), incomingTestExecution) should be (None)
  }

  /**
    * Create 100 data points. 70 with 350ms response time and 30 with 40ms
    * Uses a sliding window size of 30, so the latest test execution with a
    * response time of 45ms should NOT be flagged as an anomaly
    */
  @UnitTest
  def usesShortSlidingWindow(): Unit = {
    val testID: String = "k.l.m.n.o." + UUID.randomUUID().toString
    val allResponseTimes: Iterable[Double] =
      createDummyTestExecutions(testID, 70, 350) ++ createDummyTestExecutions(testID, 30, 40)

    val incomingTestExecution: TestExecutionRowLike = persistDummyTestExecution(testID, 42)
    val slidingWindowArbiter: SmartNumberArbiter = new SmartNumberArbiter(useSlidingWindow = true, slidingWindowSize = 30)
    val windowSmartNumber: Double = slidingWindowArbiter.smartNumber(allResponseTimes takeRight slidingWindowArbiter.slidingWindowSize)
    val allResponseTimesSmartNumber: Double = slidingWindowArbiter.smartNumber(allResponseTimes)

    Logger.debug(s"detected sliding window size: ${slidingWindowArbiter.slidingWindowSize}")
    Logger.debug(s"sliding window smart number: $windowSmartNumber")
    Logger.debug(s"all response times smart number: $allResponseTimesSmartNumber")

    // 42ms is not an anomaly
    slidingWindowArbiter.vote(new Ballot(testID), incomingTestExecution) should be (None)
  }

  /**
    * Create 100 data points. 70 with 500ms response time and 30 with 50ms
    * Uses a sliding window size of 30, so the latest test execution with a
    * response time of 500ms should be flagged as an anomaly
    */
  @UnitTest
  def usesShortSlidingWindowWithAnomaly(): Unit = {
    val testID: String = "p.q.r.s.t." + UUID.randomUUID().toString
    val allResponseTimes: Iterable[Double] =
      createDummyTestExecutions(testID, 70, 500) ++ createDummyTestExecutions(testID, 30, 50)

    val incomingTestExecution: TestExecutionRowLike = persistDummyTestExecution(testID, 500)
    val slidingWindowArbiter: SmartNumberArbiter = new SmartNumberArbiter(useSlidingWindow = true, slidingWindowSize = 30)
    val windowSmartNumber: Double = slidingWindowArbiter.smartNumber(allResponseTimes takeRight slidingWindowArbiter.slidingWindowSize)
    val allResponseTimesSmartNumber: Double = slidingWindowArbiter.smartNumber(allResponseTimes)

    Logger.debug(s"detected sliding window size: ${slidingWindowArbiter.slidingWindowSize}")
    Logger.debug(s"sliding window smart number: $windowSmartNumber")
    Logger.debug(s"all response times smart number: $allResponseTimesSmartNumber")

    // 500ms is anomalous if only considering last 30 data points
    slidingWindowArbiter.vote(new Ballot(testID), incomingTestExecution).get shouldBe a[RequirementViolationException]
  }

  /**
    * Create 100 data points with 5 having anomalous response times
    */
  @UnitTest
  def usesDoubleRpca(): Unit = {
    val testID: String = "u.v.w.x.y." + UUID.randomUUID().toString
    val allResponseTimes: Iterable[Double] = createDummyTestExecutions(testID, 75, 50) ++
        createDummyTestExecutions(testID, 5, 125) ++
        createDummyTestExecutions(testID, 20, 50)

    val incomingTestExecution: TestExecutionRowLike = persistDummyTestExecution(testID, 50)
    val doubleRpcaArbiter: SmartNumberArbiter = new SmartNumberArbiter(useDoubleRpca = true)
    doubleRpcaArbiter.isAnomaly(allResponseTimes, 125) should be (true)
    doubleRpcaArbiter.vote(new Ballot(testID), incomingTestExecution) should be (None)
  }


  /**
   * Checks that the smart threshold is persisted
   */
  @UnitTest
  def persistSmartThresholdMetaTag(): Unit = {
    val testId: String = this.getClass.getCanonicalName + "." + UUID.randomUUID().toString

    val ballot: Ballot = new Ballot(testId)

    // Create historical values to read
    for (_ <- 1 to WARP_ANOMALY_RPCA_MINIMUM_N.value.toInt) {
      this.persistenceUtils.createTestExecution(testId, Instant.now(), 50 + (Random.nextGaussian * 4), 10000.0)
    }

    // Create a test execution
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(testId, Instant.now(), 5.0, 6.0)
    val arbiter: SmartNumberArbiter = new SmartNumberArbiter

    // before the test is added to TestExecutionTag table
    arbiter.vote(ballot, testExecution).get shouldBe a[WarpFieldPersistenceException]

    // test execution added, persistence should succeed
    val testExecutionTagId: Int = this.persistenceUtils.recordTestExecutionTag(
      testExecution.idTestExecution,
      CoreConstants.WARP_SPECIFICATION_FIELDS_STRING,
      value = "",
      isUserGenerated = false
    ).idTestExecutionTag

    arbiter.vote(ballot, testExecution) should be (None)
    arbiter.voteAndThrow(ballot, testExecution)
    val tagDescriptionId: Int = this.persistenceUtils.getTagName(CoreConstants.SMART_THRESHOLD_STRING).idTagName
    this.persistenceUtils.synchronously(
      this.persistenceUtils.testExecutionMetaTagQuery(testExecutionTagId, tagDescriptionId)
    ).nonEmpty should be (true)
  }

  /**
   * Checks that smartNumber returns -1 instead of throwing an exception when there is no historical data
   */
  @UnitTest
  def smartNumberNoHistoricalData(): Unit = {
    val arbiter: SmartNumberArbiter = new SmartNumberArbiter
    // No historical response times
    val responseTimes: List[Double] = List.empty

    // smartNumber will return -1 if there are not enough response times
    val smartNumber: Double = arbiter.smartNumber(responseTimes)
    smartNumber should equal (-1)
  }


  /**
    * Checks that our smart threshold tightly follows a decreasing trend, and loosely follows an increasing trend.
    */
  @UnitTest
  def correctBehavior(): Unit = {

    /**
      * Convenience method for reading a resource file as a time series.
      */
    def readData(resourceName: String): List[Double] = {
      val stream: InputStream = this.getClass.getResourceAsStream(resourceName)
      Source.fromInputStream(stream).getLines().map(_.toDouble).toList
    }


    val arbiter: SmartNumberArbiter = new SmartNumberArbiter()

    val responseTimes: List[Double] = readData("/rpca_sample_data.txt")
    arbiter.smartNumber(responseTimes) should be(85.0 +- 1.0)
    arbiter.smartNumber(responseTimes.reverse) should be(249.0 +- 1.0)

    val responseTimes2: List[Double] = readData("/rpca_sample_data2.txt")
    arbiter.smartNumber(responseTimes2) should be(505.0 +- 5.0)
    arbiter.smartNumber(responseTimes2.reverse) should be(1233.0 +- 5.0)
  }
}


object SmartNumberArbiterSpec extends CorePersistenceAware {

  /**
    * Helper method to create latest dummy test execution and persist it to the database
    */
  def persistDummyTestExecution(testID: String, responseTime: Int): TestExecutionRowLike = {
    val incomingTestExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(
      testID,
      Instant.now(),
      responseTime,
      maxResponseTime = 10000.0
    )
    this.persistenceUtils.recordTestExecutionTag(
      incomingTestExecution.idTestExecution,
      CoreConstants.WARP_SPECIFICATION_FIELDS_STRING,
      value = "",
      isUserGenerated = false
    )

    incomingTestExecution
  }

  /**
    * Helper method to create a range of dummy test executions
    */
  def createDummyTestExecutions(testID: String,
                                range: Int,
                                responseTime: Int): Iterable[Double] = {
    val responseTimes: Iterable[Double] = for (_ <- 1 to range) yield {
      this.persistenceUtils.createTestExecution(testID, Instant.now(), responseTime + (Random.nextGaussian * 4), 10000.0).responseTime
    }

    responseTimes
  }
}