package com.workday.warp.collectors

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
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
}
