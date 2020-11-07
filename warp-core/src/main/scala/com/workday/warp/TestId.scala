package com.workday.warp

import java.lang.reflect.Method

import com.workday.warp.config.CoreConstants
import com.workday.warp.utils.Implicits.{DecoratedOption, DecoratedOptional}
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtensionContext
import org.pmw.tinylog.Logger

import scala.util.{Failure, Success, Try}

/** Logic for constructing a testId given a testClass and testMethod.
  *
  * This is our central point for test identification.
  *
  * Multiple Junit interfaces [[org.junit.jupiter.api.extension.ExtensionContext]] and [[org.junit.jupiter.api.TestInfo]],
  * for example, declare methods `getTestClass` and `getTestMethod`, but share no common supertype.
  *
  * We use ad-hoc polymorphism to declare [[TestId]] instances for [[org.junit.jupiter.api.TestInfo]] and
  * [[org.junit.jupiter.api.extension.ExtensionContext]].
  *
  * Created by tomas.mccandless on 6/18/20.
  */
case class TestId(maybeTestClass: Try[Class[_]], maybeTestMethod: Try[Method]) {

  /**
    * Attempts to construct a fully qualified method name.
    *
    * TODO make sure this works for dynamic tests
    *
    * @return Some fully qualified method name, or [[None]].
    */
  lazy val maybeTestId: Try[String] = for {
    className: String <- this.maybeTestClass.map(_.getCanonicalName)
    method: String <- this.maybeTestMethod.map(_.getName)
  } yield s"$className.$method"

  /**
    * Unsafe variant of `maybeTestId`
    *
    * @throws
    * @return some fully qualified method name.
    */
  @throws[RuntimeException]
  // TODO consider renaming this to methodSignature
  final def testId: String = this.maybeTestId.get
}


object TestId {

  lazy val empty: TestId = new TestId(Failure(new ClassNotFoundException()), Failure(new NoSuchMethodException())) {
    override lazy val maybeTestId: Try[String] = Success(CoreConstants.UNDEFINED_TEST_ID)
  }

  def fromTestInfo(info: TestInfo): TestId = TestId(info.getTestClass.toTry, info.getTestMethod.toTry)

  def fromExtensionContext(context: ExtensionContext): TestId = TestId(context.getTestClass.toTry, context.getTestMethod.toTry)

  /**
    * Constructs a [[TestId]] from a fully qualified method signature.
    *
    * Attempts to parse a test class and test method from the signature, however note that there may be overloaded
    * test methods.
    *
    * @param signature
    * @return
    */
  def fromMethodSignature(signature: String): TestId = {
    val className: String = signature take signature.lastIndexOf('.')
    val methodName: String = signature drop signature.lastIndexOf('.') + 1

    val maybeTestClass: Try[Class[_]] = Try(Class.forName(className))
    val maybeTestMethod: Try[Method] = for {
      cls <- maybeTestClass
      methods: Array[Method] = cls.getMethods.filter(_.getName == methodName)
      _ = if (methods.length > 1) {
        Logger.warn(s"detected overloaded methods for signature $signature, annotation processing may not work as expected.")
      }
      method <- methods.headOption.toTry
    } yield method

    new TestId(maybeTestClass, maybeTestMethod) {
      override lazy val maybeTestId: Try[String] = Success(signature)
    }
  }
}
