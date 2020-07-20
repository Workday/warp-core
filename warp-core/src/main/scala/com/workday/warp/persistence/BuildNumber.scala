package com.workday.warp.persistence

/**
  * Holds individual components of a build identifier.
  *
  * Created by tomas.mccandless on 1/30/17.
  */
case class BuildNumber(major: Int, minor: Int, patch: Int)


object BuildNumber {

  /**
    * Deconstructs `buildNumber` into a [[BuildNumber]] of major, minor, patch.
    *
    * @param buildNumber [[String]] containing build identifier, eg: "2016.20.314"
    * @return [[BuildNumber]] of major, minor, patch (as [[Int]])
    */
  @throws[RuntimeException]("when signature does not have 3 components")
  def apply(buildNumber: String): BuildNumber = {
    val tokens: Array[String] = buildNumber split "\\."

    if (tokens.length != 3) {
      throw new RuntimeException(s"invalid silver build number: $buildNumber (must have 3 components)")
    }

    tokens match { case Array(major, minor, patch) => BuildNumber(major.toInt, minor.toInt, patch.toInt) }
  }
}
