package com.workday.warp

import java.lang.reflect.Method
import com.workday.warp.config.CoreConstants
import com.workday.warp.utils.Implicits.{DecoratedOption, DecoratedOptional}
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtensionContext
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success, Try}

/** Logic for constructing a testId given a testClass and testMethod.
  *
  * This is our central point for test identification.
  *
  * Multiple Junit interfaces [[org.junit.jupiter.api.extension.ExtensionContext]] and [[org.junit.jupiter.api.TestInfo]],
  * for example, declare methods `getTestClass` and `getTestMethod`, but share no common supertype.
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
  lazy val maybeId: Try[String] = for {
    className: String <- this.maybeTestClass.map(_.getCanonicalName)
    method: String <- this.maybeTestMethod.map(_.getName)
  } yield s"$className.$method"

  /**
    * Unsafe variant of `maybeId`
    *
    * @throws
    * @return some fully qualified method name.
    */
  @throws[RuntimeException]
  final def id: String = this.maybeId.get

  /** @return a hashCode for this [[TestId]]. Currently only considers string id field, generally fully qualified method signature. */
  override def hashCode: Int = this.id.hashCode

  /**
    * Equality check. Currently only considers `id`.
    *
    * @param other another object to compare to.
    * @return whether this [[TestId]] is equal to `other`.
    */
  override def equals(other: Any): Boolean = {
    other != None.orNull && other.isInstanceOf[TestId] && other.asInstanceOf[TestId].id == this.id
  }
}


object TestId {

  /** A default undefined [[TestId]]. */
  lazy val undefined: TestId = new TestId(Failure(new ClassNotFoundException), Failure(new NoSuchMethodException)) {
    override lazy val maybeId: Try[String] = Success(CoreConstants.UNDEFINED_TEST_ID)
  }

  @transient
  protected lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  /**
    * Constructs a [[TestId]] from a [[TestInfo]], usually obtained from a junit test method parameter.
    *
    * @param info a [[TestInfo]].
    * @return a [[TestId]].
    */
  def fromTestInfo(info: TestInfo): TestId = TestId(info.getTestClass.toTry, info.getTestMethod.toTry)

  /**
    * Constructs a [[TestId]] from an [[ExtensionContext]], usually obtained from a junit extension hook.
    *
    * @param context a [[ExtensionContext]].
    * @return a [[TestId]].
    */
  def fromExtensionContext(context: ExtensionContext): TestId = TestId(context.getTestClass.toTry, context.getTestMethod.toTry)

  /**
    * Constructs a [[TestId]] from a [[String]].
    *
    * Attempts to parse a test class and test method from the signature, however note that there may be overloaded
    * test methods.
    *
    * If we are unable to parse a test
    *
    * @param str treated as a fully qualified method signature.
    * @return a [[TestId]].
    */
  def fromString(str: String): TestId = {
    val className: String = str take str.lastIndexOf('.')
    val methodName: String = str drop str.lastIndexOf('.') + 1

    val maybeTestClass: Try[Class[_]] = Try(Class.forName(className))
    val maybeTestMethod: Try[Method] = for {
      cls <- maybeTestClass
      methods: Array[Method] = cls.getMethods.filter(_.getName == methodName)
      _ = if (methods.length > 1) {
        logger.warn(s"detected overloaded methods for signature $str, annotation processing may not work as expected.")
      }
      method <- methods.headOption.toTry
    } yield method

    new TestId(maybeTestClass, maybeTestMethod) {
      override lazy val maybeId: Try[String] = Success(str)
    }
  }
}
