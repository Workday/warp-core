package com.workday.warp.persistence

import java.util.Date

import com.workday.telemetron.annotation.Schedule
import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.persistence.TablesLike.TestExecutionRowLike
import org.junit.experimental.categories.Category
import org.junit.{BeforeClass, Test}

/**
  * Tests concurrent database access.
  *
  * The schema is dropped and recreated before this class is instantiated.
  *
  * Created by tomas.mccandless on 1/17/17.
  */
class ConcurrentWriteTest extends WarpJUnitSpec with CorePersistenceAware {
  override val shouldVerifyResponseTime: Boolean = true

  private val methodSignature: String = "com.workday.warp.product.subproduct.Class.method"


  /** Checks that we can concurrently find or create [[Tables.Build]]. */
  @Test
  @Category(Array(classOf[UnitTest]))
  @Schedule(invocations = 8, threads = 4)
  def concurrentBuild(): Unit = {
    val id: Int = this.persistenceUtils.findOrCreateBuild(year = 2016, week = 1, buildNumber = 345).idBuild
    // id should be the same
    this.persistenceUtils.findOrCreateBuild(year = 2016, week = 1, buildNumber = 345).idBuild should be (id)
    // id should be different with a new insert
    this.persistenceUtils.findOrCreateBuild(year = 2016, week = 1, buildNumber = 346).idBuild should not be id
  }


  /** Checks that we can concurrently find or create [[Tables.TestDefinition]]. */
  @Test
  @Category(Array(classOf[UnitTest]))
  @Schedule(invocations = 8, threads = 4)
  def concurrentTestDefinition(): Unit = {
    val id: Int = this.persistenceUtils.findOrCreateTestDefinition(this.methodSignature).idTestDefinition
    // id should be the same
    this.persistenceUtils.findOrCreateTestDefinition(this.methodSignature).idTestDefinition should be (id)
    // id should be different with a new insert
    this.persistenceUtils.findOrCreateTestDefinition(this.methodSignature + "abcdef").idTestDefinition should not be id
  }


  @Test
  @Category(Array(classOf[UnitTest]))
  @Schedule(threads = 8, invocations = 32)
  // TODO we should up the number of invocations when we have a @BeforeOnce annotation
  def concurrentTestExecution(): Unit = {
    val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(this.methodSignature,
                                                                     new Date, responseTime = 5.0,
                                                                     maxResponseTime = 6.0)

    this.persistenceUtils.recordMeasurement(testExecution.idTestExecution, "some measurement", result = 1.0)
    this.persistenceUtils.recordMeasurement(testExecution.idTestExecution, "some other measurement", result = 1.0)
    this.persistenceUtils.recordMeasurement(testExecution.idTestExecution, "yet another measurement", result = 1.0)
  }

  /** Checks that we can concurrently find or create [[Tables.MeasurementName]]. */
  @Test
  @Category(Array(classOf[UnitTest]))
  @Schedule(invocations = 8, threads = 4)
  def concurrentMeasurementName(): Unit = {
    val id: Int = this.persistenceUtils.findOrCreateMeasurementName("some description").idMeasurementName
    // id should be the same
    this.persistenceUtils.findOrCreateMeasurementName("some description").idMeasurementName should be (id)
    // id should be different with a new insert
    this.persistenceUtils.findOrCreateMeasurementName("some other description").idMeasurementName should not be id
  }


  /** Checks that we can concurrently find or create [[Tables.TagName]]. */
  @Test
  @Category(Array(classOf[UnitTest]))
  @Schedule(invocations = 8, threads = 4)
  def concurrentTagName(): Unit = {
    val id: Int = this.persistenceUtils.findOrCreateTagName("some description").idTagName
    // id should be the same
    this.persistenceUtils.findOrCreateTagName("some description").idTagName should be (id)
    // id should be different with a new insert
    this.persistenceUtils.findOrCreateTagName("some other description").idTagName should not be id
  }
}


object ConcurrentWriteTest extends CorePersistenceAware {

  /** Drops, re-creates the schema, and inserts some basic seed data. */
  @BeforeClass
  def recreateSchema(): Unit = {
    CorePersistenceUtils.dropSchema()
    Connection.refresh()
    CorePersistenceUtils.initSchema()
  }
}
