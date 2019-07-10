package com.workday.warp.persistence.mysql

import java.util.{Date, Calendar}
import java.time.{Year, ZoneId, ZonedDateTime}
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.Instant

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.experimental.categories.Category
import org.junit.{Before, Test}
import com.workday.warp.persistence.Tables._
import com.workday.warp.persistence.mysql.WarpMySQLProfile.api._
import WarpSlickDslSpec._
import com.workday.warp.persistence.{Connection, CorePersistenceAware, CorePersistenceUtils, TablesLike}
import slick.lifted.Query
import TablesLike.TestExecutionRowLike


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
    check shouldEqual true

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
    this.persistenceUtils.runWithRetries(query2.result, 5).size shouldEqual 3

  }

  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests YEAR dsl. */
  def returnYear(): Unit = {
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(methodSignature1, new Date, 1.0, 10)
    val timeStamp: Rep[Timestamp] = testExecution.startTime
    val query1: Rep[Int] = timeStamp year()
    this.persistenceUtils.runWithRetries(query1.result, 5) shouldEqual Year.now.getValue

    val query2: Query[Rep[Int], Int, Seq] = TestExecution.map(t => t.startTime year())
    this.persistenceUtils.runWithRetries(query2.result, 5).head shouldEqual Year.now.getValue

  }

  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests DATE dsl. */
  def returnDate(): Unit = {
    val format: SimpleDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")
    val date: String = format.format(new java.util.Date())

    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(methodSignature1, new Date, 1.0, 10)
    val timeStamp: Rep[Timestamp] = testExecution.startTime
    val query1: Rep[String] = timeStamp date()
    this.persistenceUtils.runWithRetries(query1.result, 5) shouldEqual date

    val query2: Query[Rep[String], String, Seq] = TestExecution.map(t => t.startTime date())
    this.persistenceUtils.runWithRetries(query2.result, 5).head shouldEqual date

  }

  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests NOW dsl. */
  def getCurrentTimestamp(): Unit = {
    val query: Rep[String] = TimeStampExtensions.now()
    val result: String = this.persistenceUtils.runWithRetries(query.result, 5)

    val UTCZone: ZoneId = ZoneId.of("UTC")
    val zonedTime: ZonedDateTime = ZonedDateTime.now
    val utcDate: ZonedDateTime = zonedTime.withZoneSameInstant(UTCZone)
    val dateAsString: String = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(utcDate)

    result shouldEqual dateAsString

  }

  @Test
  @Category(Array(classOf[UnitTest]))
   /** Tests UNIX_TIMESTAMP dsl. */
  def returnUNIXTimeStamp(): Unit = {
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(methodSignature1, new Date(), 1.0, 10)
    val timeStamp: Rep[Timestamp] = testExecution.startTime
    val query: Rep[Int] = timeStamp unixTimestamp()
    val result: Int = this.persistenceUtils.runWithRetries(query.result, 5)
    val unixTimestamp: Long = Instant.now.getEpochSecond()
    result shouldEqual unixTimestamp

  }

  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests subdate dsl. */
  def getSubdate(): Unit = {
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(methodSignature1, new Date(), 1.0, 10)
    val timeStamp: Rep[Timestamp] = testExecution.startTime
    val format: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
    val cal: Calendar = Calendar.getInstance()
    val currentDate: String = format.format(cal.getTime())

    //Test years
    val cal1: Calendar = Calendar.getInstance()
    val query1: Rep[String] = timeStamp subdate (currentDate, "1 YEAR")
    val queryYear: String = this.persistenceUtils.runWithRetries(query1.result, 5)
    cal1.add(Calendar.YEAR, -1)
    val resultYear: String = format.format(cal1.getTime)
    resultYear shouldEqual queryYear

    //Test days
    val cal2: Calendar = Calendar.getInstance()
    val query2: Rep[String] = timeStamp subdate (currentDate, "57 DAY")
    val queryDay: String = this.persistenceUtils.runWithRetries(query2.result, 5)
    cal2.add(Calendar.DATE, -57)
    val resultDay: String = format.format(cal2.getTime)
    resultDay shouldEqual queryDay

    //Set to midnight
    val hourFormatter: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val cal3: Calendar = Calendar.getInstance()
    cal3.set(Calendar.HOUR_OF_DAY, 0)
    cal3.set(Calendar.MILLISECOND, 0)
    cal3.set(Calendar.SECOND, 0)
    cal3.set(Calendar.MINUTE, 0)

    //Test hours
    val query3: Rep[String] = timeStamp subdate (currentDate, "-3 HOUR")
    val queryHour: String = this.persistenceUtils.runWithRetries(query3.result, 5)
    cal3.add(Calendar.HOUR, 3)
    val resultHour: String = hourFormatter.format(cal3.getTime)
    resultHour shouldEqual queryHour

  }
}

object WarpSlickDslSpec {

  val methodSignature1 = "com.workday.warp.slick.implicits.hello"
  val methodSignature2 = "com.workday.warp.slick.implicits.bye"

}
