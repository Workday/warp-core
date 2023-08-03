package com.workday.warp.monadic

import com.workday.warp.TestId
import com.workday.warp.junit.{WarpJUnitSpec, WarpTest}
import com.workday.warp.monadic.WarpAlgebra._
import com.workday.warp.logger.WarpLogging
import org.junit.jupiter.api.TestInfo


/**
 * Goals for WarpScript:
 *   - represent computation as a pure immutable value
 *   - separate definition and execution of the script
 *   - pluggable script interpreters
 *
 * Created by tomas.mccandless on 7/22/21.
 */
class WarpScriptSpec extends WarpJUnitSpec with WarpLogging {

  @WarpTest
  def measureSpec(info: TestInfo): Unit = {

    val demo: WarpScript[Int] = for {
      // exec is useful for setup test data
      a <- exec(1 + 1)
      b <- exec("setup step creates b", a + 1)

      // measure takes a name and an expression, and measures that expression
      _ <- measure("com.workday.warp.MeasureSpec.c", { logger.info("computing b"); a + 1 })
      _ <- measure(TestId.fromTestInfo(info), a + b)
      d <- measure("com.workday.warp.MeasureSpec.d", a + b)
    } yield d

    // run the script
    interpretImpure(demo) should be (5)
  }
}
