package com.workday.warp.arbiters

import java.util.UUID

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.persistence.TablesLike._
import com.workday.warp.persistence.TablesLike.RowTypeClasses._
import com.workday.warp.utils.Ballot
import org.junit.{BeforeClass, Test}
import org.junit.experimental.categories.Category

/**
  * Created by tomas.mccandless on 6/4/18.
  */
class RobustPcaArbiterSpec extends WarpJUnitSpec {

  import RobustPcaArbiterSpec.testId

  val arbiter: RobustPcaArbiter = new RobustPcaArbiter

  @Test
  @Category(Array(classOf[UnitTest]))
  def notAnomaly(): Unit = {
    val lastTest: TestExecutionRowLike = SmartNumberArbiterSpec.persistDummyTestExecution(testId, 5)
    arbiter.vote(ballot = new Ballot(testId), lastTest) should be (None)
  }


  @Test
  @Category(Array(classOf[UnitTest]))
  def isAnomaly(): Unit = {
    val lastTest: TestExecutionRowLike = SmartNumberArbiterSpec.persistDummyTestExecution(testId, 50)
    arbiter.vote(ballot = new Ballot(testId), lastTest) should not be empty
  }


  @Test
  @Category(Array(classOf[UnitTest]))
  def passed(): Unit = {
    val lastTest: TestExecutionRowLike = SmartNumberArbiterSpec.persistDummyTestExecution(testId, 5)
    arbiter.passed(ballot = new Ballot(testId), lastTest) should be (true)
  }
}


object RobustPcaArbiterSpec {

  val testId: String = s"com.workday.warp.test.${UUID.randomUUID().toString}"

  @BeforeClass
  def setup(): Unit = {
    SmartNumberArbiterSpec.createDummyTestExecutions(
      testId,
      range = 100,
      responseTime = 5
    )
  }
}