package com.workday.warp.persistence.influxdb

import java.util.{Date, UUID}

import com.workday.warp.common.category.IntegrationTest
import com.workday.warp.common.heaphistogram.{HeapHistogram, HeapHistogramEntry}
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.persistence.{Connection, CorePersistenceAware}
import com.workday.warp.persistence.TablesLike.TestExecutionRowLike
import com.workday.warp.persistence.TablesLike.RowTypeClasses._
import org.influxdb.InfluxDB
import org.influxdb.dto.Pong
import org.junit.Test
import org.junit.experimental.categories.Category

import scala.util.Try

/**
  * Created by tomas.mccandless on 7/12/16.
  */
class InfluxDBClientSpec extends WarpJUnitSpec with CorePersistenceAware with InfluxDBClient {


  @Test
  @Category(Array(classOf[IntegrationTest]))
  def failedConnection(): Unit = {
    val maybeClient: Either[String, InfluxDB] = InfluxDBClient.connect("http://localhost:1234/bogus/", "dsjak", "sjk")
    maybeClient.isLeft should be (true)
  }


  /** Checks that we can establish a connection to influxdb. */
  @Test
  @Category(Array(classOf[IntegrationTest]))
  def testPing(): Unit = {
    val ping: Try[Pong] = this.ping
    ping.isSuccess should be (true)
    ping.get.getVersion should not be "unknown"
  }


  /** Checks that we can persist a heap histogram. */
  @Test
  @Category(Array(classOf[IntegrationTest]))
  def heapHistogram(): Unit = {
    val e1: HeapHistogramEntry = new HeapHistogramEntry("com.workday.warp.test1", 1, 3)
    val e2: HeapHistogramEntry = new HeapHistogramEntry("com.workday.warp.test2", 2, 3)
    val e3: HeapHistogramEntry = new HeapHistogramEntry("com.workday.warp.test3", 3, 3)
    val e4: HeapHistogramEntry = new HeapHistogramEntry("com.workday.warp.test4", 3, 3)

    val histo: HeapHistogram = new HeapHistogram(List(e1, e2, e3, e4))

    this.persistHeapHistogram(histo, "testHeapHistograms", "testSeries", "com.workday.warp.test").get
    this.dropDatabase("testHeapHistograms").get
  }


  /** Checks that we can persist response times and thresholds. */
  @Test
  @Category(Array(classOf[IntegrationTest]))
  def responseTimes(): Unit = {
    Connection.refresh()
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(this.getTestId, new Date, 1.0, 1.5)
    this.persistThreshold("testResponseTimes", "testResponseTimes", testExecution).get
    this.dropDatabase("testResponseTimes").get
  }


  /** Checks that we can persist response times and thresholds. */
  @Test
  @Category(Array(classOf[IntegrationTest]))
  def createDatabase(): Unit = {
    val dbName: String = s"schema-${UUID.randomUUID().toString}"

    val exists: Boolean = this.databaseExists(dbName).get
    if (exists) {
      this.dropDatabase(dbName).get
    }

    this.createDatabase(dbName).get
    this.databaseExists(dbName).get should be (true)
    this.dropDatabase(dbName).get
  }
}
