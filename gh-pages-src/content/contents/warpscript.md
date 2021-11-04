---
title: "Scala (WarpScript)"
date: 2018-04-02T12:49:11-07:00
draft: true
weight: 35
---


## WarpScript
WarpScript is an experimental measurement API based on the Free Monad, allowing users to represent a measurement plan
as a monadic pipeline of `exec` and `measure` operations. `exec` is a simple wrapper around a call-by-name parameter,
useful for side effects or setup test data. `measure` is for measuring a call-by-name parameter.





{{< highlight scala "linenos=, style=perldoc" >}}
import com.workday.warp.junit.WarpJUnitSpec
import com.workday.warp.monadic.WarpAlgebra._
import com.workday.warp.logger.WarpLogging
import org.junit.jupiter.api.Test

class WarpScriptSpec extends WarpJUnitSpec with WarpLogging {

  @Test
  def measureSpec(): Unit = {
      val demo: WarpScript[Int] = for {
        // exec() is useful for setup test data
        a <- exec(1 + 1)
        // measure() accepts a name and an expression, and measures that expression
        b <- measure("com.workday.warp.MeasureSpec.a", { logger.info("computing a"); a + 1 })
        c <- measure("com.workday.warp.MeasureSpec.b", a + b)
      } yield c

      // run the script
      interpretImpure(demo) should be (5)
  }
}


{{< /highlight >}}

## Macros

In our own testing, we found it cumbersome to repeat variables and TestIds within a WarpScript, so we provide a macro that
will rewrite a WarpScript and automatically derive a TestId for each measured section within the script. For example,
a script such as this:
{{< highlight scala "linenos=, style=perldoc" >}}
for {
  a <- exec(1 + 1)
  b <- measure(a + 1)
  c <- measure(b + 1)
  d <- measure(c + 1)
} yield d
{{< /highlight >}}

will be macro-expanded into this (assuming it is defined within a class called `com.workday.warp.MacroExampleSpec`):

{{< highlight scala "linenos=, style=perldoc" >}}
for {
  a <- exec(1 + 1)
  b <- measure("com.workday.warp.MacroExampleSpec.b", a + 1)
  c <- measure("com.workday.warp.MacroExampleSpec.c", b + 1)
  d <- measure("com.workday.warp.MacroExampleSpec.d", c + 1)
} yield d
{{< /highlight >}}

Note the match between the bound variable and the TestId on each line. We found it difficult to implement a macro that appropriately
targets minor AST differences across different scala versions, so we publish macros as a separate artifact `com.workday.warp:warp-core-macros_2.13` that only targets
scala 2.13.

Here is a more complete example:

{{< highlight scala "linenos=, style=perldoc" >}}
import com.workday.warp.monadic.WarpAlgebra._
import com.workday.warp.monadic.Macros.deriveTestIds
import org.junit.jupiter.api.Test

class MacroExampleSpec {

  @Test
  def macroExample(): Unit = {
    val script: WarpScript[Option[String]] = deriveTestIds {
      for {
        a <- exec(1 + 1)
        b <- measure(a.toString + "abcd")
        c <- measure("defg" :: List(b))
        d <- measure(c.headOption)
      } yield d
    }

    interpretImpure(script)
  }
}
{{< /highlight >}}
