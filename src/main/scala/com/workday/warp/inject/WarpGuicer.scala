package com.workday.warp.inject

import java.lang.reflect.Constructor

import com.google.inject.{AbstractModule, Guice, Injector}
import com.workday.warp.collectors.AbstractMeasurementCollectionController
import com.workday.warp.common.WarpPropertyLike
import com.workday.warp.inject.modules.{DefaultWarpModule, HasWarpBindings}
import com.workday.warp.persistence.Tag
import org.pmw.tinylog.Logger

/**
  * Centralized location for dependency injection.
  *
  * Determines which module is appropriate, and creates an instance of [[com.google.inject.Injector]] with that module.
  *
  * The module is instantiated based on the value of the system property `wd.warp.inject.module`, which should be set
  * to the fully qualified class name of the concrete module defining your bindings. Your module should mix in the trait
  * [[HasWarpBindings]].
  *
  * If that system property is not set, we'll use the default module [[DefaultWarpModule]].
  *
  * Created by tomas.mccandless on 11/6/17.
  */
object WarpGuicer {

  type WarpModule = AbstractModule with HasWarpBindings

  val moduleProp: String = "wd.warp.inject.module"

  // TODO support csv values?
  // in that case, we don't need to ensure that at least one of the modules is the correct type (ControllerModule)
  val maybeModuleClass: Option[String] = Option(System.getProperty(this.moduleProp))

  val moduleClass: Class[WarpModule] = maybeModuleClass match {
    case Some(className) =>
      Logger.info(s"will attempt using module class $className")
      Class.forName(className).asInstanceOf[Class[WarpModule]]
    case None =>
      val module: Class[WarpModule] = classOf[DefaultWarpModule].asInstanceOf[Class[WarpModule]]
      Logger.info(s"no system property set for ${this.moduleProp}. using default module ${module.getCanonicalName}")
      module
  }

  val moduleConstructor: Constructor[WarpModule] = this.moduleClass.getConstructor(
    classOf[String], classOf[List[Tag]]
  )

  val baseModule: WarpModule = this.moduleConstructor.newInstance("", List.empty[Tag])

  // won't be used for creating any controllers
  val baseInjector: Injector = Guice.createInjector(this.baseModule)


  def getInstance[T](`class`: Class[T]): T = this.baseInjector.getInstance(`class`)

  /**
    * Constructs a [[WarpModule]] dependency injection module and uses those bindings to obtain a controller instance.
    *
    * Uses the module class as defined by system property `wd.warp.inject.module`.
    *
    * @param testId
    * @param tags // TODO should become a seq
    * @return
    */
  def getController(testId: String, tags: List[Tag]): AbstractMeasurementCollectionController = {
    val module: WarpModule = this.moduleConstructor.newInstance(testId, tags)
    this.getController(module)
  }


  /**
    * Creates an [[Injector]] using the given module and uses that injector to obtain a controller instance.
    *
    * This signature is useful if you already have an instance of the module defining your bindings.
    *
    * @param module
    * @return an injected [[AbstractMeasurementCollectionController]].
    */
  def getController(module: WarpModule): AbstractMeasurementCollectionController = {
    // TODO measure the performance of creating this injector. it may be necessary to use assisted injection instead.
    val injector: Injector = Guice.createInjector(module)
    injector.getInstance(classOf[AbstractMeasurementCollectionController])
  }


  /** @return object with warp configuration [[com.workday.warp.common.PropertyEntry]]. */
  def getProperty: WarpPropertyLike = this.baseInjector.getInstance(classOf[WarpPropertyLike])
}
