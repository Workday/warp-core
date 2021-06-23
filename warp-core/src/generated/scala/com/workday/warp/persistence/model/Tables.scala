package com.workday.warp.persistence.model
// !!! AUTO-GENERATED Slick data model, do not modify.
// scalastyle:off
import com.workday.warp.persistence.TablesLike._

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import slick.model.ForeignKeyAction

  /** DDL for all tables. Call .create to execute. */
  lazy val schema = 
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  object RowTypeClasses {
  }

  // scalastyle:on
}
// scalastyle: on
