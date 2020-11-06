package com.workday.warp.persistence.mysql

import java.sql
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.text.DecimalFormat
import java.time._
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.{Calendar, TimeZone, Date => JUDate}

import com.workday.warp.persistence.Tables._
import com.workday.warp.persistence.mysql.WarpMySQLProfile.api._
import com.workday.warp.TestIdImplicits.methodSignatureIsTestId
import WarpSlickDslSpec._
import com.workday.warp.persistence.{Connection, CorePersistenceAware, CorePersistenceUtils, TablesLike}
import slick.lifted.Query
import TablesLike.{TestDefinitionRowLike, TestExecutionRowLike}
import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import org.junit.jupiter.api.parallel.Isolated

/**
  * Created by ruiqi.wang
  */
@Isolated
class WarpSlickDslSpec extends WarpJUnitSpec with CorePersistenceAware {

  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

  /** Truncates the schema. */
  def truncateSchema(): Unit = {
    Connection.refresh()
    CorePersistenceUtils.truncateSchema()
  }

  /** Tests REGEXP dsl. */
  @UnitTest
  def regexMatch(): Unit = {
    this.persistenceUtils.findOrCreateTestDefinition(methodSignature1)
    this.persistenceUtils.findOrCreateTestDefinition(methodSignature2)

    val action = TestDefinition.filter(_.methodSignature regexMatch "hello")

    val rows: Seq[TestDefinitionRow] = this.persistenceUtils.runWithRetries(action.result)
    rows.size shouldEqual 1
    val check: Boolean = rows.exists(t => t.methodSignature.contains("hello") && !t.methodSignature.contains("bye"))
    check shouldEqual true

  }

  /** Tests QUOTE dsl. */
  @UnitTest
  def returnQuoted(): Unit = {
    val testDefinition: TestDefinitionRowLike = this.persistenceUtils.findOrCreateTestDefinition(methodSignature3)
    val cast: Rep[String] = testDefinition.methodSignature
    val test: Rep[String] = cast quote()
    val query1 = this.persistenceUtils.runWithRetries(test.result)

    val addBackSlash = testDefinition.methodSignature.replaceAll("\'", "\\\\\'")
    val query2 = "\'" + addBackSlash + "\'"
    query1 shouldEqual query2

  }

  /** Tests INTERVAL dsl. */
  @UnitTest
  def betweenInterval(): Unit = {
    this.truncateSchema()
    this.persistenceUtils.createTestExecution(methodSignature1, Instant.now(), 1.0, 10)
    this.persistenceUtils.createTestExecution(methodSignature1, Instant.now(), 1.0, 10)
    this.persistenceUtils.createTestExecution(methodSignature1, Instant.now(), 1.0, 10)
    Thread.sleep(2000)

    val query1 = TestExecution.filter(_.endTime isWithinPast "1 SECOND")
    this.persistenceUtils.runWithRetries(query1.result, 5) shouldBe empty

    val query2 = TestExecution.filter(_.endTime isWithinPast "5 MINUTE")
    this.persistenceUtils.runWithRetries(query2.result, 5).size shouldEqual 3

  }

  /** Tests YEAR (string) dsl. */
  @UnitTest
  def returnYearString(): Unit = {
    this.persistenceUtils.createTestExecution(methodSignature1, Instant.now(), 1.0, 10)
    val currentTimestamp: Rep[String] = TimeStampExtensions.now()
    val query1: Rep[Int] = currentTimestamp.year()
    this.persistenceUtils.runWithRetries(query1.result, 5) shouldEqual Year.now.getValue

  }

  /** Tests YEAR (timestamp) dsl. */
  @UnitTest
  def returnYearTimestamp(): Unit = {
    this.persistenceUtils.createTestExecution(methodSignature1, Instant.now(), 1.0, 10)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(methodSignature1, Instant.now(), 1.0, 10)
    val timeStamp: Rep[Timestamp] = testExecution.startTime
    val query1: Rep[Int] = timeStamp year()
    this.persistenceUtils.runWithRetries(query1.result, 5) shouldEqual Year.now.getValue
  }

