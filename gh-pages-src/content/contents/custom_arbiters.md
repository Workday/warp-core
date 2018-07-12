---
title: "Custom Arbiters"
date: 2018-04-03T10:57:21-07:00
draft: true
weight: 50
---

An `Arbiter` defines a `vote` method that allows us to implement custom failure criteria for a given test execution.

For example, the following `Arbiter` implementation fails a test if it took longer than 5 seconds:
{{< highlight scala "linenos=" >}}
class ResponseTimeArbiter extends ArbiterLike with CorePersistenceAware {

  /**
    * Checks that the measured test took less than 5 seconds.
    *
    * @param ballot box used to register vote result.
    * @param testExecution [[TestExecutionRowLikeType]] we are voting on.
    * @return a wrapped error with a useful message, or None if the measured test passed its requirement.
    */
  override def vote[T: TestExecutionRowLikeType](ballot: Ballot, testExecution: T): Option[Throwable] = {
    val testId: String = this.persistenceUtils.getMethodSignature(testExecution)

    if (testExecution.responseTime > 5) {
      Option(new RequirementViolationException(s"$testId took longer than 5 seconds"))
    }
    else {
      None
    }
  }
}
{{< /highlight >}}

Some arbiters may want access to historical data in order to make an informed decision about the most recent test invocation.
To easily access historical data, mix in the trait `CanReadHistory`. For example, using this mechanism, we can
implement an arbiter that votes based on the z-score of the most recently test execution. Using such an arbiter,
for example, we can fail any test whose recorded measurement value is greater standard deviations away from the
historical mean.

## Anomaly Detection

WARP includes an arbiter that uses machine learning to cast votes.
The `RobustPcaArbiter` votes based on the results of an anomaly detection algorithm,
Robust Principal Component Analysis (RPCA).
Internally, we use this arbiter to fail any tests that have
suspicious or anomalous measurements.
See the dedicated RPCA page [here]({{< ref "anomaly_detection.md" >}}) for a more detailed description of how
this algorithm works, including our novel algorithmic extension designed to improve strictness and avoid
false negatives over time.
