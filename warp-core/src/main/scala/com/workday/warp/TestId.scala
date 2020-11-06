package com.workday.warp

import java.lang.reflect.Method

import com.workday.warp.config.CoreConstants

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
trait TestId {

  def maybeTestClass: Try[Class[_]]

  def maybeTestMethod: Try[Method]

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

  def empty: TestId = new TestId {
    override def maybeTestClass: Try[Class[_]] = Failure(new ClassNotFoundException)

    override def maybeTestMethod: Try[Method] = Failure(new NoSuchMethodException)

    override lazy val maybeTestId: Try[String] = Success(CoreConstants.UNDEFINED_TEST_ID)
  }
}
