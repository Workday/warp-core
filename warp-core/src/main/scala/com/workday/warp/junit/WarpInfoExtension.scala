package com.workday.warp.junit

import java.util.stream.Stream

import com.workday.warp.junit.TestIdConverters.extensionContextHasTestId
import org.junit.jupiter.api.extension.{ExtensionContext, TestTemplateInvocationContext, TestTemplateInvocationContextProvider}
import org.junit.platform.commons.util.AnnotationUtils


/**
  * A simple provider of invocation contexts that is solely meant to provide access to [[WarpInfo]], including testId.
  *
  * Note that we don't modify display name, or register a [[MeasurementExtension]] at all here.
  *
  * This extension should be used when your tests only need access to their names, and you want more fine-grained control
  * over the ordering between extensions, eg if a [[MeasurementExtension]] before and after hooks would conflict with
  * any other before/after hooks you may have.
  *
  * Created by tomas.mccandless on 10/23/20.
  */
trait WarpInfoExtensionLike extends TestTemplateInvocationContextProvider {

  /**
    * We only support test templates that are annotated with [[WarpInfoProvided]].
    *
    * @param context [[ExtensionContext]] containing test method and display name.
    * @return whether the test method is annotated with [[WarpTest]].
    */
  override def supportsTestTemplate(context: ExtensionContext): Boolean = {
    AnnotationUtils.isAnnotated(context.getTestMethod, classOf[WarpInfoProvided])
  }


  /**
    * Provides a Stream of contexts corresponding to each invocation.
    *
    * In this case, we don't insert extra invocations, we only provide [[WarpInfo]] to a singular invocation.
    *
    * @param context [[ExtensionContext]] containing test method and display name.
    * @return a singleton [[Stream]] of invocation context.
    */
  override def provideTestTemplateInvocationContexts(context: ExtensionContext): Stream[TestTemplateInvocationContext] = {
    // TODO finalize behavior here. throw an exception? fall back on another testId?
    val info: WarpInfo = WarpInfo(context.getTestId.get)
    Stream.of(Seq(WarpInfoInvocationContext(context.getDisplayName, info)): _*)
  }
}

class WarpInfoExtension extends WarpInfoExtensionLike
