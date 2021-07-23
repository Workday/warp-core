package com.workday.warp.monadic

import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import com.workday.warp.TestIdImplicits.string2TestId
import com.workday.warp.monadic.MMM._
import org.pmw.tinylog.Logger
import scalaz.Free

/**
 * Created by tomas.mccandless on 7/22/21.
 */
class MeasureSpec extends WarpJUnitSpec {

  @UnitTest
  def measureSpec(): Unit = {




    val r: Script[Int] = for {
      a <- measure("com.workday.warp.MeasureSpec.a", { Logger.info("computing a"); 1 + 1 })
      b <- measure("com.workday.warp.MeasureSpec.b", a + 1)
      c <- exec(a + b)
    } yield c


    val s: Int = interpretImpure(r)
    s should be (5)

  }
}
