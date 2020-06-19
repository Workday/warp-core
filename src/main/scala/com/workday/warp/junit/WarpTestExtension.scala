package com.workday.warp.junit

import java.lang.reflect.Method
import java.util.stream.{IntStream, Stream}

import org.junit.jupiter.api.extension.{ExtensionContext, TestTemplateInvocationContext, TestTemplateInvocationContextProvider}
import org.junit.platform.commons.util.{AnnotationUtils, Preconditions}

/**
  * Created by tomas.mccandless on 6/18/20.
  */
class WarpTestExtension extends TestTemplateInvocationContextProvider {

  override def supportsTestTemplate(context: ExtensionContext): Boolean = {
    AnnotationUtils.isAnnotated(context.getTestMethod, classOf[WarpTest])
  }


  override def provideTestTemplateInvocationContexts(context: ExtensionContext): Stream[TestTemplateInvocationContext] = {
    val testMethod: Method = context.getRequiredTestMethod
    val displayName: String = context.getDisplayName
    val repeatedTest: WarpTest = AnnotationUtils.findAnnotation(testMethod, classOf[WarpTest]).get
    val totalRepetitions: Int = validateTotalRepetitions(repeatedTest, testMethod)
    val formatter: WarpTestDisplayNameFormatterLike = displayNameFormatter(repeatedTest, testMethod, displayName)

    IntStream
      .rangeClosed(1, totalRepetitions)
      .mapToObj((repetition: Int) => WarpTestInvocationContext(repetition, totalRepetitions, formatter))
  }

  private def validateTotalRepetitions(warpTest: WarpTest, method: Method): Int = {
    val repetitions: Int = warpTest.invocations
    Preconditions.condition(
      repetitions > 0,
      () => String.format("Configuration error: @RepeatedTest on method [%s] must be declared with a positive 'value'.", method)
    )
    repetitions
  }

  private def displayNameFormatter(warpTest: WarpTest, method: Method, displayName: String): WarpTestDisplayNameFormatterLike = {
    val pattern: String = Preconditions.notBlank(
      warpTest.name.trim,
      () => String.format("Configuration error: @RepeatedTest on method [%s] must be declared with a non-empty name.", method)
    )
    WarpTestDisplayNameFormatter(pattern, displayName)
  }
}

