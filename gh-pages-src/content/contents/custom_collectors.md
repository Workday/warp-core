---
title: "Collecting Custom Measurements"
date: 2018-04-03T10:57:13-07:00
draft: true
weight: 40
---

The `AbstractMeasurementCollector` class defines two side-effecting functions: `startMeasurement` and 
`stopMeasurement`, that are invoked, respectively, before and after a test execution.

Implementations of a measurement or measurement collection should extend this class, and probably mix in
the `CorePersistenceAware` trait. Note that the collection of a measurement may 
encompass a number of different types of operations. It may include such activities as collecting 
from JMX, scraping server logs, or as simple as starting and stopping a clock.

Out of the box, WARP includes a default minimal set of measurement collectors for measuring elapsed
wall-clock time and heap usage. Internally, our engineers have developed many additional collectors
for instrumenting their code.

For example, the following class records elapsed wall clock time as a measurement:

{{< highlight scala "linenos=" >}}
class WallClockTimeCollector(testId: String) extends AbstractMeasurementCollector(testId) 
    with CorePersistenceAware {

  private var timeBeforeMs: Long = _

  /** Called prior to a test invocation.  */
  override def startMeasurement(): Unit = {
    this.timeBeforeMs = System.currentTimeMillis()
  }

  /** Called after finishing a test invocation.  */
  override def stopMeasurement[T: TestExecutionRowLikeType](maybeTestExecution: Option[T]): Unit = {
    val durationMs: Long = System.currentTimeMillis() - this.timeBeforeMs

    maybeTestExecution foreach { testExecution: T =>
      this.persistenceUtils.recordMeasurement(testExecution.idTestExecution, "wall clock time", durationMs)
    }
  }
}
{{< /highlight >}}

In some cases it may be desirable to instead record measurements at a periodic interval
throughout the duration of a test. The trait `ContinuousMeasurement` implements this functionality
by spawning a separate thread that can run arbitrary code. To create a continuous measurement collector,
subclass `AbstractMeasurementCollector`, mix in the `ContinuousMeasurement` trait, and override the method
`collectMeasurement()`.

`AbstractMeasurementCollector` provides a flexible interface for extending the core framework with 
additional measurements.
