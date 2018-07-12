package com.workday.warp.persistence

import com.workday.telemetron.annotation.{AfterOnce, BeforeOnce, Schedule}
import com.workday.warp.common.CoreWarpProperty.WARP_DATABASE_URL
import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.flywaydb.core.Flyway
import org.junit.experimental.categories.Category
import org.junit.Test
import org.pmw.tinylog.Logger

/**
  * Created by tomas.mccandless on 6/14/17.
  */
class MigrationSpec extends WarpJUnitSpec with Connection with CorePersistenceAware with MigrateSchemaLike {


  override val maybeFlyway: Option[Flyway] = this.persistenceUtils.maybeFlyway()

  /** Drops the schema once before we start our concurrent test. */
  @BeforeOnce
  def dropSchema(): Unit = CorePersistenceUtils.dropSchema()

  /** Ensures the schema is in a sane state after our test. (H2) */
  @AfterOnce
  def initSchema(): Unit = CorePersistenceUtils.initSchema()


  /** Checks that schema migration works when multiple clients are attempting to migrate the schema. */
  @Test
  @Category(Array(classOf[UnitTest]))
  @Schedule(invocations = 32, threads = 8)
  def concurrentMigration(): Unit = {
    if (this.maybeFlyway.isDefined) this.migrate()
    else Logger.info(s"migrations are only supported for mysql. check the value of ${WARP_DATABASE_URL.propertyName}")
  }
}
