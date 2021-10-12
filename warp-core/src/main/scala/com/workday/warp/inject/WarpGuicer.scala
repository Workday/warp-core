package com.workday.warp.inject

import java.lang.reflect.Constructor
import com.google.inject.{AbstractModule, Guice, Injector}
import com.workday.warp.{TestId, TestIdImplicits}
import com.workday.warp.config.{PropertyEntry, WarpPropertyLike}
import com.workday.warp.TestIdImplicits._
import com.workday.warp.controllers.AbstractMeasurementCollectionController
import com.workday.warp.inject.modules.{DefaultWarpModule, HasWarpBindings}
import com.workday.warp.persistence.influxdb.InfluxDBClient
import com.workday.warp.persistence.{PersistenceAware, Tag}
import org.junit.jupiter.api.TestInfo
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Try}

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

  // Note: we DON'T extend WarpLogging here, because it needs WarpGuicer to be initialized,
  // and we don't want a circular dependency here. For a model of what happens, consider this:
  // object A {
  //   val b: String = B.a
  // }
  // object B {
  //   val a: String = A.b
  // }
  //
  // Which evaluates to this in the REPL:
  // scala> A.b
  // res0: String = null
  //
  // scala> B.a
  // res1: String = null
  //
  // Logging levels set via warp properties won't be applied to log entries emitted by this.
  @transient
  protected lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  private val moduleProp: String = "wd.warp.inject.module"
  private[inject] val moduleEnvVar: String = PropertyEntry(moduleProp).envVarName

  // TODO support csv values?
  // in that case, we don't need to ensure that at least one of the modules is the correct type (ControllerModule)
  private val maybeModuleClass: Option[String] = sys.env.get(this.moduleEnvVar).orElse(sys.props.get(this.moduleProp))

  private val moduleClass: Class[WarpModule] = maybeModuleClass match {
    case Some(className) =>
      logger.info(s"will attempt using module class $className")
      Class.forName(className).asInstanceOf[Class[WarpModule]]
    case None =>
      val module: Class[WarpModule] = classOf[DefaultWarpModule].asInstanceOf[Class[WarpModule]]
      logger.info(s"no system property set for ${this.moduleProp}. using default module ${module.getCanonicalName}")
      module
  }

  private val moduleConstructor: Constructor[WarpModule] = this.moduleClass.getConstructor(
    classOf[TestId], classOf[List[Tag]]
  )

  val baseModule: WarpModule = this.moduleConstructor.newInstance(TestId.undefined, Nil)

  // won't be used for creating any controllers
  val baseInjector: Injector = Guice.createInjector(this.baseModule)


  def getInstance[T](`class`: Class[T]): T = this.baseInjector.getInstance(`class`)

  /**
    * Constructs a [[WarpModule]] dependency injection module and uses those bindings to obtain a controller instance.
    *
    * Uses the module class as defined by system property `wd.warp.inject.module`.
    *
    * @param testId test identifier, typically fully qualified method signature.
    * @param tags tags to use for this test.
    * @return a measurement controller.
    */
  @deprecated("use getController(TestId, Iterable[Tag]) instead", since = "5.0.0")
  def getController(testId: String, tags: Iterable[Tag] = Seq.empty): AbstractMeasurementCollectionController = {
      this.getController(TestIdImplicits.string2TestId(testId), tags)
  }


  /**
    * Convenience method for obtaining a controller instance.
    *
    * @param info junit [[TestInfo]], typically obtained through a default [[org.junit.jupiter.api.extension.ParameterResolver]]
    * @param tags tags to use for this test.
    * @return a measurement controller.
    */
  def getController(info: TestInfo, tags: Iterable[Tag]): AbstractMeasurementCollectionController = {
    this.getController(testInfo2TestId(info), tags)
  }
  def getController(info: TestInfo): AbstractMeasurementCollectionController = this.getController(info, Nil)


  /**
    * Convenience method for obtaining a controller instance.
    *
    * @param testId a testId container.
    * @param tags tags to use for this test.
    * @return a measurement controller.
    */
  def getController(testId: TestId, tags: Iterable[Tag]): AbstractMeasurementCollectionController = {
    val module: WarpModule = this.moduleConstructor.newInstance(testId, tags.toList)
    this.getController(module)
  }
  def getController(testId: TestId): AbstractMeasurementCollectionController = this.getController(testId, Nil)


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
    Try(injector.getInstance(classOf[AbstractMeasurementCollectionController])).recoverWith {
      case e =>
        logger.error(s"${e.getMessage}", e)
        Failure(e)
    }.get
  }


  /** @return object with warp configuration [[com.workday.warp.config.PropertyEntry]]. */
  def getProperty: WarpPropertyLike = this.baseInjector.getInstance(classOf[WarpPropertyLike])

  /** @return a persistence class [[PersistenceAware]]. */
  def getPersistence: PersistenceAware = this.baseInjector.getInstance(classOf[PersistenceAware])

  /** @return an influxdb client [[InfluxDBClient]]. */
  def getInfluxDb: InfluxDBClient = this.baseInjector.getInstance(classOf[InfluxDBClient])
}
