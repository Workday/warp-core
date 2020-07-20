package com.workday.telemetron.junit

import java.lang.annotation.Annotation
import java.lang.reflect.Method

import com.workday.telemetron.annotation.{AfterOnce, BeforeOnce}
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.{FrameworkMethod, Statement}
import org.pmw.tinylog.Logger

import scala.util.{Failure, Success, Try}

/**
  * Control structure for creating [[SurroundOnceStatement]].
  *
  * Created by vignesh.kalidas on 2/9/17.
  */
class SurroundOnceRule extends TestRule {

  /**
    * Required method from extending TestRule; creates a new SurroundOnceStatement with all the annotated methods
    *
    * @param base base Statement to wrap
    * @param description describes a test that needs to be run
    * @return a new Statement
    */
  override def apply(base: Statement, description: Description): Statement = {
    val testClass: Class[_] = description.getTestClass
    val methods: Array[Method] = testClass.getMethods
    val beforeOnceMethods: Array[FrameworkMethod] = getMethodsAnnotatedWith(classOf[BeforeOnce], fromMethods = methods)
    val afterOnceMethods: Array[FrameworkMethod] = getMethodsAnnotatedWith(classOf[AfterOnce], fromMethods = methods)

    // `getConstructor` is a variadic method, so by calling it with no parameters, it attempts to get the constructor
    //   with no parameters, throwing an exception if one does not exist. This will happen for test classes that are
    //   @Parameterized.
    Try(testClass.getConstructor()) match {
      case Success(constructor) =>
        new SurroundOnceStatement(base, beforeOnceMethods, afterOnceMethods, constructor.newInstance())
      case Failure(_) =>
        Logger.error("Test class has no constructor with zero parameters, cannot make use of SurroundOnce annotations")
        base
    }
  }

  /**
    * Grab a list of FrameworkMethods that has the given annotation
    *
    * @param annotationClass the class of annotation
    * @param fromMethods the list of methods to filter through
    * @tparam T the annotationClass parameter should be a type of Annotation
    * @return list of FrameworkMethods that contain the given
    */
  def getMethodsAnnotatedWith[T <: Annotation](annotationClass: Class[T], fromMethods: Array[Method]): Array[FrameworkMethod] = {
    fromMethods filter(_.isAnnotationPresent(annotationClass)) map(new FrameworkMethod(_))
  }
}
