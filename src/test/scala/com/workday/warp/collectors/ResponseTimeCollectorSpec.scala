package com.workday.warp.collectors

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.persistence.Tables.RowTypeClasses.TestExecutionRowTypeClassObject
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Created by tomas.mccandless on 6/11/18.
  */
class ResponseTimeCollectorSpec extends WarpJUnitSpec {

  /**
    * Checks persisting response times.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def responseTime(): Unit = {
    val controller: AbstractMeasurementCollectionController = new DefaultMeasurementCollectionController()
    controller.registerCollector(new ResponseTimeCollector(this.getTestId))

    controller.beginMeasurementCollection()
    controller.endMeasurementCollection()
  }


  @Test
  @Category(Array(classOf[UnitTest]))
  def stackTrace(): Unit = {
    val collector: ResponseTimeCollector = new ResponseTimeCollector(this.getTestId)

    collector.tryStartMeasurement(true)
    Thread.sleep(50)
    collector.tryStopMeasurement(None, true)
  }
}
