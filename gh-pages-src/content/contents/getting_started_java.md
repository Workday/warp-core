---
title: "Getting Started (Java)"
date: 2018-04-02T13:32:53-07:00
draft: true
weight: 20
---

Warp-core is implemented using the JUnit5 extension model. Existing JUnit tests can be annotated to repeatedly execute or record telemetry.
The most basic example is a test annotated with "@WarpTest":
{{< highlight java "linenos=" >}}
    /** A test that will be invoked a total of 6 times, 2 unmeasured warmups and 4 measured trials. */
    @WarpTest(warmups = 1, trials = 2)
    public void measured() {
        Logger.trace("hi there");
    }
{{< /highlight >}}

"@WarpTest" is a meta-annotation that combines JUnit5 "@TestTemplate" annotation with our "WarpTestExtension" JUnit extension.
"WarpTestExtension" uses JUnit before and after hooks to insert calls into our persistence module.

If your project has other constraints that preclude you from using "@TestTemplate" instead of "@Test", another possibility is
adding the "@Measure" annotation to your existing tests, however note that this approach does not support repeated measurements or warmups. 

{{< highlight java "linenos=" >}}
    @Test
    @Measure
    public void measureExtension() {
        Logger.trace("we are being measured but not repeated");
    }
{{< /highlight >}}




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
