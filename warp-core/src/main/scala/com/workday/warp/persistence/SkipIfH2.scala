package com.workday.warp.persistence

import slick.jdbc.H2Profile

/**
 * Used to skip tests that should only run against MySQL.
 *
 * Created by tomas.mccandless on 3/29/21.
 */
trait SkipIfH2 extends HasProfile {

  def skipIfH2[T](f: => T): Option[T] = {
    this.profile match {
      case H2Profile => None
      case _ => Option(f)
    }
  }
}
