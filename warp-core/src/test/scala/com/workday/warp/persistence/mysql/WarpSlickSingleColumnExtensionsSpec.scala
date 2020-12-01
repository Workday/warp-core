package com.workday.warp.persistence.mysql

import java.time.Instant

import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import com.workday.warp.persistence.Tables._
import com.workday.warp.persistence.mysql.WarpMySQLProfile.api._
import com.workday.warp.persistence.mysql.WarpSlickSingleColumnExtensionsSpec._
import com.workday.warp.persistence.{Connection, CorePersistenceAware, CorePersistenceUtils}
import com.workday.warp.TestIdImplicits.string2TestId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.parallel.Isolated

/**
  * Created by ruiqi.wang
  */
@Isolated
class WarpSlickSingleColumnExtensionsSpec extends WarpJUnitSpec with CorePersistenceAware {

  /** Truncates the schema. */
  @BeforeEach
  def truncateSchema(): Unit = {
    Connection.refresh()
    CorePersistenceUtils.truncateSchema()
  }

  /**
    * Checks that our custom standard deviation aggregation function works correctly.
    */
  @UnitTest
  def standardDeviation(): Unit = {
    this.persistenceUtils.createTestExecution(methodSignature1, Instant.now(), 1.0, 10)
    this.persistenceUtils.createTestExecution(methodSignature1, Instant.now(), 2.0, 10)
    this.persistenceUtils.createTestExecution(methodSignature1, Instant.now(), 3.0, 10)
    this.persistenceUtils.createTestExecution(methodSignature2, Instant.now(), 2.0, 10)
    this.persistenceUtils.createTestExecution(methodSignature2, Instant.now(), 4.0, 10)
    this.persistenceUtils.createTestExecution(methodSignature2, Instant.now(), 6.0, 10)
    this.persistenceUtils.createTestExecution(methodSignature3, Instant.now(), 2.0, 10)
    this.persistenceUtils.createTestExecution(methodSignature3, Instant.now(), 2.0, 10)
    this.persistenceUtils.createTestExecution(methodSignature3, Instant.now(), 2.0, 10)

    val query = TestExecution.groupBy(_.idTestDefinition)
    val action = query.map { case (id, metrics) => (
      id,
      metrics.map(_.responseTime).std
      )
    }

    val rows: Seq[(Int, Option[Double])] = this.persistenceUtils.runWithRetries(action.result, 5).sortBy(_._1)
    rows.head._2 shouldBe defined
    rows.head._2.get shouldEqual STD_1
    rows(1)._2 shouldBe defined
    rows(1)._2.get shouldEqual STD_2
    rows(2)._2 shouldBe defined
    rows(2)._2.get shouldEqual 0
  }
}

object WarpSlickSingleColumnExtensionsSpec {

  val methodSignature1 = "com.workday.warp.slick.implicits.test.1"
  val methodSignature2 = "com.workday.warp.slick.implicits.test.2"
  val methodSignature3 = "com.workday.warp.slick.implicits.test.3"

  val STD_1: Double = 0.816496580927726
  val STD_2: Double = 1.632993161855452

}
