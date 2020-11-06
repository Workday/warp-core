package com.workday.warp.persistence

import java.time.Instant

import com.workday.warp.dsl._
import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import com.workday.warp.persistence.TablesLike.TestExecutionRowLike
import com.workday.warp.TestIdImplicits.methodSignatureIsTestId
import org.junit.jupiter.api.BeforeAll

/**
  * Tests concurrent database access.
  *
  * The schema is dropped and recreated before this class is instantiated.
  *
  * // TODO refactor schedule
  *
  * Created by tomas.mccandless on 1/17/17.
  */
class ConcurrentWriteTest extends WarpJUnitSpec with CorePersistenceAware {
  override val shouldVerifyResponseTime: Boolean = true

  private val methodSignature: String = "com.workday.warp.product.subproduct.Class.method"


  /** Checks that we can concurrently find or create [[Tables.Build]]. */
  @UnitTest
  def concurrentBuild(): Unit = {
    using threads 4 invocations 8 invoke {
      val id: Int = this.persistenceUtils.findOrCreateBuild(year = 2016, week = 1, buildNumber = 345).idBuild
      // id should be the same
      this.persistenceUtils.findOrCreateBuild(year = 2016, week = 1, buildNumber = 345).idBuild should be(id)
      // id should be different with a new insert
      this.persistenceUtils.findOrCreateBuild(year = 2016, week = 1, buildNumber = 346).idBuild should not be id
    }
  }


  /** Checks that we can concurrently find or create [[Tables.TestDefinition]]. */
  @UnitTest
  def concurrentTestDefinition(): Unit = {
    using threads 4 invocations 8 invoke {
      val id: Int = this.persistenceUtils.findOrCreateTestDefinition(this.methodSignature).idTestDefinition
      // id should be the same
      this.persistenceUtils.findOrCreateTestDefinition(this.methodSignature).idTestDefinition should be(id)
      // id should be different with a new insert
      this.persistenceUtils.findOrCreateTestDefinition(this.methodSignature + "abcdef").idTestDefinition should not be id
    }
  }


  @UnitTest
  def concurrentTestExecution(): Unit = {
    using threads 8 invocations 32 invoke {
      val testExecution: TestExecutionRowLike = this.persistenceUtils.createTestExecution(this.methodSignature,
        Instant.now(), responseTime = 5.0,
        maxResponseTime = 6.0)

      this.persistenceUtils.recordMeasurement(testExecution.idTestExecution, "some measurement", result = 1.0)
      this.persistenceUtils.recordMeasurement(testExecution.idTestExecution, "some other measurement", result = 1.0)
      this.persistenceUtils.recordMeasurement(testExecution.idTestExecution, "yet another measurement", result = 1.0)
    }
  }

  /** Checks that we can concurrently find or create [[Tables.MeasurementName]]. */
  @UnitTest
  def concurrentMeasurementName(): Unit = {
    using threads 4 invocations 8 invoke {
      val id: Int = this.persistenceUtils.findOrCreateMeasurementName("some description").idMeasurementName
      // id should be the same
      this.persistenceUtils.findOrCreateMeasurementName("some description").idMeasurementName should be(id)
      // id should be different with a new insert
      this.persistenceUtils.findOrCreateMeasurementName("some other description").idMeasurementName should not be id
    }
  }


  /** Checks that we can concurrently find or create [[Tables.TagName]]. */
  @UnitTest
  def concurrentTagName(): Unit = {
    using threads 4 invocations 8 invoke {
      val id: Int = this.persistenceUtils.findOrCreateTagName("some description").idTagName
      // id should be the same
      this.persistenceUtils.findOrCreateTagName("some description").idTagName should be(id)
      // id should be different with a new insert
      this.persistenceUtils.findOrCreateTagName("some other description").idTagName should not be id
    }
  }
}


object ConcurrentWriteTest extends CorePersistenceAware {

  /** Drops, re-creates the schema, and inserts some basic seed data. */
  @BeforeAll
  def recreateSchema(): Unit = {
    CorePersistenceUtils.dropSchema()
    Connection.refresh()
    CorePersistenceUtils.initSchema()
  }
}
