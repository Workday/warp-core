package com.workday.warp

/**
  * Configuration parameters for generating slick code
  *
  * Created by tomas.mccandless on 1/17/17.
  */
object Config {
  // Jdbc url
  val url: String = "jdbc:mysql://localhost:3306/warp?user=root&password=1234"
  // We'll use this to connect to the above url
  val jdbcDriver: String = "com.mysql.jdbc.Driver"
  // Profile used by the generated slick code
  val slickProfile: String = "slick.driver.MySQLDriver"
  // Package we'll generate code into
  val packageName: String = "com.workday.warp.persistence.model"
  // Trait to wrap around supertraits
  val traitsContainer: String = "TablesLike"
  // File name for supertraits
  val traitsFile: String = "TablesLike.scala"
}
