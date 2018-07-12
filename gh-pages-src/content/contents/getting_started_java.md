---
title: "Getting Started (Java)"
date: 2018-04-02T13:32:53-07:00
draft: true
weight: 20
---

The recommended way to interact with WARP is through the Scala DSL. 
The Scala DSL is more rich-featured,
however, we also support calling a subset of our functionality through a Java API called Telemetron.
Telemetron is based on annotations and JUnit [@Rule](https://junit.org/junit4/javadoc/4.12/org/junit/Rule.html). 

Each facet of WARP functionality is implemented as a separate rule,
and we use a [RuleChain](https://junit.org/junit4/javadoc/4.12/org/junit/rules/RuleChain.html)
to ensure the rules are applied in the correct order.

To get started, your JUnit class can either:

  * declare an `@Rule` with a `TelemetronRule` instance.
  * extend the abstract class `TelemetronJUnitSpec`.

## Telemetron Java Annotations

  * `@Schedule` is used to configure the measurement framework with information about how the test should be invoked, and
  supports several optional configuration parameters:
    - `invocations` indicates how many times the test should be invoked.
    - `warmupInvocations` indicates how many times the test should be invoked without being measured or having requirements
      checked.
    - `threads` indicates the number of concurrent threads the test will be executed on. Note that we do not create
      multiple instances of the test class. References to shared mutable state must be synchronized.

  * `@BeforeOnce` and `@AfterOnce` provide a hook for one-time setup/teardown JUnit methods.
  Telemetron honors the `@Before`/`@After` JUnit contract: an `@Before` method should be invoked before each test invocation.
  However, some use cases (especially using `@Schedule` with multiple invocations and threads) require the ability to perform
  a single setup/teardown cycle for the entire test schedule.
  Methods annotated with `@BeforeOnce` or `@AfterOnce` will be invoked a single time at the start/end 
  of the test schedule for each test method.

  * `@Distribution` is used to conduct forms of load testing.
  Test methods with multiple invocations can be further modified with this annotation to artificially inject latency
  between each test invocation. 

  * `@Measure` is used to specify that response time measurements for the test method should be persisted.

  * `@Required` is used to specify a response time threshold that your test must meet.

Putting it all together, we can write a test that is invoked 32 times on a small thread pool. Each individual invocation
must complete within 1 second. A Gaussian distribution with mean and standard deviation of 50 and 10 milliseconds is used
to inject artificial latency between each invocation.
{{< highlight java "linenos=" >}}
@Test
@Schedule(
	invocations = 32,
	threads = 4,
	distribution = @Distribution(
		clazz = GaussianDistribution.class,
		parameters = {50, 10}
	)
)
@Measure
@Required(maxResponseTime = 1)
public void exampleTest() {
	Logger.info("executing test");
}
{{< /highlight >}}

When the run has completed, we can see some rudimentary statistics printed to stdout:
```
samples:    32
min:        0.000
max:        0.037
median:     0.001
avg:        0.005
geomean:    0.000
std dev:    0.012
skewness:   2.361
kurtosis:   3.855
```
