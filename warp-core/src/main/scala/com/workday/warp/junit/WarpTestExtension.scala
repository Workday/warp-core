package com.workday.warp.junit

import java.lang.reflect.Method
import java.util.stream.Stream

import com.workday.warp.TestIdImplicits.extensionContext2TestId
import org.junit.jupiter.api.extension.{ExtensionContext, TestTemplateInvocationContext, TestTemplateInvocationContextProvider}
import org.junit.platform.commons.util.{AnnotationUtils, Preconditions}

import scala.collection.JavaConversions._

/** TestTemplate for running and measuring WarpTests.
  *
  * We emit a stream of invocation contexts corresponding to warmups and measured trials based on [[WarpTest]] annotation.
  *
  * See [[org.junit.jupiter.api.RepeatedTest]].
  *
  * Created by tomas.mccandless on 6/18/20.
 */
trait WarpTestExtensionLike extends TestTemplateInvocationContextProvider {

  /**
    * We only support test templates that are annotated with [[WarpTest]].
    *
    * @param context [[ExtensionContext]] containing test method and display name.
    * @return whether the test method is annotated with [[WarpTest]].
    */
  override def supportsTestTemplate(context: ExtensionContext): Boolean = {
    AnnotationUtils.isAnnotated(context.getTestMethod, classOf[WarpTest])
  }


  /**
   * Provides a Stream of contexts corresponding to each warmup and measured trial.
   *
   * This is how we influence the JUnit execution schedule and insert our measurement extensions.
   *
   * @param context [[ExtensionContext]] containing test method and display name.
   * @return a [[Stream]] of invocation contexts corresponding to warmups and measured trials.
   */
  override def provideTestTemplateInvocationContexts(context: ExtensionContext): Stream[TestTemplateInvocationContext] = {
    val testMethod: Method = context.getRequiredTestMethod
    val displayName: String = context.getDisplayName
    // we know this will be defined, due to our test template constraints
    val warpTest: WarpTest = AnnotationUtils.findAnnotation(testMethod, classOf[WarpTest]).get

    val numWarmups: Int = validateWarmups(warpTest, testMethod)
    val numTrials: Int = validateMeasuredReps(warpTest, testMethod)
    val testId: String = context.id

    val warmups: Seq[WarpTestInvocationContext] = (1 to numWarmups).map { w =>
      val warmupInfo: WarpInfo = WarpInfo(testId, w, Warmup, numWarmups, numTrials)
      WarpTestInvocationContext(displayName, warmupInfo)
    }
    // tweak our display name for warmups vs trials
    val trials: Seq[WarpTestInvocationContext] = (1 to numTrials).map { t =>
      val trialInfo: WarpInfo = WarpInfo(testId, t, Trial, numWarmups, numTrials)
      WarpTestInvocationContext(
      displayName,
      trialInfo,
      // measured trial reps should have an additional measurement extension
      additionalExtensions = List(new MeasurementExtension)
    )}

    Stream.concat(warmups.stream(), trials.stream())
  }


  /**
    * Validates and returns number of measured trial reps, which must be positive.
    *
    * @param warpTest an annotation instance with `trials`.
    * @param method annotated method. Used to construct error messages.
    * @return number of measured trials.
    */
  @throws[RuntimeException]("when trials is non-positive")
  private def validateMeasuredReps(warpTest: WarpTest, method: Method): Int = {
    val repetitions: Int = warpTest.trials
    Preconditions.condition(
      repetitions > 0,
      String.format("Configuration error: @WarpTest on method [%s] must be declared with positive 'trials'.", method)
    )
    repetitions
  }


  /**
    * Validates and returns number of warmup reps, which must be non-negative.
    *
    * @param warpTest an annotation instance with `warmups`.
    * @param method annotated method. Used to construct error messages.
    * @return number of warmups
    */
  @throws[RuntimeException]("when warmups is negative")
  private def validateWarmups(warpTest: WarpTest, method: Method): Int = {
    val repetitions: Int = warpTest.warmups
    Preconditions.condition(
      repetitions >= 0,
      String.format("Configuration error: @WarpTest on method [%s] must be declared with non-negative 'warmups'.", method)
    )
    repetitions
  }
}

class WarpTestExtension extends WarpTestExtensionLike