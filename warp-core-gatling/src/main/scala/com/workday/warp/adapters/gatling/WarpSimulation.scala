package com.workday.warp.adapters.gatling

import com.workday.warp.{TestId, TrialResult}
import com.workday.warp.controllers.AbstractMeasurementCollectionController
import com.workday.warp.inject.WarpGuicer
import io.gatling.core.Predef.Simulation

import scala.util.Success

/**
  * Created by ruiqi.wang
  * All load/integration Gatling tests measured with WARP should subclass this.
  * @param testId unique name of the gatling simulation to be measured. Defaults to the name of the class created.
  */
abstract class WarpSimulation(val testId: TestId) extends Simulation with HasDefaultTestName with HasWarpHooks {

  def this() = this(TestId.undefined)

  // override this field to set documentation at the test definition level
  val maybeDocumentation: Option[String] = None

  val controller: AbstractMeasurementCollectionController = WarpGuicer.getController(this.testId, tags = List.empty)

  before {
    beforeStart()
    controller.beginMeasurementCollection()
    afterStart()
  }

  after {
    beforeEnd()
    val trial: TrialResult[Unit] = TrialResult(maybeDocumentation = this.maybeDocumentation)
    controller.endMeasurementCollection(Success(trial))
    afterEnd()
  }

}
