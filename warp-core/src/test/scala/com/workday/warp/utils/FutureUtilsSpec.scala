package com.workday.warp.utils

import com.workday.warp.config.CoreWarpProperty._
import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Try}

/**
 * Created by tomas.mccandless on 1/5/16.
 */
class FutureUtilsSpec extends WarpJUnitSpec {

  @UnitTest
  def testParseThreadpoolSize(): Unit = {
    val defaultThreads: Int = WARP_NUM_COLLECTOR_THREADS.value.toInt

    FutureUtils.threadpoolSize("16") shouldBe 16
    FutureUtils.threadpoolSize("32") shouldBe 32
    FutureUtils.threadpoolSize("-1") shouldBe defaultThreads
    FutureUtils.threadpoolSize("0") shouldBe defaultThreads
    FutureUtils.threadpoolSize("abc") shouldBe defaultThreads
  }



  @UnitTest
  def testExecute(): Unit = {
    // create a sequence of Future Ints
    val tasks: Seq[Future[Int]] = List( Future { 1 + 1 }, Future { 2 + 2 }, Future { 3 + 3 } )
    // execute the sequenced tasks
    val completedTasks: Seq[Try[Int]] = FutureUtils.execute(tasks)
    // verify all tasks completed successfully as expected
    completedTasks shouldBe Seq( Success(2), Success(4), Success(6) )
  }
}
