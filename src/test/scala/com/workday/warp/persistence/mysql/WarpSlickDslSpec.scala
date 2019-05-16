package com.workday.warp.persistence.mysql

import java.util.Date

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.experimental.categories.Category
import org.junit.{Before, Test}
import com.workday.warp.persistence.Tables._
import com.workday.warp.persistence.mysql.WarpMySQLProfile.api._
import WarpSlickDslSpec._
import com.workday.warp.persistence.{Connection, CorePersistenceAware, CorePersistenceUtils}

/**
  * Created by ruiqi.wang
  */
class WarpSlickDslSpec extends WarpJUnitSpec with CorePersistenceAware {

  /** Truncates the schema. */
  @Before
  def truncateSchema(): Unit = {
    Connection.refresh()
    CorePersistenceUtils.truncateSchema()
  }

  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests REGEXP dsl. */
  def regexMatch(): Unit = {
    this.persistenceUtils.findOrCreateTestDefinition(methodSignature1)
    this.persistenceUtils.findOrCreateTestDefinition(methodSignature2)

    val action = TestDefinition.filter(_.methodSignature regexMatch "hello")

    val rows: Seq[TestDefinitionRow] = this.persistenceUtils.runWithRetries(action.result, 5)
    rows.size shouldEqual 1
    val check: Boolean = rows.exists(t => t.methodSignature.contains("hello") && !t.methodSignature.contains("bye"))
    check shouldBe true
  }

  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests INTERVAL dsl. */
  def betweenInterval(): Unit = {
    this.persistenceUtils.createTestExecution(methodSignature1, new Date, 1.0, 10)
    this.persistenceUtils.createTestExecution(methodSignature1, new Date, 1.0, 10)
    this.persistenceUtils.createTestExecution(methodSignature1, new Date, 1.0, 10)
    Thread.sleep(2000)

    val query1 = TestExecution.filter(_.endTime betweenInterval "1 SECOND")
    this.persistenceUtils.runWithRetries(query1.result, 5) shouldBe empty

    val query2 = TestExecution.filter(_.endTime betweenInterval "5 MINUTE")
    this.persistenceUtils.runWithRetries(query2.result, 5).size shouldBe 3
  }

}

object WarpSlickDslSpec {

  val methodSignature1 = "com.workday.warp.slick.implicits.hello"
  val methodSignature2 = "com.workday.warp.slick.implicits.bye"

}
