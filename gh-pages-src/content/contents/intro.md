---
title: "Introduction"
date: 2018-04-02T13:38:13-07:00
draft: true
weight: 10
---

We've all been there before: struggling to reason about why a function is not performing the way we expect,
and whether a candidate replacement that may appear faster actually bring a statistically significant
improvement to the table. 

We on the performance-test-frameworks team have noticed that engineers often reason about the performance
of their code in a very informal way. 

Academic papers published in other scientific fields, such as biology, place an emphasis on 
obtaining statistically significant results and sound experimental design. Unfortunately, the field of computer
science tends to not place such importance on statistical rigor.
This can make it difficult to draw meaningful conclusions, or even worse, can lead us to arrive at misleading or
downright incorrect conclusions.

To help engineers reason about performance in a more scientific way, 
We're proud to introduce WARP (Workday Automated Regression Platform): a flexible, lightweight, (mostly) functional Scala framework for measuring and comparing 
the performance of arbitrary code blocks.

The goal of this library is to make it easier to apply the scientific method to the performance testing process.
Our intended user audience consists of perforrmance optimization engineers, test engineers, and ML researchers.
We at Workday believe engineers should reason scientifically about code performance.

This library is inspired in part by [this](https://dl.acm.org/citation.cfm?id=1297033) research on
statistically rigorous performance evaluation techniques for Java.

WARP features a DSL for describing experimental designs (conducting repeated trials for different experimental
groups).
Additionally, we support collecting custom measurements, expressing custom performance requirements,
and conducting statistical significance tests on history of two tests.

Collected measurements are written to a relational database for subsequent view and analysis. 
WARP has been tested on both H2 and MySQL.

WARP framework includes features that make it simple to:

  - collect arbitrary custom measurements for a test invocation.
  - repeatedly invoke a test, collecting measurements for each invocation.
  - exclude initial invocations from being measured (for example, to allow the JVM to warmup, or perform JIT).
  - distribute all test invocations to a threadpool of configurable size.
  - inject statistical delay between invocations (load testing).
  - group tests for the purpose of determining significant statistical differences.
  - tag test invocations with custom identifiers (JIRA ticket, A/B testing).
  - define custom failure conditions (Arbiters)

The general WARP framework has two supported frontends:

  - JUnit annotations and extensions (for Java or Scala users)

{{< highlight java "linenos=, style=perldoc" >}}
import com.workday.warp.junit.WarpTest;
import org.junit.jupiter.api.Assertions;

public class ExampleTest {

    @WarpTest(warmups = 2, trials = 6)
    public void example() {
        Logger.info("hello, world!");
        Assertions.assertEquals(2, 1 + 1);
    }
}
{{< /highlight >}}

  - DSL (for Scala users only)

{{< highlight scala "linenos=, style=perldoc" >}}
import org.junit.jupiter.api.{Test, TestInfo}
import com.workday.warp.dsl._
import com.workday.warp.junit.WarpJUnitSpec

class ExampleSpec extends WarpJUnitSpec {

  @Test
  def example(testInfo: TestInfo): Unit = {
    using testId testInfo invocations 8 threads 4 measuring {
      someExperiment() 
    } should not exceed (1 second)
  }
}
{{< /highlight >}}

The above DSL example creates a pool of 4 threads, invokes the given test 8 times, collects a default set of measurements
from each invocation, and writes the results to a relational database.

WARP includes many features that make it easy to reason scientifically about your performance tests,
such as statistical significance tests and anomaly detection to help find performance regressions.

Internally, we use WARP in several different ways:
 
  - detecting performance regressions in test suites in our CI environment
  - ad-hoc performance testing in response to reported performance issues
  - statistically evaluating multiple method implementations (A/B testing)

We are excited to provide a platform to help engineers reason about their code performance in a more principled manner.
