---
title: "Getting Started"
date: 2018-04-02T13:32:53-07:00
draft: true
weight: 20
---

Warp-core allows users to instrument and persist measurements collected for their tests. The primary key warp-core uses
to identify individual tests is a fully qualified test method signature. We refer to this as a "TestId".
To get started, add warp-core to your dependencies and ensure your build has a JUnit engine on the test runtime classpath:

build.gradle:
{{< highlight groovy "linenos=,style=perldoc">}}
apply plugin: 'scala'
sourceCompatibility = 1.8

repositories {
    mavenCentral()
}

dependencies {
    testImplementation 'com.workday.warp:warp-core_2.12:5.0.1'
	testRuntimeOnly "org.junit.jupiter:junit-jupiter-engine:5.7.0"
	testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.7.0") {
		because 'allows JUnit 3 and JUnit 4 tests to run'
	}
}

test {
	testLogging {
		events 'started', 'passed'
		showStandardStreams = true
	}

	useJUnitPlatform()
}
{{< /highlight >}}

Warp-core is implemented using the JUnit5 extension model. Existing JUnit tests can be annotated to repeatedly execute or record telemetry.
By default, warp-core will record test data in an-memory H2 database. For more information on configuring database credentials and other properties, see the section on runtime configuration [here]({{< relref "runtime_configuration.md" >}} "runtime configuration") 
The most basic example is a plain JUnit test annotated with "@WarpTest":

Java:
{{< highlight java "linenos=, style=perldoc">}}
import com.workday.warp.junit.WarpTest;
import org.junit.jupiter.api.Assertions;

public class ExampleTest {

    /** A test that will be invoked a total of 6 times, 2 unmeasured warmups and 4 measured trials. */
    @WarpTest(warmups = 1, trials = 2)
    public void measured() {
        Assertions.assertEquals(2, 1 + 1);
    }
}
{{< /highlight >}}

Scala:
{{< highlight scala "linenos=, style=perldoc" >}}
import com.workday.warp.junit.WarpTest
import org.junit.jupiter.api.Assertions

class ExampleSpec {

  /** A test that will be invoked a total of 6 times, 2 unmeasured warmups and 4 measured trials. */
  @WarpTest(warmups = 1, trials = 2)
  def measured(): Unit = {
    Assertions.assertEquals(2, 1 + 1)
  }
}
{{< /highlight >}}

"@WarpTest" is a meta-annotation that combines JUnit5 "@TestTemplate" annotation with our "WarpTestExtension" JUnit extension.
A JUnit test template is not directly a test case, but is rather a template designed to be invoked multiple times as dictated by
invocation context providers.
"WarpTestExtension" is an invocation context provider that also uses JUnit "@BeforeEach" and after hooks to insert calls into our persistence module via the "MeasurementExtension"

If your project has other constraints that preclude you from using "@TestTemplate" instead of "@Test", another possibility is
adding the "@Measure" annotation to your existing tests, however note that this approach does not support repeated measurements or warmups. 

Java:
{{< highlight java "linenos=, style=perldoc" >}}
import com.workday.warp.junit.Measure;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExampleTest {

    @Test
    @Measure
    public void measureExtension() {
        Assertions.assertEquals(2, 1 + 1);
    }
}
{{< /highlight >}}

Scala:
{{< highlight scala "linenos=, style=perldoc" >}}
import com.workday.warp.junit.Measure
import org.junit.jupiter.api.{Assertions, Test}

class ExampleSpec {

  @Test
  @Measure
  def measureExtension(): Unit = {
    Assertions.assertEquals(2, 1 + 1)
  }
}
{{< /highlight >}}


Occasionally users may require usage of a lower-level api and direct access to a "TestId". At the database level, a "TestId" is 
used as a unique test identifier, stored as a fully qualified test method name. For this use case we provide implicits
augmenting Junit "TestInfo". A "TestInfo" is available to all JUnit tests using a default "ParameterResolver" that is automatically configured
for all tests. Java users can call "TestId.fromTestInfo" directly, while scala users can make use of an implicit conversion:

Java:
{{< highlight java "linenos=, style=perldoc" >}}
import com.workday.warp.TestId;
import com.workday.warp.junit.WarpTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInfo;

public class ExampleTest {

    @WarpTest
    public void testId(final TestInfo info) {
        final String id = TestId.fromTestInfo(info).id();
        Assertions.assertTrue("com.workday.warp.examples.ExampleTest.testId".equals(id));
    }
}
{{< /highlight >}}

Scala:
{{< highlight scala "linenos=, style=perldoc" >}}
import com.workday.warp.TestIdImplicits._
import com.workday.warp.junit.WarpTest
import org.junit.jupiter.api.{Assertions, TestInfo}

class ExampleSpec {

  @WarpTest
  def testId(info: TestInfo): Unit = {
    // TestIdImplicits implicit conversion
    val testId: String = info.id
    Assertions.assertEquals("com.workday.warp.examples.ExampleSpec.testId", testId)
  }
}
{{< /highlight >}}

Alternatively, we also provide a "ParameterResolver" that allows resolution of "WarpInfo". "WarpInfo" is similar to Junit "TestInfo", but
also allows users to access metadata about current test iteration sequences. Note, however, that this parameter resolver is tightly coupled
to warp-core invocation context extensions, and will only work for tests annotated with "@WarpTest".

Java:
{{< highlight java "linenos=, style=perldoc" >}}
package com.workday.warp.examples;

import com.workday.warp.junit.WarpTest;
import org.junit.jupiter.api.Assertions;

public class ExampleTest {

    /** Annotated WarpTests can also use the same parameter provider mechanism to pass WarpInfo. */
    @WarpTest
    public void measuredWithInfo(final WarpInfo info) {
        Assertions.assertEquals("com.workday.warp.examples.ExampleTest.measuredWithInfo", info.testId());
    }
}
{{< /highlight >}}


Scala:
{{< highlight scala "linenos=, style=perldoc" >}}
package com.workday.warp.examples 

import com.workday.warp.junit.WarpTest
import org.junit.jupiter.api.Assertions

class ExampleSpec {

  /** Annotated WarpTests can also use the same parameter provider mechanism to pass WarpInfo. */
  @WarpTest
  def measuredWithInfo(info: WarpInfo): Unit = {
    Assertions.assertTrue("com.workday.warp.examples.ExampleSpec.measuredWithInfo", info.testId)
  }
}
{{< /highlight >}}