package com.workday.warp.persistence

import com.workday.warp.persistence.Tables.profile.api._
import org.pmw.tinylog.Logger

import scala.util.{Failure, Success}

/**
  * Trait for dropping a schema.
  *
  * Placed alone in a trait so this can be mixed in as needed without initializing PersistenceUtils companion object.
  *
  * Created by tomas.mccandless on 2/9/18.
  */
trait CanDropCoreSchema extends Connection {

  /**
    * Drops the schema, if it exists.
    *
    * If mysql is being used, parses out the schema name from the url, and drops the schema.
    * If another db type is being used, uses the generated drop statements in [[Tables.schema]].
    *
    * You probably need to call [[Connection.refresh()]] after calling this.
    */
  @throws[RuntimeException]("when there is a database error other than relating to non-existing schema")
  def dropSchema(): Unit = {
    val action = this.getMySQLDbName match {
      case Some(name) => sql"drop schema #$name".asUpdate
      case None => Tables.schema.drop
    }

    this.trySynchronously(action) match {
      case Success(_) =>
        Logger.info("dropped schema")
      // if we get errors about tables not existing, its likely because the schema doesn't exist to begin with.
      // the exception messages for mysql and h2 are slightly different and we need to match both.
      case Failure(exception) if exception.getMessage matches "Table '.*' doesn't exist" =>
        Logger.trace("schema does not exist")
      // note that java regex .* won't match line terminators
      case Failure(exception) if exception.getMessage matches "Table \".*\" not found; SQL statement:\n.*" =>
        Logger.trace("schema does not exist")
      case Failure(exception) =>
        throw exception
    }
  }
}
