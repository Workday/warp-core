package com.workday.warp.collectors

import com.workday.warp.HasRandomTestId
import com.workday.warp.controllers.{AbstractMeasurementCollectionController, DefaultMeasurementCollectionController}
import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import com.workday.warp.persistence.Tables.RowTypeClasses.TestExecutionRowTypeClassObject
import org.junit.jupiter.api.TestInfo

/**
  * Created by tomas.mccandless on 6/11/18.
  */
class ResponseTimeCollectorSpec extends WarpJUnitSpec with HasRandomTestId {

  /**
    * Checks persisting response times.
    */
  @UnitTest
  def responseTime(info: TestInfo): Unit = {
    val controller: AbstractMeasurementCollectionController = new DefaultMeasurementCollectionController(info)
    controller.registerCollector(new ResponseTimeCollector)

    controller.beginMeasurementCollection()
    controller.endMeasurementCollection()
  }


  @UnitTest
  def stackTrace(): Unit = {
    val collector: ResponseTimeCollector = new ResponseTimeCollector

    collector.tryStartMeasurement(shouldLogStacktrace = true)
    Thread.sleep(50)
    collector.tryStopMeasurement(None, shouldLogStacktrace = true)
  }
}
