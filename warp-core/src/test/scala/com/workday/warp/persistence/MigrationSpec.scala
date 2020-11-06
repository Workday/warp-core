package com.workday.warp.persistence

import com.workday.warp.config.CoreWarpProperty.WARP_DATABASE_URL
import com.workday.warp.dsl._
import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.{AfterAll, BeforeAll}
import org.junit.jupiter.api.parallel.Isolated
import org.pmw.tinylog.Logger

/**
  * Created by tomas.mccandless on 6/14/17.
  *
  * // TODO refactor BeforeOnce and AfterOnce
  */
@Isolated
class MigrationSpec extends WarpJUnitSpec with Connection with CorePersistenceAware with MigrateSchemaLike {

  val maybeFlyway: Option[Flyway] = this.persistenceUtils.maybeFlyway()

  /** Checks that schema migration works when multiple clients are attempting to migrate the schema. */
  @UnitTest
  def concurrentMigration(): Unit = {
    using threads 8 invocations 32 invoke {
      if (this.maybeFlyway.isDefined) this.migrate()
      else Logger.debug(s"migrations are only supported for mysql. check the value of ${WARP_DATABASE_URL.propertyName}")
    }
  }
}

object MigrationSpec {
  /** Drops the schema once before we start our concurrent test. */
  @BeforeAll
  def dropSchema(): Unit = {
    CorePersistenceUtils.dropSchema()
    Connection.refresh()
  }

  /** Ensures the schema is in a sane state after our test. (H2) */
  @AfterAll
  def initSchema(): Unit = CorePersistenceUtils.initSchema()
}