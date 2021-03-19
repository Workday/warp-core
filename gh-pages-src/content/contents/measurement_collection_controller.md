---
title: "Measurement Collection Controller"
date: 2018-04-06T12:16:51-07:00
draft: true
weight: 51
---

The `MeasurementCollectionController` (often called `MCC`) is the central "overlord" class responsible for tracking all
registered collectors and arbiters, wrapping collectors around measured tests in the correct order,
and invoking the measured code block.

Note that all collectors must be stopped in the reverse order from which they were started to maximize
measurement accuracy and avoid mistakenly capturing any overheard from other collectors.

The multiple circles of hell serve as a useful mental model: each circle represents a collector
that is started/stopped in a specific order.
{{< figure src="/images/circles_of_hell.png" >}}

`MCC` is the lowest-level api we provide, and it is possible to use this class directly:

{{< highlight scala "linenos=,style=perldoc" >}}

import com.workday.warp.controllers.DefaultMeasurementCollectionController
import org.junit.jupiter.api.{Test, TestInfo}

class ExampleSpec {

  // this example manages the measurement lifecycle directly and thus uses @Test instead of @WarpTest
  @Test
  def mcc(testInfo: TestInfo): Unit = {
    val mcc = new DefaultMeasurementCollectionController(testId = testInfo)
    mcc.registerCollector(new SomeMeasurementCollector)
    // start all measurement collectors prior to running the experiment
    mcc.beginMeasurementCollection()

    // run your experiment code
    someExperiment()

    // stop all measurement collectors, persist results, other cleanup
    mcc.endMeasurementCollection()
    
    // alternate signature that allows for manually specifying elapsed test time and a test timing threshold
    // mcc.endMeasurementCollection(elapsedTime = 5.seconds, threshold = 6.seconds)
  }
}

{{< /highlight >}}

The DSL encapsulates the above sequence of operations in a more convenient API and provides some higher-level 
configuration options, such as distributing multiple test invocations onto a threadpool.

