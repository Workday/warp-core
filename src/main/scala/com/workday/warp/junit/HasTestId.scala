package com.workday.warp.junit

import java.lang.reflect.Method

/**
  * Logic for constructing a testId given a testClass and testMethod.
  *
  * Multiple Junit interfaces [[org.junit.jupiter.api.extension.ExtensionContext]] and [[org.junit.jupiter.api.TestInfo]],
  * for example, declare methods `getTestClass` and `getTestMethod`, but share no common supertype.
  *
  * Created by tomas.mccandless on 6/18/20.
  */
trait HasTestId {

  def getTestClass: Option[Class[_]]

  def getTestMethod: Option[Method]

  /**
    * Attempts to construct a fully qualified method name.
    *
    * TODO make sure this works for dynamic tests
    *
    * @return Some fully qualified method name, or [[None]].
    */
  def getTestId: Option[String] = for {
    clazz: String <- this.getTestClass.map(_.getCanonicalName)
    method: String <- this.getTestMethod.map(_.getName)
  } yield s"$clazz.$method"
}


