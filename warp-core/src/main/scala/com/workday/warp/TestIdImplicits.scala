package com.workday.warp

import java.lang.reflect.Method

import com.workday.warp.utils.Implicits.DecoratedOptional
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtensionContext
import com.workday.warp.utils.Implicits.DecoratedOption
import org.pmw.tinylog.Logger

import scala.util.{Success, Try}

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


  /**
    * Constructs a [[TestId]] from a fully qualified method signature.
    *
    * Included mainly for java interop and backwards compatibility, however,
    * note that there may be overloaded ambiguous methods. Prefer using `testInfoIsTestId` over this.
    *
    * @param signature
    * @return
    */
  implicit def methodSignatureIsTestId(signature: String): TestId = new TestId {
    private lazy val className: String = signature take signature.lastIndexOf('.')
    private lazy val methodName: String = testId drop testId.lastIndexOf('.') + 1
    // return None if we can't locate the class
    // we have this double monadic nesting to avoid returning a Success(null)
    override def maybeTestClass: Try[Class[_]] = Try(Option(Class.forName(className)).toTry).flatten

    override def maybeTestMethod: Try[Method] = for {
      cls <- maybeTestClass
      methods: Array[Method] = cls.getMethods.filter(_.getName == methodName)
      _ = if (methods.length > 1) {
        Logger.warn(s"detected overloaded methods for signature $testId, annotation processing may not work as expected.")
      }
      method <- methods.headOption.toTry
    } yield method

    override lazy val maybeTestId: Try[String] = Success(signature)
  }
}
