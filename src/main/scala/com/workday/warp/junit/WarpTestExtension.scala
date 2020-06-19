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
    val totalWarmups: Int = validateWarmups(repeatedTest, testMethod)
    val totalRepetitions: Int = validateMeasuredReps(repeatedTest, testMethod)
    val warmupFormatter: WarpTestDisplayNameFormatter = displayNameFormatter(repeatedTest, testMethod, displayName, "warmup")

    val warmups = IntStream
      .rangeClosed(1, totalWarmups)
      .mapToObj((repetition: Int) => WarpTestInvocationContext(repetition, totalWarmups, warmupFormatter))
    // measured reps should have an additional measurement extension

    val measuredRepFormatter: WarpTestDisplayNameFormatterLike = warmupFormatter.copy(op = "measured rep")
    val measuredReps = IntStream
      .rangeClosed(1, totalRepetitions)
      .mapToObj((repetition: Int) => WarpTestInvocationContext(
        repetition,
        totalRepetitions,
        measuredRepFormatter,
        Seq(new MeasurementExtension)
      ))

    Stream.concat(warmups, measuredReps)
  }

  private def validateMeasuredReps(warpTest: WarpTest, method: Method): Int = {
    val repetitions: Int = warpTest.invocations
    Preconditions.condition(
      repetitions > 0,
      () => String.format("Configuration error: @WarpTest on method [%s] must be declared with a positive 'invocations'.", method)
    )
    repetitions
  }

  private def validateWarmups(warpTest: WarpTest, method: Method): Int = {
    val repetitions: Int = warpTest.warmupInvocations
    Preconditions.condition(
      repetitions > 0,
      () => String.format("Configuration error: @WarpTest on method [%s] must be declared with a positive 'warmupInvocations'.", method)
    )
    repetitions
  }

  private def displayNameFormatter(warpTest: WarpTest, method: Method, displayName: String, op: String): WarpTestDisplayNameFormatter = {
    val pattern: String = Preconditions.notBlank(
      warpTest.name.trim,
      () => String.format("Configuration error: @WarpTest on method [%s] must be declared with a non-empty name.", method)
    )
    WarpTestDisplayNameFormatter(pattern, displayName, op)
  }
}

