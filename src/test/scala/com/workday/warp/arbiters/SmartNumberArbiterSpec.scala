package com.workday.warp.arbiters

import java.util
import java.sql.Timestamp
import java.time.LocalDate
import java.util.UUID

import com.workday.telemetron.RequirementViolationException
import com.workday.warp.persistence.exception.WarpFieldPersistenceException
import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.common.CoreWarpProperty._
import com.workday.warp.common.CoreConstants
import com.workday.warp.persistence.CorePersistenceAware
import com.workday.warp.persistence.TablesLike._
import com.workday.warp.persistence.TablesLike.RowTypeClasses._
import com.workday.warp.utils.Ballot
import org.junit.Test
import org.junit.experimental.categories.Category
import org.pmw.tinylog.Logger

import scala.util.Random

/**
  * Created by tomas.mccandless on 9/13/16.
  */
class SmartNumberArbiterSpec extends WarpJUnitSpec with CorePersistenceAware {

  import SmartNumberArbiterSpec.{createDummyTestExecutions, persistDummyTestExecution}

  /** test behavior of smartNumber calculation, and also the effect of tolerance factor on the resultant threshold. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def testSmartNumberBehavior(): Unit = {
    val arbiter: SmartNumberArbiter = new SmartNumberArbiter
    // create 1000 gaussian numbers with mean 50
    val responseTimes: List[Double] = List.fill(1000)(50 + (Random.nextGaussian * 4))
    val smartNumber: Double = arbiter.smartNumber(responseTimes)
    Logger.info(s"detected smart number: $smartNumber")

    arbiter.isAnomaly(responseTimes, smartNumber) should be (true)
    arbiter.isAnomaly(responseTimes, smartNumber * .95) should be (false)

    // with high tolerance factor
    val arbiterWithHighToleranceFactor: SmartNumberArbiter = new SmartNumberArbiter(toleranceFactor = 4.0)
    val smartNumberWithHighTolerance: Double = arbiterWithHighToleranceFactor.smartNumber(responseTimes)
    val incomingResponseTime: Double = 65.0
    Logger.info(s"smart number with high tolerance: $smartNumberWithHighTolerance")

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
  @Test
  @Category(Array(classOf[UnitTest]))
  def usesStartDateLowerBound(): Unit = {
    // We need a unique testID so that we don't generate more than 30 datapoints after the cutoff date
    // (if this test is run multiple times in a row without clearing the database)
    val testID: String = "a.b.c.d.e." + UUID.randomUUID().toString
    val baselineDate: LocalDate = LocalDate.now().minusWeeks(1)
    val someDateBeforeCutoff: Timestamp = Timestamp.valueOf("1980-01-01 00:00:00")
    val someDateAfterCutoff: Timestamp = new Timestamp(System.currentTimeMillis())

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
    * Create 100 data points. 70 with 500ms response time and 30 with 50ms
    * Uses a sliding window size of 100, so the latest test execution with a
    * response time of 600ms should NOT be flagged as an anomaly
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def usesLongSlidingWindow(): Unit = {
    val testID: String = "f.g.h.i.j." + UUID.randomUUID().toString
    val allResponseTimes: Iterable[Double] =
      createDummyTestExecutions(testID, 70, 500) ++ createDummyTestExecutions(testID, 30, 50)

    val incomingTestExecution: TestExecutionRowLike = persistDummyTestExecution(testID, 600)
    val slidingWindowArbiter: SmartNumberArbiter = new SmartNumberArbiter(useSlidingWindow = true, slidingWindowSize = 100)
    val windowSmartNumber: Double = slidingWindowArbiter.smartNumber(allResponseTimes takeRight slidingWindowArbiter.slidingWindowSize)
    val allResponseTimesSmartNumber: Double = slidingWindowArbiter.smartNumber(allResponseTimes)

    Logger.info(s"detected sliding window size: ${slidingWindowArbiter.slidingWindowSize}")
    Logger.info(s"sliding window smart number: $windowSmartNumber")
    Logger.info(s"all response times smart number: $allResponseTimesSmartNumber")

    // 600ms is not anomalous if considering all 100 data points
    slidingWindowArbiter.vote(new Ballot(testID), incomingTestExecution) should be (None)
  }

  /**
    * Create 100 data points. 70 with 350ms response time and 30 with 40ms
    * Uses a sliding window size of 30, so the latest test execution with a
    * response time of 45ms should NOT be flagged as an anomaly
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def usesShortSlidingWindow(): Unit = {
    val testID: String = "k.l.m.n.o." + UUID.randomUUID().toString
    val allResponseTimes: Iterable[Double] =
      createDummyTestExecutions(testID, 70, 350) ++ createDummyTestExecutions(testID, 30, 40)

    val incomingTestExecution: TestExecutionRowLike = persistDummyTestExecution(testID, 45)
    val slidingWindowArbiter: SmartNumberArbiter = new SmartNumberArbiter(useSlidingWindow = true, slidingWindowSize = 30)
    val windowSmartNumber: Double = slidingWindowArbiter.smartNumber(allResponseTimes takeRight slidingWindowArbiter.slidingWindowSize)
    val allResponseTimesSmartNumber: Double = slidingWindowArbiter.smartNumber(allResponseTimes)

    Logger.info(s"detected sliding window size: ${slidingWindowArbiter.slidingWindowSize}")
    Logger.info(s"sliding window smart number: $windowSmartNumber")
    Logger.info(s"all response times smart number: $allResponseTimesSmartNumber")

    // 45ms is not an anomaly
    slidingWindowArbiter.vote(new Ballot(testID), incomingTestExecution) should be (None)
  }

  /**
    * Create 100 data points. 70 with 500ms response time and 30 with 50ms
    * Uses a sliding window size of 30, so the latest test execution with a
    * response time of 500ms should be flagged as an anomaly
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def usesShortSlidingWindowWithAnomaly(): Unit = {
    val testID: String = "p.q.r.s.t." + UUID.randomUUID().toString
    val allResponseTimes: Iterable[Double] =
      createDummyTestExecutions(testID, 70, 500) ++ createDummyTestExecutions(testID, 30, 50)

    val incomingTestExecution: TestExecutionRowLike = persistDummyTestExecution(testID, 500)
    val slidingWindowArbiter: SmartNumberArbiter = new SmartNumberArbiter(useSlidingWindow = true, slidingWindowSize = 30)
    val windowSmartNumber: Double = slidingWindowArbiter.smartNumber(allResponseTimes takeRight slidingWindowArbiter.slidingWindowSize)
    val allResponseTimesSmartNumber: Double = slidingWindowArbiter.smartNumber(allResponseTimes)

    Logger.info(s"detected sliding window size: ${slidingWindowArbiter.slidingWindowSize}")
    Logger.info(s"sliding window smart number: $windowSmartNumber")
    Logger.info(s"all response times smart number: $allResponseTimesSmartNumber")

    // 500ms is anomalous if only considering last 30 data points
    slidingWindowArbiter.vote(new Ballot(testID), incomingTestExecution).get shouldBe a[RequirementViolationException]
  }

  /**
    * Create 100 data points with 5 having anomalous response times
    */
  @Test
  @Category(Array(classOf[UnitTest]))
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
  @Test
  @Category(Array(classOf[UnitTest]))
  def persistSmartThresholdMetaTag(): Unit = {

    val ballot: Ballot = new Ballot(this.getTestId)

    // Create historical values to read
    for (_ <- 1 to WARP_ANOMALY_RPCA_MINIMUM_N.value.toInt) {
      this.persistenceUtils.createTestExecution(this.getTestId, new util.Date, 50 + (Random.nextGaussian * 4), 10000.0)
    }

    // Create a test execution
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(this.getTestId, new util.Date, 5.0, 6.0)
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
  @Test
  @Category(Array(classOf[UnitTest]))
  def smartNumberNoHistoricalData(): Unit = {
    val arbiter: SmartNumberArbiter = new SmartNumberArbiter
    // No historical response times
    val responseTimes: List[Double] = List.empty

    // smartNumber will return -1 if there are not enough response times
    val smartNumber: Double = arbiter.smartNumber(responseTimes)
    smartNumber should equal (-1)
  }



  @Test
  @Category(Array(classOf[UnitTest]))
  def incorrectBehavior(): Unit = {

    val arbiter: SmartNumberArbiter = new SmartNumberArbiter(useSlidingWindow = true)

    val decreasingResponseTimes: List[Double] = List.fill(50)(100.0) ++ (30 to 100).reverse.toList.map(_.toDouble)
    val increasingResponseTimes: List[Double] = List.fill(50)(100.0) ++ (100 to 170).map(_.toDouble)

//    val smartThresholds = responseTimes.inits.toList.reverse map arbiter.smartNumber

//    Logger.info(smartThresholds)

    val decreasingSmartThreshold = arbiter.smartNumber(decreasingResponseTimes)
    Logger.info(s"smart threshold for decreasing response times: $decreasingSmartThreshold")
    val increasingSmartThreshold = arbiter.smartNumber(increasingResponseTimes)
    Logger.info(s"smart threshold for increasing response times: $increasingSmartThreshold")



    val responseTimes: List[Double] = List(
      173.630,
      141.558,
      143.626,
      151.638,
      183.704,
      171.685,
      163.693,
      161.615,
      203.949,
      201.883,
      203.350,
      181.654,
      163.232,
      151.602,
      183.438,
      161.718,
      183.347,
      181.730,
      172.526,
      172.187,
      173.282,
      171.672,
      183.383,
      191.793,
      173.265,
      171.722,
      173.231,
      171.675,
      183.327,
      171.652,
      183.295,
      181.617,
      173.412,
      171.674,
      182.341,
      162.080,
      173.303,
      171.625,
      292.847,
      281.896,
      162.465,
      232.197,
      233.450,
      231.771,
      173.467,
      171.641,
      173.430,
      171.700,
      172.692,
      171.638,
      173.288,
      171.733,
      173.312,
      171.743,
      173.077,
      171.673,
      183.256,
      171.747,
      173.296,
      171.724,
      173.594,
      171.711,
      183.696,
      171.753,
      173.688,
      171.972,
      173.463,
      171.748,
      173.476,
      171.691,
      173.699,
      171.794,
      173.843,
      171.868,
      183.746,
      171.861,
      183.784,
      171.792,
      173.713,
      181.872,
      183.721,
      181.950,
      173.744,
      171.873,
      193.672,
      191.915,
      183.767,
      181.847,
      183.718,
      181.881,
      183.589,
      171.854,
      183.646,
      171.805,
      173.771,
      181.847,
      173.520,
      181.900,
      173.656,
      171.790,
      173.655,
      171.921,
      183.624,
      181.877,
      182.626,
      182.146,
      183.589,
      181.873,
      183.730,
      181.894,
      183.648,
      181.833,
      193.685,
      181.919,
      184.431,
      181.932,
      184.691,
      181.906,
      184.541,
      191.883,
      184.499,
      181.832,
      184.640,
      171.811,
      184.676,
      181.861,
      184.534,
      181.879,
      184.531,
      181.834,
      184.603,
      181.967,
      184.511,
      181.915,
      184.803,
      171.910,
      184.473,
      182.007,
      174.599,
      181.937,
      184.543,
      181.885,
      184.670,
      181.910,
      184.613,
      181.971,
      184.487,
      181.939,
      184.312,
      171.698,
      184.446,
      171.994,
      174.377,
      171.740,
      174.297,
      174.446,
      174.492,
      171.818,
      174.384,
      171.875,
      174.418,
      171.720,
      184.300,
      171.684,
      174.345,
      171.700,
      174.387,
      181.694,
      184.732,
      181.740,
      174.444,
      171.818,
      176.446,
      181.773,
      174.540,
      171.768,
      185.618,
      182.127,
      185.713,
      182.232,
      185.485,
      182.090,
      185.490,
      182.123,
      185.645,
      182.133,
      185.744,
      182.204,
      195.534,
      182.184,
      185.550,
      182.191,
      185.629,
      182.121,
      195.690,
      182.190,
      187.255,
      182.195,
      185.648,
      182.179,
      185.541,
      182.234,
      185.558,
      182.217,
      195.633,
      182.222,
      185.544,
      182.193,
      195.692,
      182.170,
      185.877,
      182.180,
      185.763,
      182.174,
      185.684,
      182.160,
      185.596,
      182.139,
      205.591,
      182.157,
      185.944,
      182.456,
      195.679,
      182.198,
      185.564,
      182.696,
      185.487,
      182.178,
      185.604,
      182.209,
      185.652,
      182.140,
      185.621,
      182.209,
      172.727,
      172.221,
      185.914,
      182.604,
      185.719,
      182.214,
      195.961,
      182.231,
      184.550,
      182.146,
      185.720,
      182.209,
      195.643,
      182.197,
      185.778,
      182.272,
      185.636,
      182.195,
      185.495,
      182.340,
      185.618,
      182.236,
      172.791,
      172.198,
      194.603,
      182.103,
      185.805,
      182.284,
      195.825,
      182.204,
      185.574,
      182.216,
      185.631,
      182.215,
      185.555,
      182.182,
      185.650,
      182.151,
      185.586,
      182.159,
      185.881,
      182.201,
      185.548,
      182.193,
      185.685,
      182.201,
      172.645,
      172.197,
      172.579,
      172.290,
      185.456,
      182.279,
      172.475,
      172.068,
      185.769,
      182.195,
      185.994,
      182.230,
      182.624,
      172.085,
      185.536,
      182.161,
      185.619,
      162.113,
      195.662,
      182.231,
      185.593,
      192.209,
      196.136,
      192.204,
      185.613,
      182.137,
      172.733,
      182.162,
      172.666,
      172.109,
      195.850,
      192.223,
      172.507,
      171.999,
      185.503,
      182.143,
      185.540,
      192.219,
      185.484,
      182.516,
      185.667,
      182.164,
      185.722,
      182.161,
      195.795,
      182.184,
      183.524,
      182.875,
      185.531,
      182.184,
      185.399,
      182.128,
      185.502,
      192.200,
      185.667,
      182.264,
      185.864,
      182.371,
      186.121,
      182.179,
      182.571,
      182.062,
      182.536,
      172.125,
      196.218,
      182.144,
      186.139,
      182.171,
      173.477,
      172.553,
      182.761,
      172.309,
      186.164,
      182.168,
      186.307,
      182.173,
      186.496,
      182.405,
      196.445,
      192.353,
      186.292,
      192.152,
      65.918,
      61.723,
      75.952,
      61.699,
      76.009,
      71.798,
      73.087,
      71.731,
      72.546,
      71.854,
      75.949,
      71.733,
      72.264,
      71.711,
      75.680,
      71.744,
      75.827,
      71.712,
      73.114,
      71.777,
      75.925,
      71.752,
      75.957,
      71.718,
      75.744,
      71.753,
      72.215,
      71.741,
      75.912,
      71.766,
      75.963,
      71.729,
      72.284,
      71.762,
      82.504,
      71.648,
      82.403,
      61.612,
      92.955,
      71.615,
      82.322,
      71.786,
      82.132,
      71.625,
      72.134,
      71.660,
      72.081,
      71.674,
      72.247,
      71.689,
      75.435,
      71.298,
      65.240,
      71.248,
      75.230,
      91.316,
      75.296,
      71.295,
      75.486,
      81.356,
      75.394,
      71.365,
      75.448,
      71.284,
      75.432,
      71.338,
      75.358,
      71.325,
      75.424,
      71.348,
      75.604,
      71.309,
      75.440,
      91.388,
      65.394,
      71.341,
      65.603,
      61.330,
      65.328,
      61.284,
      65.141,
      61.252,
      65.353,
      61.249,
      65.238,
      61.261,
      65.255,
      61.211,
      65.251,
      61.222,
      65.293,
      61.274,
      65.181,
      61.240,
      61.929,
      61.456,
      65.283,
      61.273,
      65.313,
      61.296,
      65.550,
      61.332,
      62.450,
      61.648,
      62.354,
      61.867,
      63.026,
      61.826,
      65.371,
      61.276,
      65.350,
      61.291,
      65.343,
      61.293,
      65.393,
      71.338,
      65.303,
      61.287,
      65.366,
      71.320,
      65.438,
      61.282,
      65.518,
      71.363,
      65.501,
      61.288,
      65.386,
      61.312,
      75.386,
      71.315,
      75.400,
      71.437,
      74.995,
      71.298,
      74.926,
      71.299,
      65.160,
      71.289,
      75.196,
      71.310,
      64.930,
      71.368,
      74.808,
      71.304,
      65.033,
      71.279,
      75.066,
      71.297,
      75.164,
      71.331,
      75.176,
      71.314,
      65.195,
      71.331,
      75.108,
      71.283,
      75.084,
      71.298,
      75.072,
      71.308,
      72.742,
      71.867,
      75.218,
      71.297,
      75.076,
      71.323,
      65.282,
      71.297,
      75.113,
      71.302,
      74.026,
      61.986,
      65.229,
      71.296,
      75.129,
      71.330,
      75.232,
      71.295,
      75.157,
      71.357,
      74.973,
      71.330,
      74.984,
      71.295,
      74.904,
      71.327,
      75.497,
      71.332,
      75.579,
      71.348,
      75.462,
      71.407,
      75.853,
      71.380,
      75.651,
      71.368,
      75.774,
      71.316,
      75.285,
      71.317,
      75.202,
      71.306,
      75.182,
      71.288
    ).reverse

    val smartThresholds: List[Double] = responseTimes.inits.toList.reverse map arbiter.smartNumber

    import breeze.plot._

    val f = Figure()
    val p = f.subplot(0)

    p += plot((1 to responseTimes.length).map(_.toDouble), responseTimes, name = "response times")

    val numInvalid = smartThresholds.count(_ < 0.0)
    p += plot(
      (1 to smartThresholds.length).drop(numInvalid).map(_.toDouble).toList,
      smartThresholds.drop(numInvalid),
      name = "smart thresholds"
    )
    p.xlabel = "test date"
    p.ylabel = "seconds"
    p.legend = true

//    Thread.sleep(20000)
  }
}


object SmartNumberArbiterSpec extends CorePersistenceAware {

  /**
    * Helper method to create latest dummy test execution and persist it to the database
    */
  def persistDummyTestExecution(testID: String, responseTime: Int): TestExecutionRowLike = {
    val incomingTestExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(
      testID,
      new util.Date,
      responseTime,
      10000.0
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
      this.persistenceUtils.createTestExecution(testID, new util.Date, responseTime + (Random.nextGaussian * 4), 10000.0).responseTime
    }

    responseTimes
  }
}