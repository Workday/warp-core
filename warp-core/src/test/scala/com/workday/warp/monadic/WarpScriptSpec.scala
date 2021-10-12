package com.workday.warp.monadic

import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import com.workday.warp.TestIdImplicits.string2TestId
import com.workday.warp.monadic.WarpAlgebra._
import com.github.dwickern.macros.NameOf._
import com.workday.warp.logger.WarpLogging

/**
 * Goals for WarpScript:
 *   - represent computation as a pure immutable value
 *   - separate definition and execution of the script
 *   - pluggable script interpreters
 *
 * Created by tomas.mccandless on 7/22/21.
 */
class WarpScriptSpec extends WarpJUnitSpec with WarpLogging {

  @UnitTest
  def measureSpec(): Unit = {

    val demo: WarpScript[Int] = for {
      // exec is useful for setup test data
      a <- exec(1 + 1)
      // measure takes a name and an expression, and measures that expression
      b <- measure("com.workday.warp.MeasureSpec.a", { logger.info("computing a"); a + 1 })
      c <- measure("com.workday.warp.MeasureSpec.b", a + b)
    } yield c

    // run the script
    interpretImpure(demo) should be (5)
  }
}
