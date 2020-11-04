package com.workday.warp.junit

import java.lang.reflect.Method

import com.workday.warp.common.utils.Implicits.DecoratedOptional
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtensionContext

import scala.util.Try

/**
  * Ad-hoc polymorphism for constructing [[HasTestId]].
  *
  * Created by tomas.mccandless on 6/18/20.
  */
object TestIdConverters {

  implicit def testInfoHasTestId(info: TestInfo): HasTestId = new HasTestId {
    override def maybeTestClass: Try[Class[_]] = info.getTestClass.toTry
    override def maybeTestMethod: Try[Method] = info.getTestMethod.toTry
  }


  implicit def extensionContextHasTestId(context: ExtensionContext): HasTestId = new HasTestId {
    override def maybeTestClass: Try[Class[_]] = context.getTestClass.toTry
    override def maybeTestMethod: Try[Method] = context.getTestMethod.toTry
  }
}
