package com.workday.warp.utils

import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}

/**
  * Created by tomas.mccandless on 6/11/18.
  */
class WarpStopwatchSpec extends WarpJUnitSpec {


  @UnitTest
  def testAlreadyStopped(): Unit = {
    val stopwatch: WarpStopwatch = WarpStopwatch.start("abcd")
    stopwatch.stop()
    intercept[IllegalStateException] {
      stopwatch.stop()
    }
  }


  @UnitTest
  def testNormalTiming(): Unit = {
    val stopwatch = WarpStopwatch.start("")
    Thread.sleep(5)
    stopwatch.stop()
    val elapsed: Long = stopwatch.elapsedMilliseconds()
    elapsed should be < 15L
  }
}
