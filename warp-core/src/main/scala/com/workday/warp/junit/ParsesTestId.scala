package com.workday.warp.junit

import com.workday.warp.logger.WarpLogging

import scala.util.matching.Regex

/** Utility methods for constructing test ids.
  *
  * Created by tomas.mccandless on 6/17/20.
  */
trait ParsesTestId extends WarpLogging {
  import ParsesTestId._

  /**
    * Parses a Junit unique id to obtain a test id.
    *
    * @param uid a JUnit unique id. example:
    *   [engine:junit-jupiter]/[class:com.workday.warp.junit.MeasurementCallbacksSpec]/[method:foo()]
    * @return a test id in the form of a fully qualified method name, or None.
    */
  def fromUniqueId(uid: String): Option[String] = uid match {
    case uidPattern(_, clazz, _, methodName, _) =>
      Option(s"$clazz.$methodName")
    case _ =>
      logger.debug(s"unable to parse testId from $uid")
      None
  }
}

object ParsesTestId {
  // use triple quotes to avoid escaping backslash
  val uidPattern: Regex = """\[engine:(.*)\]/\[class:(.*)\]/\[(method|test-template):(.*)\((.*)\)\].*""".r
}
