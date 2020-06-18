package com.workday.warp.junit

import com.workday.warp.common.CoreConstants
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtensionContext

import scala.compat.java8.OptionConverters._

/**
  * Created by tomas.mccandless on 6/17/20.
  */
trait TestIdSupport {

  /**
    *
    *
    * @param info
    * @return
    */
  def getTestId(info: TestInfo): String = {
    val maybeId: Option[String] = for {
      clazz <- info.getTestClass.asScala
      method <- info.getTestMethod.asScala
    } yield clazz.getCanonicalName + "." + method

    maybeId.getOrElse(CoreConstants.UNDEFINED_TEST_ID)
  }



  def getTestId(context: ExtensionContext): String = {
    context.getRequiredTestClass.getCanonicalName + "." + context.getRequiredTestMethod
  }


  /**
    * Parses a Junit unique id to obtain a test id.
    *
    * @param id
    * @return
    */
  def fromUniqueId(id: String): String = {
    // [engine:junit-jupiter]/[class:com.workday.warp.junit.MeasurementCallbacksSpec]/[method:foo()]

    // use triple quotes to avoid escaping backslash
    val r = """\[engine:(.*)\]/\[class:(.*)\]/\[method:(.*)\((.*)\)\].*""".r
    id match {
      case r(engine, clazz, methodName, methodArgs) => clazz + "." + methodName
      case _ => throw new RuntimeException(s"unable to parse testId from $id")
    }
  }
}

// can be imported or mixed in
object TestId extends TestIdSupport
