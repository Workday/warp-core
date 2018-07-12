---
title: "Measurement Collection Controller"
date: 2018-04-06T12:16:51-07:00
draft: true
weight: 51
---

The `MeasurementCollectionController` (often called `MCC`) class is the central "overlord" responsible for tracking all
registered collectors and arbiters, wrapping collectors around measured tests in the correct order,
and invoking the measured code block.

Note that all collectors must be stopped in the reverse order from which they were started to maximize
measurement accuracy and avoid capturing the overheard from other collectors.

The multiple circles of hell serve as a useful mental model; each circle represents a logical group of collectors
that is started/stopped in parallel.
{{< figure src="/images/circles_of_hell.png" >}}

`MCC` is the lowest-level api we provide, and it is possible to use this class directly:

{{< highlight scala "linenos=" >}}
val mcc = new DefaultMeasurementCollectionController(testId = "com.workday.warp.some.experiment")
mcc.registerCollector(new SomeMeasurementCollector)
// start all measurement collectors prior to running the experiment
mcc.beginMeasurementCollection()

// run your experiment code
someExperiment()

// stop all measurement collectors, persist results, other cleanup
mcc.endMeasurementCollection()
{{< /highlight >}}

The DSL encapsulates the above sequence of operations in a more convenient API and provides some higher-level 
configuration options.

