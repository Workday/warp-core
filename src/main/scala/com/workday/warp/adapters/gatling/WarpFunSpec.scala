package com.workday.warp.adapters.gatling

import com.workday.warp.adapters.gatling.traits.{HasDefaultTestName, HasWarpHooks}
import com.workday.warp.collectors.{AbstractMeasurementCollectionController, DefaultMeasurementCollectionController}
import com.workday.warp.common.CoreConstants.{UNDEFINED_TEST_ID => DEFAULT_TEST_ID}
import io.gatling.http.funspec.GatlingHttpFunSpec

/**
  * Created by ruiqi.wang
  * All functional gatling tests measured with WARP should subclass this.
  * @param testId unique name of the gatling simulation to be measured. Defaults to the name of the class created.
  */
abstract class WarpFunSpec(val testId: String) extends GatlingHttpFunSpec with HasDefaultTestName with HasWarpHooks {

  def this() = this(DEFAULT_TEST_ID)

  val controller: AbstractMeasurementCollectionController = new DefaultMeasurementCollectionController(this.canonicalName)

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
