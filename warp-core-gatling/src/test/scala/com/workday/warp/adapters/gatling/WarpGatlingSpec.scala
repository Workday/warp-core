package com.workday.warp.adapters.gatling

import com.workday.warp.TestId
import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import com.workday.warp.TestIdImplicits.methodSignatureIsTestId

/**
  * Created by ruiqi.wang
  */
class WarpGatlingSpec extends WarpJUnitSpec {
  import WarpGatlingSpec._

  /**
    * Tests that classes that are instantiated without a user provided name defaults to use their user-defined class name.
    */
  @UnitTest
  def defaultTestName(): Unit = {
    defaultSimulation.canonicalName should equal (s"$packageName.DefaultSimulation")
    defaultFunSpec.canonicalName should equal (s"$packageName.DefaultFunSpec")
  }

  /**
    * Test that classes that are instantiated with a custom test name should be reflected in the canonical name.
    */
  @UnitTest
  def customTestName(): Unit = {
    customSimulation.canonicalName should equal (s"$packageName.MyCustomSimulationTest")
    customFunSpec.canonicalName should equal (s"$packageName.MyCustomFunSpec")
  }

  // TODO: Add integration tests for our own custom hooks.

  /**
    * Tests that WarpSimulations properly inherited from other WarpSimulations do not include its parent(s) within the canonical name.
    */
  @UnitTest
  def inheritedSimulation(): Unit = {
    childSpec.canonicalName should equal (s"$packageName.DAMNITJIM")
    babySpec.canonicalName should equal (s"$packageName.BABYCOMEBACK")
  }

  /**
    * Sanity check for custom hooks
    */
  @UnitTest
  def testWarpHooks(): Unit = {
    defaultSimulation.beforeStart()
    defaultSimulation.afterStart()
    defaultSimulation.beforeEnd()
    defaultSimulation.afterEnd()
    defaultFunSpec.beforeStart()
    defaultFunSpec.afterStart()
    defaultFunSpec.beforeEnd()
    defaultFunSpec.afterEnd()
  }
}

object WarpGatlingSpec {

  class DefaultSimulation extends WarpSimulation
  class CustomSimulation extends WarpSimulation("MyCustomSimulationTest")

  // GatlingFunSpec requires `baseURL` to be implemented
  class DefaultFunSpec extends WarpFunSpec {val baseUrl: String = ""}
  class CustomFunSpec extends WarpFunSpec("MyCustomFunSpec") {val baseUrl: String = ""}

  // Child Spec that overrides CustomFunSpec
  class ChildCustomFunSpec(override val testId: TestId = "DAMNITJIM") extends CustomFunSpec
  // Child Spec that overrides ChildCustomFunSpec
  class BabyCustomFunSpec extends ChildCustomFunSpec("BABYCOMEBACK")

  val defaultSimulation: DefaultSimulation = new DefaultSimulation
  val defaultFunSpec: DefaultFunSpec = new DefaultFunSpec
  val customFunSpec: CustomFunSpec = new CustomFunSpec
  val customSimulation: CustomSimulation = new CustomSimulation
  val childSpec: ChildCustomFunSpec = new ChildCustomFunSpec
  val babySpec: BabyCustomFunSpec = new BabyCustomFunSpec
  val packageName: String = this.getClass.getPackage.getName
}
