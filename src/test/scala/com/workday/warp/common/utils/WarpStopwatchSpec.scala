package com.workday.warp.common.utils

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.Test
import org.junit.experimental.categories.Category

/**
  * Created by tomas.mccandless on 6/11/18.
  */
class WarpStopwatchSpec extends WarpJUnitSpec {


  @Test
  @Category(Array(classOf[UnitTest]))
  def testAlreadyStopped(): Unit = {
    val stopwatch: WarpStopwatch = WarpStopwatch.start("abcd")
    stopwatch.stop()
    intercept[IllegalStateException] {
      stopwatch.stop()
    }
  }


  @Test
  @Category(Array(classOf[UnitTest]))
  def testNormalTiming(): Unit = {
    val stopwatch = WarpStopwatch.start("")
    Thread.sleep(5)
    stopwatch.stop()
    val elapsed: Long = stopwatch.elapsedMilliseconds()
    elapsed should be < 10L
  }
}
