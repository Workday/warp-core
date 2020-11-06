package com.workday.warp.collectors

import com.workday.warp.HasRandomTestId
import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import com.workday.warp.persistence.Tables.RowTypeClasses.TestExecutionRowTypeClassObject

/**
  * Created by tomas.mccandless on 6/11/18.
  */
class ResponseTimeCollectorSpec extends WarpJUnitSpec with HasRandomTestId {

  /**
    * Checks persisting response times.
    */
  @UnitTest
  def responseTime(): Unit = {
    val controller: AbstractMeasurementCollectionController = new DefaultMeasurementCollectionController()
    controller.registerCollector(new ResponseTimeCollector(this.randomTestId()))

    controller.beginMeasurementCollection()
    controller.endMeasurementCollection()
  }


  @UnitTest
  def stackTrace(): Unit = {
    val collector: ResponseTimeCollector = new ResponseTimeCollector(this.randomTestId())

    collector.tryStartMeasurement(true)
    Thread.sleep(50)
    collector.tryStopMeasurement(None, true)
  }
}
