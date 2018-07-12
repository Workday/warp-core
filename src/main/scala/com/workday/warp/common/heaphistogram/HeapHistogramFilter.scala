package com.workday.warp.common.heaphistogram

import com.workday.warp.common.CoreWarpProperty._
import org.pmw.tinylog.Logger

import scala.collection.mutable

/**
  * A small container class to hold separate histograms (the overall histogram, persistedHistogram; and the smaller
  * subhistogram, plottedHistogram, that is intended to be graphed in grafana)
  *
  * Created by tomas.mccandless on 9/21/15.
  */
class HeapHistogramFilter(val persistedHistogram: HeapHistogram, val plottedHistogram: HeapHistogram) {

  def this(persistedHistogram: Seq[HeapHistogramEntry], plottedHistogram: Seq[HeapHistogramEntry]) {
    this(new HeapHistogram(persistedHistogram), new HeapHistogram(plottedHistogram))
  }
}


object HeapHistogramFilter {

  /**
   * Constructs a HeapHistogramFilter given a List of HeapHistogramEntry objects.
   *
   * The list of entries will be filtered as follows:
   *     We create two separate heap histograms, the first contains all classes that were either generated or in the
   *     top WARP_HEAPHISTO_PROCESSING_LIMIT list of classes by either instance or memory usage. During testing we
   *     set the default so this list contains roughly 1k entries. However, since the influxdb query language is
   *     still quite limited (there is no ORDER BY clause, the TOP function does not works as expected or in
   *     conjunction with a LIMIT clause) it is cumbersome to visualize the top class from this list.
   *
   *     To remedy this, we also create a separate smaller heap histogram that only contains classes which were in
   *     the top WARP_HEAPHISTO_GRAFANA_LIMIT list of classes by either instances or memory usage. This smaller
   *     histogram is then persisted in a separate series.
   *
   * @param histogramEntries
   */
  def apply(histogramEntries: Seq[HeapHistogramEntry]): HeapHistogramFilter = {
    val entriesToPersist: mutable.HashSet[HeapHistogramEntry] = new mutable.HashSet[HeapHistogramEntry]
    val entriesToPlot: mutable.HashSet[HeapHistogramEntry] = new mutable.HashSet[HeapHistogramEntry]

    // whether to force inclusion of all generated classes even if they were not among the top resource consumers.
    // the list of persisted class should also contain the generated classes, which have names like:
    // com.workday.instancedata.relationship.{SCURltn_16190, Rltn_16109, Inst_4784}
    if (WARP_HEAPHISTO_INCLUDE_GENERATED.value.toBoolean) {
      entriesToPersist ++= histogramEntries filter { entry: HeapHistogramEntry => this.isClassGenerated(entry.className) }
    }

    // limit on the top n classes to be examined. we sort the list of entries by instance allocations and byte
    // usage to obtain two separate orderings of the histogram entries. we then union the top n classes from both
    // orderings to determine the final list of classes that will be stored as part of this histogram.
    val processingLimit: Int = WARP_HEAPHISTO_PROCESSING_LIMIT.value.toInt
    Logger.debug(s"heap histogram processing limit: $processingLimit")

    // limit on the top n classes to be persisted in a separate smaller series suitable for graphing in grafana
    val plotLimit: Int = WARP_HEAPHISTO_GRAFANA_LIMIT.value.toInt
    Logger.debug(s"heap histogram grafana plot limit: $plotLimit")

    val sortedByInstances: Seq[HeapHistogramEntry] = histogramEntries sorted InstancesOrdering
    val sortedByBytes: Seq[HeapHistogramEntry] = histogramEntries sorted BytesOrdering

    entriesToPersist ++= sortedByInstances take processingLimit
    entriesToPersist ++= sortedByBytes take processingLimit
    entriesToPlot ++= sortedByInstances take plotLimit
    entriesToPlot ++= sortedByBytes take plotLimit

    new HeapHistogramFilter(entriesToPersist.toSeq, entriesToPlot.toSeq)
  }


  /**
    * @param className name of a class that appears in a heap sample
    * @return whether the specified class is generated
    */
  def isClassGenerated(className: String): Boolean = className matches "(Rltn|Inst)_"
}

