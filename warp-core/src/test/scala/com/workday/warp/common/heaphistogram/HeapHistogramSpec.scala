package com.workday.warp.common.heaphistogram

import java.io.InputStream

import com.workday.telemetron.spec.HasRandomTestId
import com.workday.warp.collectors.abstracts.AbstractMeasurementCollector
import com.workday.warp.collectors.{AbstractMeasurementCollectionController, ContinuousHeapHistogramCollector}
import com.workday.warp.collectors.DefaultMeasurementCollectionController
import com.workday.warp.common.CoreConstants
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.junit.UnitTest

/**
  * Tests for the Histogram-related methods
  *
  * Created by vignesh.kalidas on 4/6/18.
  */
class HeapHistogramSpec extends WarpJUnitSpec with HistogramIoLike with HasRandomTestId {

  /**
    * Gets the heap histogram
    */
  @UnitTest
  def getHeapHistogramSpec(): Unit = {
    case class Cat()
    List.fill(1000)(new Cat)
    val histogram: Seq[HeapHistogramEntry] = getHeapHistogram

    histogram.find(_.className.contains("HeapHistogramSpec$Cat")).map(_.numInstances) should be (Some(1000))
  }

  /**
    * Calls the PID method of the companion object explicitly
    */
  @UnitTest
  def getPidSpec(): Unit = {
    val pid = HistogramIoLike.pid

    pid should not be 0
  }

  /**
    * Calls the companion object's `vm` field explicitly
    */
  @UnitTest
  def initCompanionObjectSpec(): Unit = {
    val inputStream: InputStream = HistogramIoLike.vm.heapHisto("-live")
    val heapHistogramString: String = scala.io.Source.fromInputStream(inputStream).mkString

    heapHistogramString should not be empty
  }

  /**
    * Tests the full process of registering a collector and calling begin/end for measurement collection
    */
  @UnitTest
  def lifecycleSpec(): Unit = {
    val measCollectionController: AbstractMeasurementCollectionController = new DefaultMeasurementCollectionController()
    val contHeapHistoCollector: AbstractMeasurementCollector = new ContinuousHeapHistogramCollector(CoreConstants.UNDEFINED_TEST_ID)

    measCollectionController.registerCollector(contHeapHistoCollector)
    measCollectionController.registerCollector(new HeapHistogramCollector(this.randomTestId()))

    measCollectionController.beginMeasurementCollection()
    val histogram: Seq[HeapHistogramEntry] = getHeapHistogram
    measCollectionController.endMeasurementCollection()

    histogram should not be empty
  }
}
