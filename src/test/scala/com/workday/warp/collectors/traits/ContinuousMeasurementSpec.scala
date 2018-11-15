package com.workday.warp.collectors.traits

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.persistence.Tables.RowTypeClasses.TestExecutionRowTypeClassObject
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Created by tomas.mccandless on 11/14/18.
  */
class ContinuousMeasurementSpec extends WarpJUnitSpec {


  class DummyCollector extends ContinuousMeasurement {
    override val measurementIntervalMs: Int = 600

    var count: Int = 0 // scalastyle:ignore

    /**
      * Collects a single sample of measurement. Invoked periodically throughout the duration of a warp test.
      * Should be overridden to provide the actual implementation of measurement sampling.
      */
    override def collectMeasurement(): Unit = this.count += 1
  }


  @Test
  @Category(Array(classOf[UnitTest]))
  def continuous(): Unit = {
    val collector: DummyCollector = new DummyCollector
    collector.startMeasurement()
    Thread.sleep(800)
    collector.stopMeasurement(None)

    collector.count should be (2)
  }
}
