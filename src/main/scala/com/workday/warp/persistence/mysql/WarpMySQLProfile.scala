package com.workday.warp.persistence.mysql

import slick.jdbc.MySQLProfile

/**
  * Created by ruiqi.wang
  * Extended MySQL Profile that adds some standard MySQL functions to the slick DSL.
  */
trait WarpMySQLProfile extends MySQLProfile {

  trait WarpSlickAPI extends API with HasWarpSlickDsl with WarpSlickImplicits

  override val api: WarpSlickAPI = new WarpSlickAPI {}

}

object WarpMySQLProfile extends WarpMySQLProfile
