package com.workday.warp

import java.lang.reflect.Method

import com.workday.warp.common.utils.Implicits.DecoratedOptional
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtensionContext

import scala.util.Try

/**
  * Ad-hoc polymorphism for constructing [[TestId]].
  *
  * Created by tomas.mccandless on 6/18/20.
  */
object TestIdImplicits {


  /**
    * Constructs a [[TestId]] from a [[TestInfo]].
    *
    * @param info
    * @return
    */
  implicit def testInfoIsTestId(info: TestInfo): TestId = new TestId {
    override def maybeTestClass: Try[Class[_]] = info.getTestClass.toTry
    override def maybeTestMethod: Try[Method] = info.getTestMethod.toTry
  }


  /**
    * Constructs a [[TestId]] from a [[TestInfo]].
    *
    * @param context
    * @return
    */
  implicit def extensionContextIsTestId(context: ExtensionContext): TestId = new TestId {
    override def maybeTestClass: Try[Class[_]] = context.getTestClass.toTry
    override def maybeTestMethod: Try[Method] = context.getTestMethod.toTry
  }
}