  /** Tests UNIX_TIMESTAMP (Date) dsl. */
  @UnitTest
  def returnUNIXTimeStampDate(): Unit = {
    val date: Rep[java.sql.Date] = new sql.Date(Instant.now.toEpochMilli)
    val query: Rep[Long] = date unixTimestamp()
    val result: Long = this.persistenceUtils.runWithRetries(query.result, 5)

    val cal: Calendar = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MINUTE, 0)
    val instant: Instant = cal.getTime().toInstant
    val unixTimestamp: Long = instant.getEpochSecond()
    result shouldEqual unixTimestamp +- 2

  }

  /** Tests UNIX_TIMESTAMP dsl. */
  @UnitTest
  def returnUNIXTimeStampNow(): Unit = {
    val query: Rep[Long] = TimeStampExtensions.unixTimestamp()
    val result: Long = this.persistenceUtils.runWithRetries(query.result)
    val unixTimestamp: Long = Instant.now.getEpochSecond()
    result shouldEqual unixTimestamp +- 2

  }

  /** Tests subdate(date, interval) dsl. */
  @UnitTest
  def getSubdateInterval(): Unit = {
  this.persistenceUtils.createTestExecution(methodSignature1, Instant.now(), 1.0, 10)
    val format: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
    val cal: Calendar = Calendar.getInstance()
    val currentDate: String = format.format(cal.getTime)

    // Test years
    val cal1: Calendar = Calendar.getInstance()
    val query1: Rep[sql.Timestamp] = TimeStampExtensions.subdate(currentDate, "1 YEAR")
    val queryYear: sql.Timestamp = this.persistenceUtils.runWithRetries(query1.result)
    cal1.add(Calendar.YEAR, -1)
    val resultYear: String = format.format(cal1.getTime)
    val date1: JUDate = format.parse(resultYear)
    date1 shouldEqual queryYear

    // Test days
    val cal2: Calendar = Calendar.getInstance()
    val query2: Rep[sql.Timestamp] = TimeStampExtensions.subdate(currentDate, "57 DAY")
    val queryDay: sql.Timestamp = this.persistenceUtils.runWithRetries(query2.result)
    cal2.add(Calendar.DATE, -57)
    val resultDay: String = format.format(cal2.getTime)
    val date2: JUDate = format.parse(resultDay)
    date2 shouldEqual queryDay

    // Set to midnight
    val cal3: Calendar = Calendar.getInstance()
    cal3.set(Calendar.HOUR_OF_DAY, 0)
    cal3.set(Calendar.SECOND, 0)
    cal3.set(Calendar.MINUTE, 0)
    cal3.set(Calendar.MILLISECOND, 0)

    // Test hours
    val query3: Rep[sql.Timestamp] = TimeStampExtensions.subdate(currentDate, "-3 HOUR")
    val queryHour: sql.Timestamp = this.persistenceUtils.runWithRetries(query3.result)
    cal3.add(Calendar.HOUR, 3)
    val resultHour: sql.Timestamp = new Timestamp(cal3.getTimeInMillis)
    resultHour shouldEqual queryHour

  }

  /** Tests subdate(date, days) dsl. */
  @UnitTest
  def getSubdateNoInterval(): Unit = {
    this.persistenceUtils.createTestExecution(methodSignature1, Instant.now(), 1.0, 10)
    val format: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
    val cal: Calendar = Calendar.getInstance()
    val currentDate: String = format.format(cal.getTime)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    cal.set(Calendar.MINUTE, 0)

    val query: Rep[sql.Timestamp] = TimeStampExtensions.subdate(currentDate, -1)
    val queryDay: sql.Timestamp = this.persistenceUtils.runWithRetries(query.result)
    cal.add(Calendar.DATE, 1)
    val resultDay: sql.Timestamp = new Timestamp(cal.getTimeInMillis)
    resultDay shouldEqual queryDay
  }

  /** Tests subdate(timestamp, interval) dsl. */
  @UnitTest
  def getSubdateTimestamp(): Unit = {
    this.persistenceUtils.createTestExecution(methodSignature1, Instant.now(), 1.0, 10)
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(methodSignature1, Instant.now(), 1.0, 10)
    val time: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)
    val startTime: Rep[Timestamp] = testExecution.startTime

    val result: ZonedDateTime = time.minus(5, ChronoUnit.HOURS)
    val timestamp = Timestamp.valueOf(result.toLocalDateTime)

    val query: Rep[sql.Timestamp] = startTime subdate("5 HOUR")
    val queryDay: sql.Timestamp = this.persistenceUtils.runWithRetries(query.result, 5)
    val timestamp2 = new Timestamp(queryDay.getTime)

    val difference: Long = (timestamp.getTime - timestamp2.getTime)/1000
    difference shouldEqual(0.toLong +- 2)

  }

  /** Tests ROUND dsl. */
  @UnitTest
  def roundNumberNoParameter(): Unit = {
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(methodSignature1, Instant.now(), 1.593, 10.352)

    val df1: DecimalFormat = new DecimalFormat("#")
    val query1: Rep[Int] = NumberExtension.round(testExecution.responseTime)
    val result1: Int = this.persistenceUtils.runWithRetries(query1.result, 5)
    val roundedNumber1: Int = df1.format(testExecution.responseTime).toInt
    result1 shouldEqual roundedNumber1

  }

  /** Tests DATE(timestamp) dsl. */
  @UnitTest
  def returnDateTimestamp(): Unit = {
    val format: SimpleDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")
    val date: String = format.format(new java.util.Date())

    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(methodSignature1, Instant.now(), 1.0, 10)
    val timeStamp: Rep[sql.Timestamp] = testExecution.startTime
    val query1: Rep[sql.Date] = timeStamp.date()
    this.persistenceUtils.runWithRetries(query1.result).toString shouldEqual date

    val query2: Query[Rep[sql.Date], sql.Date, Seq] = TestExecution.map(_.startTime.date())
    this.persistenceUtils.runWithRetries(query2.result).head.toString shouldEqual date

  }

  /** Tests DATE(string) dsl. */
  @UnitTest
  def returnDateString(): Unit = {
    val format: SimpleDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")
    val format2: SimpleDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    val currentDate: String = format.format(new java.util.Date())

    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(methodSignature1, Instant.now(), 1.0, 10)
    val queryDate: Rep[String] = format2.format(testExecution.startTime)
    val query: Rep[sql.Date] = queryDate.date()
    this.persistenceUtils.runWithRetries(query.result).toString shouldEqual currentDate

  }

  /** Tests NOW dsl. */
  @UnitTest
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

  /** Tests UNIX_TIMESTAMP dsl. */
  @UnitTest
  def returnUNIXTimeStamp(): Unit = {
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(methodSignature1, Instant.now(), 1.0, 10)
    val timeStamp: Rep[Timestamp] = testExecution.startTime
    val query: Rep[Long] = timeStamp unixTimestamp()
    val result: Long = this.persistenceUtils.runWithRetries(query.result)
    val unixTimestamp: Long = Instant.now.getEpochSecond()
    result shouldEqual unixTimestamp +- 2

  }

  /** Tests ROUND dsl. */
  @UnitTest
  def roundNumber(): Unit = {
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(methodSignature1, Instant.now(), 1.593, 10.352)

    val df1: DecimalFormat = new DecimalFormat("#.#")
    val query1: Rep[Double] = NumberExtension.round(testExecution.responseTime, 1)
    val result1: Double = this.persistenceUtils.runWithRetries(query1.result)
    val roundedNumber1: Double = df1.format(testExecution.responseTime).toDouble
    result1 shouldEqual roundedNumber1

    val df2: DecimalFormat = new DecimalFormat("#")
    val query2: Rep[Double] = NumberExtension.round(testExecution.responseTimeRequirement, 0)
    val result2: Double = this.persistenceUtils.runWithRetries(query2.result)
    val roundedNumber2: Double = df2.format(testExecution.responseTimeRequirement).toDouble
    result2 shouldEqual roundedNumber2

  }
}

object WarpSlickDslSpec {

  val methodSignature1 = "com.workday.warp.slick.implicits.hello"
  val methodSignature2 = "com.workday.warp.slick.implicits.bye"
  val methodSignature3 = "com.workday.wa'rp.slick.implic'its.bye"
}
