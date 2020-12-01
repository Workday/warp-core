package com.workday.warp.adapters.gatling

import com.workday.warp.TestId
import com.workday.warp.controllers.AbstractMeasurementCollectionController
import com.workday.warp.inject.WarpGuicer
import io.gatling.http.funspec.GatlingHttpFunSpec

/**
  * Created by ruiqi.wang
  * All functional gatling tests measured with WARP should subclass this.
  * @param testId unique name of the gatling simulation to be measured. Defaults to the name of the class created.
  */
abstract class WarpFunSpec(val testId: TestId) extends GatlingHttpFunSpec with HasDefaultTestName with HasWarpHooks {

  def this() = this(TestId.undefined)

  val controller: AbstractMeasurementCollectionController = WarpGuicer.getController(this.canonicalName, tags = List.empty)

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
