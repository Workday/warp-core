---
title: "Getting Started (Scala DSL)"
date: 2018-04-02T12:49:11-07:00
draft: true
weight: 30
---

The recommended way to interact with WARP is through the Scala DSL. 
This API is more mature than the Telemetron Java API and provides a richer feature set, including the ability
to register custom `MeasurementCollector` and `Arbiter` instances, and add 
new tags in the form of String metadata that will be persisted.

The DSL is implemented by using an immutable case class, `ExecutionConfig` to hold
all configuration parameters, such as number of invocations, warmups, threadpool size, etc.
We give sane defaults to all parameters.
Calls to the various DSL methods invoke the compiler-generated `copy` method to build up
new instances of `ExecutionConfig`:
{{< highlight scala "linenos=" >}}
import com.workday.warp.dsl._
val config: ExecutionConfig = using invocations 32 threads 4 distribution GaussianDistribution(50, 10)
{{< /highlight >}}

Finally, a call-by name function is passed to `ExecutionConfig.measuring`:
{{< highlight scala "linenos=" >}}
@Test
def dslExample(): Unit = {
  using invocations 32 threads 4 measuring {
    val i: Int = 1
    Logger.info(s"result is ${i + i}")
  } should not exceed (2 seconds)
}
{{< /highlight >}}

Custom `Arbiter` and `MeasurementCollector` instances can be registered by calling the `arbiters` and `collectors` methods:
{{< highlight scala "linenos=" >}}
import com.workday.warp.dsl.Implicits._

@Test
def dslCollectors(): Unit = {
  // disables the existing default collectors, and registers a new collector
  using only these collectors {
    new SomeMeasurementCollector
  // registers two new arbiters
  } arbiters {
    List(new SomeArbiter, new SomeOtherArbiter)
  } measuring {
    someExperiment()
  } should not exceed (5 seconds)
}
{{< /highlight >}}

The `arbiters` and `collectors` methods both accept a call-by-name function that returns an `Iterable`. We use an implicit
to lift a single instance into an `Iterable` type.

The threshold defined by the `should not exceed` syntax is implemented as a [scalatest](http://www.scalatest.org/user_guide/using_matchers) `Matcher[Duration]`.

## DSL Operations
The DSL provides a flexible way to describe experimental setups for conducting repeated trials, and supports the following operations:

* `testId(id: String)` sets a testId, the name under which results will be recorded in our database. (default `"com.workday.warp.Undefined.undefined"`. Typically we use the fully qualified method name of the test being measured.)

* `invocations(i: Int)` sets the number of measured trial invocations (default 1).
* `warmups(w: Int)` sets the number of unmeasured warmups (default 0).
* `threads(p: Int)` sets the thread pool size (default 1).

* `distribution(d: DistributionLike)` sets a `Distribution` to govern expected delay between scheduling invocations (default 0 delay).
* `mode(m: ModeWord)` sets a "mode" for test measurement. This is an advanced feature that only applies to experiments with a threadpool of at least 2 threads. The `single` mode will treat the entire schedule of invocations as a single logical test. A single controller will be created to measure the entire schedule. The `multi` mode (which is the default) measures each invocation on an individual basis.
* `only defaults` is a no-op included for more English-like readability.
* `no arbiters` disables all existing default `Arbiter`.
* `arbiters(a: => Iterable[ArbiterLike])` registers a collection of new `Arbiter` to act on the results of the measured test.
* `only these arbiters(a: => Iterable[ArbiterLike])` composes the two above operations; disabling all existing arbiters and 
  registering some new arbiters.
* `no collectors` disables all existing default measurement collectors.
* `collectors(c: => Iterable[AbstractMeasurementCollector])` registers a collection of new measurement collectors to measure the given test.
* `only these collectors(c: => Iterable[AbstractMeasurementCollector])` composes the two above operations; disabling all existing
  collectors before registering some new collectors.
* `tags(t: => Iterable[Tag])` registers a sequence of Tags that will be applied to the trials.
* `measure(f: => T)` (and its synonym, `measuring`) perform the measurement process wrapped around `f`. 
  We include the `measuring` synonym solely for more English-like readability when combined with
  a static threshold using our scalatest `Matcher[Duration]`:
{{< highlight scala "linenos=" >}}
using only defaults measure someExperiment
using only defaults measuring someExperiment should not exceed (5 seconds)
{{< /highlight >}}
  This is naturally the most complex operation, as it involves creating a threadpool, layering collectors
  in the correct order, persisting results, etc.
  

## DSL Return Type

After we have constructed an `ExecutionConfig` that fits the needs of our experiment, we call the `measuring`
(or `measure`, a synonym) method
with the call-by-name function `f` that we want to measure. The return type of `measuring` is `List[TrialResult[T]]`, where `T`
is the return type of `f`. This gives us access to the returned values of each invocation of `f` if they are needed for further
analysis.

Additionally, `TrialResult` holds a `TestExecutionRow` corresponding to the newly written row in our database, and 
the measured response time of the trial.

For example, suppose we are interested in measuring the performance of `List.fill[Int]` construction:

{{< highlight scala "linenos=" >}}
@Test
def listFill(): Unit = {
  // measure creating a list of 1000 0's
  val results: Seq[TrialResult[List[Int]]] = using invocations 8 measure { List.fill(1000)(0) }

  // make some assertions about the created lists
  results should have length 8
  results.head.maybeResult should not be empty

  for {
    result <- results
    list <- result.maybeResult
    element <- list
  } element should be (0)
}
{{< /highlight >}}

After measurement has been completed, we are able to access the return values of the function being measured.

The DSL provides a flexible way to customize the execution schedule of your experiment, including adding new
measurement collectors and arbiters for defining failure criteria.

