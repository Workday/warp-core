package com.workday.warp

import java.lang.reflect.Method

import com.workday.warp.common.utils.Implicits.DecoratedOptional
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtensionContext

import scala.util.Try

/**
  * Ad-hoc polymorphism for constructing [[TestId]].
  *
  *
  * This provides some flexibility in terms of multiple entrypoints into our framework,
  * and avoids the boilerplate of multiple method overloadings.
  *
  * Java users can statically import and explicitly call these methods.
  *
  * Created by tomas.mccandless on 6/18/20.
  */
object TestIdImplicits {


  /**
    * Constructs a [[TestId]] from a [[TestInfo]].
    *
    * @param info a [[TestInfo]], usually obtained from a default [[org.junit.jupiter.api.extension.ParameterResolver]]
    *             as part of a running test.
    * @return a [[TestId]] used to identify tests.
    */
  implicit def testInfoIsTestId(info: TestInfo): TestId = new TestId {
    override def maybeTestClass: Try[Class[_]] = info.getTestClass.toTry
    override def maybeTestMethod: Try[Method] = info.getTestMethod.toTry
  }


  /**
    * Constructs a [[TestId]] from an [[ExtensionContext]].
    *
    * @param context an [[ExtensionContext]], usually obtained as part of [[org.junit.jupiter.api.BeforeEach]] or other hook.
    * @return a [[TestId]] used to identify tests.
    */
  implicit def extensionContextIsTestId(context: ExtensionContext): TestId = new TestId {
    override def maybeTestClass: Try[Class[_]] = context.getTestClass.toTry
    override def maybeTestMethod: Try[Method] = context.getTestMethod.toTry
  }
}
