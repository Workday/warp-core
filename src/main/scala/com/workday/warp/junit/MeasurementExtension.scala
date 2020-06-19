package com.workday.warp.junit

import java.util.Optional

import com.workday.warp.collectors.AbstractMeasurementCollectionController
import com.workday.warp.junit.TestIdConverters.extensionContextHasTestId
import com.workday.warp.inject.WarpGuicer
import org.junit.jupiter.api.extension.ExtensionContext.{Namespace, Store}
import org.junit.jupiter.api.extension.{AfterEachCallback, BeforeEachCallback, ExtensionContext}
import org.pmw.tinylog.Logger

/**
  * JUnit callbacks for measurement.
  *
  * Extensions must be stateless. There are no guarantees about when this is instantiated.
  *
  * Created by tomas.mccandless on 6/17/20.
  */
trait MeasurementExtensionLike extends BeforeEachCallback with AfterEachCallback {
  import MeasurementExtensionLike._

  /**
    * Begins measurement collection and stores controller in `context`.
    *
    * @param context
    */
  override def beforeEach(context: ExtensionContext): Unit = {
    // TODO trying to explore behavior here
    val testId: String = context.getTestId.get
    Logger.info(s"measuring junit: ${context.getUniqueId}")
    Logger.info(s"test id: $testId")
    val controller: AbstractMeasurementCollectionController = WarpGuicer.getController(testId)
    this.getStore(context).put(controllerKey, controller)
    controller.beginMeasurementCollection()
  }


  /**
    * Retrieves our measurement controller and ends measurement collection.
    *
    * @param context
    */
  override def afterEach(context: ExtensionContext): Unit = {
    // TODO don't record failures
    val controller: AbstractMeasurementCollectionController = this.getStore(context)
      .get(controllerKey)
      .asInstanceOf[AbstractMeasurementCollectionController]

//    val failed = context.publishReportEntry()
//    val failed: Optional[Throwable] = context.getExecutionException
    Logger.info(s"end measuring junit: ${context.getUniqueId}")
    controller.endMeasurementCollection()
  }


  /**
    * Gets a store given our extension context.
    *
    * We use JUnit context unique id for our namespace.
    *
    * @param context
    * @return
    */
  protected def getStore(context: ExtensionContext): Store = context.getStore(Namespace.create(context.getUniqueId))
}

object MeasurementExtensionLike {
  val controllerKey: String = "controller"
}

class MeasurementExtension extends MeasurementExtensionLike
