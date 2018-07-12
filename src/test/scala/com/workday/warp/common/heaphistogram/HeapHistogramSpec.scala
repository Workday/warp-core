package com.workday.warp.common.heaphistogram

import java.io.InputStream

import com.workday.warp.collectors.abstracts.AbstractMeasurementCollector
import com.workday.warp.collectors.{AbstractMeasurementCollectionController, ContinuousHeapHistogramCollector}
import com.workday.warp.collectors.DefaultMeasurementCollectionController
import com.workday.warp.common.CoreConstants
import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.scalatest.Matchers._
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Tests for the Histogram-related methods
  *
  * Created by vignesh.kalidas on 4/6/18.
  */
class HeapHistogramSpec extends WarpJUnitSpec with HistogramIoLike {

  /**
    * Gets the heap histogram
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def getHeapHistogramSpec(): Unit = {
    case class Cat()
    val cats: List[Cat] = List.fill(1000)(new Cat)
    val histogram: Seq[HeapHistogramEntry] = getHeapHistogram

    histogram.find(_.className.contains("HeapHistogramSpec$Cat")).map(_.numInstances) should be (Some(1000))
  }

  /**
    * Calls the PID method of the companion object explicitly
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def getPidSpec(): Unit = {
    val pid = HistogramIoLike.pid

    pid should not be 0
  }

  /**
    * Calls the companion object's `vm` field explicitly
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def initCompanionObjectSpec(): Unit = {
    val inputStream: InputStream = HistogramIoLike.vm.heapHisto("-live")
    val heapHistogramString: String = scala.io.Source.fromInputStream(inputStream).mkString

    heapHistogramString should not be empty
  }

  /**
    * Tests the full process of registering a collector and calling begin/end for measurement collection
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def lifecycleSpec(): Unit = {
    val measCollectionController: AbstractMeasurementCollectionController = new DefaultMeasurementCollectionController()
    val contHeapHistoCollector: AbstractMeasurementCollector = new ContinuousHeapHistogramCollector(CoreConstants.UNDEFINED_TEST_ID)

    measCollectionController.registerCollector(contHeapHistoCollector)
    measCollectionController.registerCollector(new HeapHistogramCollector(this.getTestId))

    measCollectionController.beginMeasurementCollection()
    val histogram: Seq[HeapHistogramEntry] = getHeapHistogram
    measCollectionController.endMeasurementCollection()

    histogram should not be empty
  }
}
