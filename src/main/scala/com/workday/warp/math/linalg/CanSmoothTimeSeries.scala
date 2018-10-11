package com.workday.warp.math.linalg

/**
  * Created by tomas.mccandless on 10/10/18.
  */
trait CanSmoothTimeSeries {

  /**
    * Smooths the last k entries in `series`, for a smoother smart threshold.
    *
    * This helps avoid our smart thresholds closely following jitter in the underlying time series.
    *
    * For example, the sequence [1, 2, 2, 4, 6] with k=3 will yield the new sequence [1, 2, 4, 4, 4].
    *
    * @param series time series to be smoothed.
    * @param k number of entries to be replaced with their average.
    * @return a new time series with the last `k` entries replaced by their average.
    */
  def smooth(series: Iterable[Double], k: Int): Iterable[Double] = {
    val avg: Double = (series takeRight k).sum / k
    (series dropRight k) ++ Iterable.fill(k)(avg)
  }
}