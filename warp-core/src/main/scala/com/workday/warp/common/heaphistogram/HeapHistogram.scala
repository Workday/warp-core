package com.workday.warp.common.heaphistogram

import org.pmw.tinylog.Logger

/**
 * Container class for a heap histogram (a collection of heap histogram entries)
 *
 * Created by tomas.mccandless on 9/21/15.
 */
class HeapHistogram(val histogramEntries: Seq[HeapHistogramEntry]) {
  Logger.debug(s"created HeapHistogram with ${this.histogramEntries.length} entries.")
}
