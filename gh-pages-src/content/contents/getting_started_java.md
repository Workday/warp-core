---
title: "Getting Started"
date: 2018-04-02T13:32:53-07:00
draft: true
weight: 20
---

Warp-core allows users to instrument and persist measurements collected for their tests. The primary key warp-core uses
to identify individual tests is a fully qualified test method signature. We refer to this as a "TestId".
Warp-core is implemented using the JUnit5 extension model. Existing JUnit tests can be annotated to repeatedly execute or record telemetry.
The most basic example is a test annotated with "@WarpTest":

{{< highlight java "linenos=" >}}
    /** A test that will be invoked a total of 6 times, 2 unmeasured warmups and 4 measured trials. */
    @WarpTest(warmups = 1, trials = 2)
    public void measured() {
        Logger.trace("hi there");
    }
{{< /highlight >}}


{{< highlight scala "linenos=" >}}
    /** A test that will be invoked a total of 6 times, 2 unmeasured warmups and 4 measured trials. */
    @WarpTest(warmups = 1, trials = 2)
    def measured(): Unit = {
        Logger.trace("hi there")
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


{{< highlight scala "linenos=" >}}
    @Test
    @Measure
    def measureExtension(): Unit = {
        Logger.trace("we are being measured but not repeated")
    }
{{< /highlight >}}


Occasionally users may require usage of a lower-level api and direct access to a "TestId". For this use case we provide implicits
augmenting Junit "TestInfo", which is available to all JUnit tests using a default "ParameterResolver" that is automatically configured
for all tests. Java users can call "TestId.fromTestInfo" directly, while scala users can make use of an implicit conversion:

{{< highlight java "linenos=" >}}
import com.workday.warp.TestId;

public class ExampleTest {

    @Test
    public void testId(final TestInfo info) {
        final String id = TestId.fromTestInfo(info).id();
        Assert.assertTrue("com.workday.warp.examples.ExampleTest.testId".equals(id));
    }
}
{{< /highlight >}}

{{< highlight scala "linenos=" >}}
import com.workday.warp.TestIdImplicits._

class ExampleSpec {

  @Test
  def testId(info: TestInfo): Unit = {
    // TestIdImplicits implicit conversion
    val testId: String = info.id
    Assert.assertTrue("com.workday.warp.examples.ExampleTest.testId" == testId)
  }
}
{{< /highlight >}}

Alternatively, we also provide a "ParameterResolver" that allows resolution of "WarpInfo". "WarpInfo" is similar to Junit "TestInfo", but
also allows users to access metadata about current test iteration sequences. Note, however, that this parameter resolver is tightly coupled
to warp-core invocation context extensions, and will only work correctly for tests annotated with "@WarpTest".

{{< highlight scala "linenos=" >}}

  /** Annotated WarpTests can also use the same parameter provider mechanism to pass WarpInfo. */
  @WarpTest
  def measuredWithInfo(info: WarpInfo): Unit = {
    Assert.assertTrue(info.testId == "com.workday.warp.examples.ExampleTest.measuredWithInfo")
  }
{{< /highlight >}}