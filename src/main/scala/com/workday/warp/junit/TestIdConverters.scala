package com.workday.warp.junit

import java.lang.reflect.Method

import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtensionContext

import scala.compat.java8.OptionConverters._

/**
  * Ad-hoc polymorphism for constructing [[HasTestId]].
  *
  * Created by tomas.mccandless on 6/18/20.
  */
object TestIdConverters {

  implicit def testInfoHasTestId(info: TestInfo): HasTestId = new HasTestId {
    override def getTestClass: Option[Class[_]] = info.getTestClass.asScala
    override def getTestMethod: Option[Method] = info.getTestMethod.asScala
  }


  implicit def extensionContextHasTestId(context: ExtensionContext): HasTestId = new HasTestId {
    override def getTestClass: Option[Class[_]] = context.getTestClass.asScala
    override def getTestMethod: Option[Method] = context.getTestMethod.asScala
  }
}
