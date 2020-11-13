package com.workday.warp.junit

import com.workday.warp.{TestId, TrialResult}
import com.workday.warp.TestIdImplicits.extensionContext2TestId
import com.workday.warp.controllers.AbstractMeasurementCollectionController
import com.workday.warp.inject.WarpGuicer
import org.junit.jupiter.api.extension.ExtensionContext.{Namespace, Store}
import org.junit.jupiter.api.extension.{AfterEachCallback, BeforeEachCallback, ExtensionContext}
import org.pmw.tinylog.Logger

import scala.compat.java8.OptionConverters._
import scala.util.{Failure, Success, Try}

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
    // we would rather throw an exception here than record meaningless info under a default or undefined testId
    val testId: TestId = context
    Logger.info(s"measuring junit: ${context.getUniqueId}")
    Logger.debug(s"test id: $testId")
    // TODO this adds some latency on the first run should be warmed up somehow
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

    val testResult: Try[TrialResult[Unit]] = context.getExecutionException.asScala match {
      case Some(throwable) => Failure(throwable)
      case None => Success(TrialResult.empty)
    }
    Logger.info(s"end measuring junit: ${context.getUniqueId}")
    controller.endMeasurementCollection(testResult)
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
