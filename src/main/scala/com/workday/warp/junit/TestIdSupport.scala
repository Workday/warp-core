package com.workday.warp.junit

import org.pmw.tinylog.Logger

/**
  * Created by tomas.mccandless on 6/17/20.
  */
trait TestIdSupport {
  import TestIdSupport._

  /**
    * Parses a Junit unique id to obtain a test id.
    *
    * Example unique id:
    *   [engine:junit-jupiter]/[class:com.workday.warp.junit.MeasurementCallbacksSpec]/[method:foo()]
    *
    * @param uid
    * @return
    */
  def fromUniqueId(uid: String): Option[String] = uid match {
    case uidPattern(_, clazz, _, methodName, _) =>
      Option(clazz + "." + methodName)
    case _ =>
      Logger.debug(s"unable to parse testId from $uid")
      None
  }
}

object TestIdSupport {
  // use triple quotes to avoid escaping backslash
  val uidPattern = """\[engine:(.*)\]/\[class:(.*)\]/\[(method|test-template):(.*)\((.*)\)\].*""".r
}

// can be imported or mixed in
object TestId extends TestIdSupport
