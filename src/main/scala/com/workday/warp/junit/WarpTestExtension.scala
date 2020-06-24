package com.workday.warp.junit

import java.lang.reflect.Method
import java.util.stream.Stream

import org.junit.jupiter.api.extension.{ExtensionContext, TestTemplateInvocationContext, TestTemplateInvocationContextProvider}
import org.junit.platform.commons.util.{AnnotationUtils, Preconditions}

import scala.compat.java8.StreamConverters._

/** TestTemplate for running WarpTests.
 *
 * We emit a stream of invocation contexts corresponding to warmups and measured trials based on [[WarpTest]] annotation.
 *
 * See [[org.junit.jupiter.api.RepeatedTest]].
 *
 * Created by tomas.mccandless on 6/18/20.
 */
class WarpTestExtension extends TestTemplateInvocationContextProvider {

  /**
   * We only support test templates that are annotated with [[WarpTest]].
   *
   * @param context
   * @return
   */
  override def supportsTestTemplate(context: ExtensionContext): Boolean = {
    AnnotationUtils.isAnnotated(context.getTestMethod, classOf[WarpTest])
  }


  /**
   * Provides a Stream of contexts corresponding to each warmup and measured trial.
   *
   * This is how we influence the JUnit execution schedule and insert our measurement extensions.
   *
   * @param context
   * @return
   */
  override def provideTestTemplateInvocationContexts(context: ExtensionContext): Stream[TestTemplateInvocationContext] = {
    val testMethod: Method = context.getRequiredTestMethod
    val displayName: String = context.getDisplayName
    // we know this will be defined, due to our test template constraints
    val warpTest: WarpTest = AnnotationUtils.findAnnotation(testMethod, classOf[WarpTest]).get

    val numWarmups: Int = validateWarmups(warpTest, testMethod)
    val warmups: Seq[WarpTestInvocationContext] = (1 to numWarmups).map(WarpTestInvocationContext(displayName, "warmup", _, numWarmups))

    val numTrials: Int = validateMeasuredReps(warpTest, testMethod)
    // tweak our display name for warmups vs trials
    val trials: Seq[WarpTestInvocationContext] = (1 to numTrials).map(WarpTestInvocationContext(
      displayName,
      "trial",
      _,
      numTrials,
      // measured trial reps should have an additional measurement extension
      additionalExtensions = Seq(new MeasurementExtension)
    ))

    Stream.concat(warmups.seqStream, trials.seqStream)
  }

  private def validateMeasuredReps(warpTest: WarpTest, method: Method): Int = {
    val repetitions: Int = warpTest.trials
    Preconditions.condition(
      repetitions > 0,
      () => String.format("Configuration error: @WarpTest on method [%s] must be declared with positive 'trials'.", method)
    )
    repetitions
  }

  private def validateWarmups(warpTest: WarpTest, method: Method): Int = {
    val repetitions: Int = warpTest.warmups
    Preconditions.condition(
      repetitions >= 0,
      () => String.format("Configuration error: @WarpTest on method [%s] must be declared with non-negative 'warmups'.", method)
    )
    repetitions
  }
}

