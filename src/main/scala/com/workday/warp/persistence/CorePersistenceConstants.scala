package com.workday.warp.persistence

/**
  * Central class to store constants used for SQL access.
  *
  * Created with IntelliJ IDEA.
  * User: michael.ottati
  * Date: 1/16/14
  * Time: 1:52 PM
  */
trait CorePersistenceConstants {

  /* Note that this corresponds to the DB Schema */
  val DESCRIPTION_LENGTH = 255
  val SIGNATURE_LENGTH = 255

  // number of times to retry persistence operations
  val RETRIES = 8
}

// can be imported or mixed in
object CorePersistenceConstants extends CorePersistenceConstants
