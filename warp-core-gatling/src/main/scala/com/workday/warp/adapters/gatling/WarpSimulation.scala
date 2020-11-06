package com.workday.warp.adapters.gatling

import com.workday.warp.adapters.gatling.traits.{HasDefaultTestName, HasWarpHooks}
import com.workday.warp.collectors.AbstractMeasurementCollectionController
import com.workday.warp.config.CoreConstants.{UNDEFINED_TEST_ID => DEFAULT_TEST_ID}
import com.workday.warp.inject.WarpGuicer
import io.gatling.core.Predef.Simulation

/**
  * Created by ruiqi.wang
  * All load/integration Gatling tests measured with WARP should subclass this.
  * @param testId unique name of the gatling simulation to be measured. Defaults to the name of the class created.
  */
abstract class WarpSimulation(val testId: String) extends Simulation with HasDefaultTestName with HasWarpHooks {

  def this() = this(DEFAULT_TEST_ID)

  val controller: AbstractMeasurementCollectionController = WarpGuicer.getController(this.testId, tags = List.empty)

  before {
    beforeStart()
    controller.beginMeasurementCollection()
    afterStart()
  }

  after {
    beforeEnd()
    controller.endMeasurementCollection()
    afterEnd()
  }

}
