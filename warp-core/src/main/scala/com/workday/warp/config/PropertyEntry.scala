package com.workday.warp.config

/**
  * Represents a single configuration option.
  *
  * Created by tomas.mccandless on 11/14/17.
  */
case class PropertyEntry(propertyName: String, isRequired: Boolean = false, defaultValue: Option[String] = None) {

  // used as a name for looking up a corresponding environment variable
  val envVarName: String = this.propertyName.map(_.toUpper).replace(".", "_")

  /** @return normalized configured value of this entry. Use this to obtain configuration values at runtime. */
  lazy val value: String = this.normalize(this.rawValue)

  /** @return raw configured value of this entry. */
  lazy val rawValue: String = WarpPropertyManager.valueOf(this.propertyName, this.isRequired)

  /**
    * Normalizes `rawPropertyValue` to conform to some expected value.
    *
    * This allows entries to validate or otherwise add constraints on their allowed values.
    *
    * The default implementation here is the identity function, with no further constraints or validation.
    *
    * @param rawPropertyValue raw configured value.
    * @return normalized configured value.
    */
  def normalize(rawPropertyValue: String): String = rawPropertyValue
}


object PropertyEntry {

  def apply(propertyName: String, isRequired: Boolean, defaultValue: String): PropertyEntry = {
    PropertyEntry(propertyName, isRequired, Option(defaultValue))
  }
}
