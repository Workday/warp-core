package com.workday.warp.junit

import com.workday.warp.collectors.AbstractMeasurementCollectionController
import com.workday.warp.inject.WarpGuicer
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import org.junit.jupiter.api.extension.{AfterEachCallback, BeforeEachCallback, ExtensionContext}
import org.pmw.tinylog.Logger

/**
  * JUnit callbacks for measurement.
  *
  * Extensions must be stateless. There are no guarantees about when this is instantiated.
  *
  * Created by tomas.mccandless on 6/17/20.
  */
trait MeasurementExtensionLike extends BeforeEachCallback with AfterEachCallback with TestIdSupport {
  import MeasurementExtensionLike._

  /**
    * Begins measurement collection and stores controller in `context`.
    *
    * @param context
    */
  override def beforeEach(context: ExtensionContext): Unit = {
    // unique for this execution. repeated invocations of the same test will have different ids.
    val uniqueId: String = context.getUniqueId
    Logger.info(s"measuring junit: $uniqueId")
    val testId: String = this.fromUniqueId(uniqueId)
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
    val controller: AbstractMeasurementCollectionController = this.getStore(context)
      .get(controllerKey)
      .asInstanceOf[AbstractMeasurementCollectionController]
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
  protected def getStore(context: ExtensionContext): ExtensionContext.Store = {
    context.getStore(Namespace.create(context.getUniqueId))
  }
}

object MeasurementExtensionLike {
  val controllerKey: String = "controller"
}

class MeasurementExtension extends MeasurementExtensionLike

