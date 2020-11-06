package com.workday.warp.config

import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import org.junit.jupiter.api.Disabled
import org.pmw.tinylog.Logger

/**
  * Created by tomas.mccandless on 11/15/17.
  */
class PropertyInspectorSpec extends WarpJUnitSpec {


  /** Checks that we can use reflection to read all the property values. */
  @UnitTest
  def valuesTest(): Unit = {
    CoreWarpProperty.values should not be empty
    val level: String = CoreWarpProperty.WARP_CONSOLE_LOG_LEVEL.value
    Logger.info(s"console log level set to $level")
    PropertyInspector.values(CoreWarpProperty.getClass) should not be empty
  }

  /** Checks that we cannot read property values from a class, even if it has the correct mixins. */
  @UnitTest
  def classValuesTest(): Unit = {
    intercept[ClassNotFoundException] {
      PropertyInspector.values(classOf[NotAnObject])
    }
  }

  /** Checks that only public accessor methods with [[com.workday.warp.config.PropertyEntry]] return type are included. */
  @UnitTest
  def filteredReturnType(): Unit = {
    TestProperty.values should be (TestCoreWarpProperty.values)
  }


  /** Checks that we can read config from an object defined within a def. */
  // TODO add this functionality
  @UnitTest
  @Disabled("until this functionality is added")
  def innerConfig(): Unit = {
    object InnerProperty extends WarpPropertyLike with HasCoreWarpProperties

    InnerProperty.values should not be empty
  }


  object InstanceProperty extends WarpPropertyLike with HasCoreWarpProperties

  /** Checks that we can read config from an object defined within a class. */
  // TODO add this functionality
  @UnitTest
  @Disabled("until this functionality is added")
  def instanceConfig(): Unit = {
    InstanceProperty.values should be (TestCoreWarpProperty.values)
  }
}


object TestCoreWarpProperty extends WarpPropertyLike with HasCoreWarpProperties

// make a new property object that adds an extra field with the wrong type (Int)
object TestProperty extends WarpPropertyLike with HasCoreWarpProperties {
  // this is a publicly readable field with a type other than PropertyEntry, so it shouldn't show up in the values method.
  val imNotAProperty: Int = 0
}

// attempting to read property from this class should fail since there is no MODULE$ field we can reflect on
class NotAnObject extends WarpPropertyLike with HasCoreWarpProperties