package com.workday.warp.inject

import com.google.inject.{AbstractModule, Guice, Injector}
import com.workday.warp.collectors.{AbstractMeasurementCollectionController, DefaultMeasurementCollectionController}
import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.{CoreWarpProperty, WarpPropertyLike}
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  *
  * Created by tomas.mccandless on 11/6/17.
  */
class WarpGuicerSpec extends WarpJUnitSpec {

  /** Just an example with binding an instance. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def booleanInjectionSpec(): Unit = {

    class BooleanBindingModule extends AbstractModule {
      override def configure(): Unit = {
        bind(classOf[Boolean]).toInstance(true)
      }
    }

    val injector: Injector = Guice.createInjector(new BooleanBindingModule)

    val b = injector.getInstance(classOf[Boolean])
    b should be (true)
  }


  /** Checks that we bind the correct runtime implementation for collection controller. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def injectMeasurementCollectionController(): Unit = {
    val controller: AbstractMeasurementCollectionController = WarpGuicer.getController("com.workday.warp.some.test.id", List.empty)
    controller.getClass should be (classOf[DefaultMeasurementCollectionController])
  }


  /** Checks that we bind the correct runtime implementation for property. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def injectCoreWarpProperty(): Unit = {
    WarpGuicer.getProperty.getClass should be (CoreWarpProperty.getClass)
    WarpGuicer.getInstance(classOf[WarpPropertyLike]).getClass should be (CoreWarpProperty.getClass)
  }
}
