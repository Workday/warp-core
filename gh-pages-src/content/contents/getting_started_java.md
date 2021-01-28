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
A JUnit test template is not directly a test case, but is rather a template designed to be invoked multiple times as dictated by
invocation context providers.
"WarpTestExtension" is an invocation context provider that also uses JUnit "@BeforeEach" and after hooks to insert calls into our persistence module via the "MeasurementExtension"

If your project has other constraints that preclude you from using "@TestTemplate" instead of "@Test", another possibility is
adding the "@Measure" annotation to your existing tests, however note that this approach does not support repeated measurements or warmups. 

{{< highlight java "linenos=" >}}
    @Test
    @Measure
    public void measureExtension() {
        Logger.trace("we are being measured but not repeated");
    }
{{< /highlight >}}

