package com.workday.warp.common.heaphistogram

import javax.management.openmbean.CompositeData

/**
 * Small container class for class usage in a heap histogram.
 *
 * Created by tomas.mccandless on 9/21/15.
 */
class HeapHistogramEntry(val className: String, val numInstances: Long, val numBytes: Long) {

  /**
   * Auxiliary constructor accepting a CompositeData object
   *
   * @param histogramEntryCD CompositeData to construct a HeapHistogramEntry from
   */
  def this(histogramEntryCD: CompositeData) = {
    this(
      histogramEntryCD.get("className").toString,
      histogramEntryCD.get("numInstances").toString.toLong,
      histogramEntryCD.get("numBytes").toString.toLong
    )
  }
}

// compare entries based on their number of instances
object InstancesOrdering extends Ordering[HeapHistogramEntry] {
  // descending order
  override def compare(histo1: HeapHistogramEntry, histo2: HeapHistogramEntry): Int = {
    histo2.numInstances compareTo histo1.numInstances
  }
}

// compare entries based on their heap usage
object BytesOrdering extends Ordering[HeapHistogramEntry] {
  // descending order
  override def compare(histo1: HeapHistogramEntry, histo2: HeapHistogramEntry): Int = {
    histo2.numBytes compareTo histo1.numBytes
  }
}
