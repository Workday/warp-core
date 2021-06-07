package com.workday.warp.heaphistogram

import com.workday.warp.logger.WarpLogging

/**
 * Container class for a heap histogram (a collection of heap histogram entries)
 *
 * Created by tomas.mccandless on 9/21/15.
 */
class HeapHistogram(val histogramEntries: Seq[HeapHistogramEntry]) extends WarpLogging {
  logger.debug(s"created HeapHistogram with ${this.histogramEntries.length} entries.")
}
