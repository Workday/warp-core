package com.workday.warp.math.linalg

import com.workday.warp.TrialResult
import com.workday.warp.dsl._
import com.workday.warp.dsl.WarpMatchers._
import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.common.utils.Implicits.{DecoratedDuration, DecoratedInt}
import com.workday.warp.utils.Ballot
import org.junit.Test
import org.junit.experimental.categories.Category
import org.pmw.tinylog.Logger

import scala.util.Random

/**
  * Created by tomas.mccandless on 9/14/16.
  */
class RobustPcaRunnerSpec extends WarpJUnitSpec {

  /** Checks usage of the public rpca method. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def robustPca(): Unit = {
    val ballot: Ballot = new Ballot("com.workday.warp.test.test1")
    // use an empty list of response times and take the minimum number from configuration
    RobustPcaRunner().robustPca(List.empty[Double], ballot) shouldBe None
    // override minimum number of required measurements
    RobustPcaRunner(requiredMeasurements = 1).robustPca(List(1.5, 2.0, 3.14), ballot) shouldBe defined
  }


  /** Checks usage of sliding window. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def slidingWindow(): Unit = {
    val runner: RobustPcaRunner = RobustPcaRunner(requiredMeasurements = 1, useSlidingWindow = true)
    val dataSize: Int = 1000
    // create some dummy data, we don't care about the actual values, just the size
    val dummyData: List[Double] = List.fill(dataSize)(Random.nextDouble)
    runner.robustPca(dummyData) should not be empty
  }


  /** Checks behavior when there are not enough measurements. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def notEnoughMeasurements(): Unit = {
    val runner: RobustPcaRunner = RobustPcaRunner(requiredMeasurements = 1000)
    val dataSize: Int = 100
    // create some dummy data, we don't care about the actual values, just the size
    val dummyData: List[Double] = List.fill(dataSize)(Random.nextDouble)
    runner.robustPca(dummyData) should be (None)
  }


  /** Performance test for double rpca. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def testDoubleRobustPca(): Unit = {
    val runner: RobustPcaRunner = RobustPcaRunner(requiredMeasurements = 1, useDoubleRpca = true)

    val dataSize: Int = 1000
    // create some dummy data, we don't care about the actual values, just the size
    val dummyData: List[Double] = List.fill(dataSize)(Random.nextDouble)

    val result: List[TrialResult[_]] = using no collectors no arbiters measure {
      runner.robustPca(dummyData)
    }

    result should not exceed (30 seconds)
    Logger.debug(s"processed $dataSize in ${result.head.maybeResponseTime.get.humanReadable}")
  }
}
