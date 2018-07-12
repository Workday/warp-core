package com.workday.warp.persistence

import com.workday.warp.common.CoreWarpProperty.WARP_DATABASE_DRIVER
import com.workday.warp.common.exception.WarpConfigurationException
import slick.jdbc.JdbcProfile

/**
  * Extends the generated model with an overridden jdbc profile and some convenience functions.
  * Your code should probably be referencing this object rather than [[model.Tables]].
  *
  * Created by tomas.mccandless on 2/1/17.
  */
object Tables extends model.Tables with CommonTables with HasProfile

object TablesLike extends model.TablesLike


object Drivers {

  val mysql: String = "com.mysql.jdbc.Driver"
  val h2: String = "org.h2.Driver"

  /**
    * Creates a [[WarpConfigurationException]] with a useful message relating to an unsupported driver.
    *
    * @param driver the unsupported driver.
    * @return a [[WarpConfigurationException]] for an unsupported driver.
    */
  def unsupportedDriverException(driver: String): WarpConfigurationException = {
    new WarpConfigurationException(s"unsupported persistence driver: $driver. must be one of (${Drivers.mysql}, ${Drivers.h2})")
  }
}

trait CommonTables {
  // pass to row constructors as a dummy id when we're inserting something that has an autoinc
  val nullId: Int = 0

  val disableForeignKeys: String = WARP_DATABASE_DRIVER.value match {
    case Drivers.mysql => "SET FOREIGN_KEY_CHECKS=0;"
    case Drivers.h2 => "SET REFERENTIAL_INTEGRITY FALSE;"
    case unsupported => throw Drivers.unsupportedDriverException(unsupported)
  }

  val enableForeignKeys: String = WARP_DATABASE_DRIVER.value match {
    case Drivers.mysql => "SET FOREIGN_KEY_CHECKS=1;"
    case Drivers.h2 => "SET REFERENTIAL_INTEGRITY TRUE;"
    case unsupported => throw Drivers.unsupportedDriverException(unsupported)
  }
}


trait HasProfile {
  // shameless assumption
  val profile: JdbcProfile = WARP_DATABASE_DRIVER.value match {
    case Drivers.mysql => slick.jdbc.MySQLProfile
    case Drivers.h2 => slick.jdbc.H2Profile
    case unsupported => throw Drivers.unsupportedDriverException(unsupported)
  }
}
