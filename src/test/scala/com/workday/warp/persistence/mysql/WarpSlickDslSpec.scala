package com.workday.warp.persistence.mysql

import java.util.Date
import java.time.{Year, ZoneId, ZonedDateTime}
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.experimental.categories.Category
import org.junit.{Before, Test}
import com.workday.warp.persistence.Tables._
import com.workday.warp.persistence.mysql.WarpMySQLProfile.api._
import WarpSlickDslSpec._
import com.workday.warp.persistence.{Connection, CorePersistenceAware, CorePersistenceUtils, TablesLike}
import slick.lifted.Query


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

    val query1 = TestExecution.filter(_.endTime isWithinPast "1 SECOND")
    this.persistenceUtils.runWithRetries(query1.result, 5) shouldBe empty

    val query2 = TestExecution.filter(_.endTime isWithinPast "5 MINUTE")
    this.persistenceUtils.runWithRetries(query2.result, 5).size shouldBe 3

  }


  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests YEAR dsl. */
  def returnYear(): Unit = {
    val testExecution: TablesLike.TestExecutionRowLike = this.persistenceUtils.createTestExecution(methodSignature1, new Date, 1.0, 10)
    val timeStamp: Rep[Timestamp] = testExecution.startTime
    val query1: Rep[Int] = timeStamp year()
    this.persistenceUtils.runWithRetries(query1.result, 5) shouldBe Year.now.getValue

    val query2: Query[Rep[Int], Int, Seq] = TestExecution.map(t => t.startTime year())
    this.persistenceUtils.runWithRetries(query2.result, 5).head shouldBe Year.now.getValue

  }

  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests DATE dsl. */
  def returnDate(): Unit = {
    val format: SimpleDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")
    val date: String = format.format(new java.util.Date())

    val testExecution: TablesLike.TestExecutionRowLike = this.persistenceUtils.createTestExecution(methodSignature1, new Date, 1.0, 10)
    val timeStamp: Rep[Timestamp] = testExecution.startTime
    val query1: Rep[String] = timeStamp date()
    this.persistenceUtils.runWithRetries(query1.result, 5) shouldBe date

    val query2: Query[Rep[String], String, Seq] = TestExecution.map(t => t.startTime date())
    this.persistenceUtils.runWithRetries(query2.result, 5).head shouldBe date

  }

  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests NOW dsl. */
  def getCurrentTimestamp(): Unit = {
    val testExecution: TablesLike.TestExecutionRowLike = this.persistenceUtils.createTestExecution(methodSignature1, new Date(), 1.0, 10)
    val timeStamp: Rep[Timestamp] = testExecution.startTime
    val query: Rep[String] = timeStamp now()
    val result: String = this.persistenceUtils.runWithRetries(query.result, 5)

    val UTCZone: ZoneId = ZoneId.of("UTC")
    val zonedTime: ZonedDateTime = ZonedDateTime.now
    val utcDate: ZonedDateTime = zonedTime.withZoneSameInstant(UTCZone)
    val dateAsString: String = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(utcDate)

    result shouldBe dateAsString

  }

}

object WarpSlickDslSpec {

  val methodSignature1 = "com.workday.warp.slick.implicits.hello"
  val methodSignature2 = "com.workday.warp.slick.implicits.bye"

}
