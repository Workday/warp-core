package com.workday.warp.persistence.mysql


import java.sql
import java.util.{Calendar, TimeZone, Date => JUDate}
import java.time._
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.text.DecimalFormat
import java.time.temporal.ChronoUnit

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.experimental.categories.Category
import org.junit.{Before, Test}
import com.workday.warp.persistence.Tables._
import com.workday.warp.persistence.mysql.WarpMySQLProfile.api._
import WarpSlickDslSpec._
import com.workday.warp.persistence.{Connection, CorePersistenceAware, CorePersistenceUtils, TablesLike}
import slick.lifted.Query
import TablesLike.{TestDefinitionRowLike, TestExecutionRowLike}

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
  /** Tests QUOTE dsl. */
  def returnQuoted(): Unit = {
    val testDefinition: TestDefinitionRowLike = this.persistenceUtils.findOrCreateTestDefinition(methodSignature3)
    val cast: Rep[String] = testDefinition.methodSignature
    val test: Rep[String] = cast quote()
    val query1 = this.persistenceUtils.runWithRetries(test.result, 5)

    val addBackSlash = testDefinition.methodSignature.replaceAll("\'", "\\\\\'")
    val query2 = "\'" + addBackSlash + "\'"
    query1 shouldEqual query2

  }

  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests INTERVAL dsl. */
  def betweenInterval(): Unit = {
    this.persistenceUtils.createTestExecution(methodSignature1, new JUDate, 1.0, 10)
    this.persistenceUtils.createTestExecution(methodSignature1, new JUDate, 1.0, 10)
    this.persistenceUtils.createTestExecution(methodSignature1, new JUDate, 1.0, 10)
    Thread.sleep(2000)

    val query1 = TestExecution.filter(_.endTime isWithinPast "1 SECOND")
    this.persistenceUtils.runWithRetries(query1.result, 5) shouldBe empty

    val query2 = TestExecution.filter(_.endTime isWithinPast "5 MINUTE")
    this.persistenceUtils.runWithRetries(query2.result, 5).size shouldEqual 3

  }

  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests YEAR (string) dsl. */
  def returnYearString(): Unit = {
    this.persistenceUtils.createTestExecution(methodSignature1, new JUDate, 1.0, 10)
    val currentTimestamp: Rep[String] = TimeStampExtensions.now()
    val query1: Rep[Int] = currentTimestamp.year()
    this.persistenceUtils.runWithRetries(query1.result, 5) shouldEqual Year.now.getValue

  }

  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests YEAR (timestamp) dsl. */
  def returnYearTimestamp(): Unit = {
    this.persistenceUtils.createTestExecution(methodSignature1, new JUDate, 1.0, 10)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(methodSignature1, new JUDate, 1.0, 10)
    val timeStamp: Rep[Timestamp] = testExecution.startTime
    val query1: Rep[Int] = timeStamp year()
    this.persistenceUtils.runWithRetries(query1.result, 5) shouldEqual Year.now.getValue
  }

  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests UNIX_TIMESTAMP (Date) dsl. */
  def returnUNIXTimeStampDate(): Unit = {
    val date: Rep[java.sql.Date] = new sql.Date(Instant.now.toEpochMilli)
    val query: Rep[Long] = date unixTimestamp()
    val result: Long = this.persistenceUtils.runWithRetries(query.result, 5)

    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    val cal: Calendar = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MINUTE, 0)
    val instant: Instant = cal.getTime().toInstant
    val unixTimestamp: Long = instant.getEpochSecond()
    result shouldEqual unixTimestamp +- 2

  }

  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests UNIX_TIMESTAMP dsl. */
  def returnUNIXTimeStampNow(): Unit = {
    val query: Rep[Long] = TimeStampExtensions.unixTimestamp()
    val result: Long = this.persistenceUtils.runWithRetries(query.result, 5)
    val unixTimestamp: Long = Instant.now.getEpochSecond()
    result shouldEqual unixTimestamp +- 2

  }

  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests subdate(date, interval) dsl. */
  def getSubdateInterval(): Unit = {
  this.persistenceUtils.createTestExecution(methodSignature1, new JUDate, 1.0, 10)
    val format: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
    val cal: Calendar = Calendar.getInstance()
    val currentDate: String = format.format(cal.getTime())

    // Test years
    val cal1: Calendar = Calendar.getInstance()
    val query1: Rep[String] = TimeStampExtensions.subdate(currentDate, "1 YEAR")
    val queryYear: String = this.persistenceUtils.runWithRetries(query1.result, 5)
    cal1.add(Calendar.YEAR, -1)
    val resultYear: String = format.format(cal1.getTime)
    resultYear shouldEqual queryYear

    // Test days
    val cal2: Calendar = Calendar.getInstance()
    val query2: Rep[String] = TimeStampExtensions.subdate(currentDate, "57 DAY")
    val queryDay: String = this.persistenceUtils.runWithRetries(query2.result, 5)
    cal2.add(Calendar.DATE, -57)
    val resultDay: String = format.format(cal2.getTime)
    resultDay shouldEqual queryDay

    // Set to midnight
    val hourFormatter: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val cal3: Calendar = Calendar.getInstance()
    cal3.set(Calendar.HOUR_OF_DAY, 0)
    cal3.set(Calendar.SECOND, 0)
    cal3.set(Calendar.MINUTE, 0)

    // Test hours
    val query3: Rep[String] = TimeStampExtensions.subdate(currentDate, "-3 HOUR")
    val queryHour: String = this.persistenceUtils.runWithRetries(query3.result, 5)
    cal3.add(Calendar.HOUR, 3)
    val resultHour: String = hourFormatter.format(cal3.getTime)
    resultHour shouldEqual queryHour

  }

  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests subdate(date, days) dsl. */
  def getSubdateNoInterval(): Unit = {
    this.persistenceUtils.createTestExecution(methodSignature1, new JUDate, 1.0, 10)
    val format: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
    val cal: Calendar = Calendar.getInstance()
    val currentDate: String = format.format(cal.getTime())

    val cal2: Calendar = Calendar.getInstance()
    val query: Rep[String] = TimeStampExtensions.subdate(currentDate, 57)
    val queryDay: String = this.persistenceUtils.runWithRetries(query.result, 5)
    cal2.add(Calendar.DATE, -57)
    val resultDay: String = format.format(cal2.getTime)
    resultDay shouldEqual queryDay

    val cal3: Calendar = Calendar.getInstance()
    val query2: Rep[String] = TimeStampExtensions.subdate(currentDate, -1)
    val queryDay2: String = this.persistenceUtils.runWithRetries(query2.result, 5)
    cal3.add(Calendar.DATE, 1)
    val resultDay2: String = format.format(cal3.getTime)
    resultDay2 shouldEqual queryDay2
  }

  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests subdate(timestamp, interval) dsl. */
  def getSubdateTimestamp(): Unit = {
    this.persistenceUtils.createTestExecution(methodSignature1, new JUDate, 1.0, 10)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(methodSignature1, new JUDate, 1.0, 10)
    val time: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)
    val startTime: Rep[Timestamp] = testExecution.startTime

    val result: ZonedDateTime = time.minus(5, ChronoUnit.HOURS)
    val timestamp = Timestamp.valueOf(result.toLocalDateTime)

    val format: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    val query: Rep[String] = startTime subdate("5 HOUR")
    val queryDay: String = this.persistenceUtils.runWithRetries(query.result, 5)
    val resultFormat: JUDate = format.parse(queryDay)
    val timestamp2 = new Timestamp(resultFormat.getTime)

    val difference: Long = (timestamp.getTime - timestamp2.getTime)/1000
    difference shouldEqual(0.toLong +- 2)

  }

  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests ROUND dsl. */
  def roundNumberNoParameter(): Unit = {
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(methodSignature1, new JUDate, 1.593, 10.352)

    val df1: DecimalFormat = new DecimalFormat("#")
    val responseTimeTest1: Double = testExecution.responseTime
    val query1: Rep[Int] = NumberExtension.round(responseTimeTest1)
    val result1: Int = this.persistenceUtils.runWithRetries(query1.result, 5)
    val roundedNumber1: Int = df1.format(responseTimeTest1).toInt
    result1 shouldEqual roundedNumber1

  }

  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests DATE(timestamp) dsl. */
  def returnDateTimestamp(): Unit = {
    val format: SimpleDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")
    val date: String = format.format(new java.util.Date())

    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(methodSignature1, new JUDate, 1.0, 10)
    val timeStamp: Rep[Timestamp] = testExecution.startTime
    val query1: Rep[String] = timeStamp date()
    this.persistenceUtils.runWithRetries(query1.result, 5) shouldEqual date

    val query2: Query[Rep[String], String, Seq] = TestExecution.map(t => t.startTime date())
    this.persistenceUtils.runWithRetries(query2.result, 5).head shouldEqual date

  }

  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests DATE(string) dsl. */
  def returnDateString(): Unit = {
    val format: SimpleDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")
    val format2: SimpleDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    val currentDate: String = format.format(new java.util.Date())

    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(methodSignature1, new JUDate, 1.0, 10)
    val queryDate: Rep[String] = format2.format(testExecution.startTime)
    val query: Rep[String] = queryDate date()
    this.persistenceUtils.runWithRetries(query.result, 5) shouldEqual currentDate

  }

  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests NOW dsl. */
  def getCurrentTimestamp(): Unit = {
    val query: Rep[String] = TimeStampExtensions.now()
    val result: String = this.persistenceUtils.runWithRetries(query.result, 5)
    val format: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val convertToTimeStamp: LocalDateTime = LocalDateTime.parse(result, format)
    val parsedResult: ZonedDateTime = convertToTimeStamp.atZone(ZoneId.of("UTC"))


    val UTCZone: ZoneId = ZoneId.of("UTC")
    val zonedTime: ZonedDateTime = ZonedDateTime.now
    val utcDate: ZonedDateTime = zonedTime.withZoneSameInstant(UTCZone)

    val period: Duration = Duration.between(parsedResult, utcDate)
    val difference: Long = Math.abs(period.toMinutes())

    difference shouldEqual 0.toLong +- 1

  }

  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests UNIX_TIMESTAMP dsl. */
  def returnUNIXTimeStamp(): Unit = {
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(methodSignature1, new JUDate, 1.0, 10)
    val timeStamp: Rep[Timestamp] = testExecution.startTime
    val query: Rep[Long] = timeStamp unixTimestamp()
    val result: Long = this.persistenceUtils.runWithRetries(query.result, 5)
    val unixTimestamp: Long = Instant.now.getEpochSecond()
    result shouldEqual unixTimestamp +- 2

  }

  @Test
  @Category(Array(classOf[UnitTest]))
  /** Tests ROUND dsl. */
  def roundNumber(): Unit = {
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(methodSignature1, new JUDate, 1.593, 10.352)

    val df1: DecimalFormat = new DecimalFormat("#.#")
    val responseTimeTest1: Double = testExecution.responseTime
    val query1: Rep[Double] = NumberExtension.round(responseTimeTest1, 1)
    val result1: Double = this.persistenceUtils.runWithRetries(query1.result, 5)
    val roundedNumber1: Double = df1.format(responseTimeTest1).toDouble
    result1 shouldEqual roundedNumber1

    val df2: DecimalFormat = new DecimalFormat("#")
    val responseTimeTest2: Double = testExecution.responseTimeRequirement
    val query2: Rep[Double] = NumberExtension.round(responseTimeTest2, 0)
    val result2: Double = this.persistenceUtils.runWithRetries(query2.result, 5)
    val roundedNumber2: Double = df2.format(responseTimeTest2).toDouble
    result2 shouldEqual roundedNumber2

  }
}

object WarpSlickDslSpec {

  val methodSignature1 = "com.workday.warp.slick.implicits.hello"
  val methodSignature2 = "com.workday.warp.slick.implicits.bye"
  val methodSignature3 = "com.workday.wa'rp.slick.implic'its.bye"
}
