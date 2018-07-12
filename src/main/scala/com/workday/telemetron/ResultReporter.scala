package com.workday.telemetron

import com.workday.telemetron.utils.TimeUtils
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

import scala.collection.mutable


/**
  * This class stores and reports statistics on [[TestResult]] objects.
  *
  * Created by leslie.lam on 12/14/17
  * Based on java class created by michael.ottati on 9/3/15.
  */
class ResultReporter {

  /**
    * Mutable list containing errors
    */
  private val errors: mutable.ListBuffer[Throwable] = new mutable.ListBuffer()
  private val statistics = new DescriptiveStatistics

  /**
    * Records a [[TestResult]] for future statistical reporting.
    *
    * @param result The test result to be recorded.
    */
  def recordResult(result: TestResult): Unit = {
    var recordResult = true

    result.getErrors.foreach(error => {
      /* We only record results with no exceptions, or explicit RequirementViolationExceptions */
      if (!error.isInstanceOf[RequirementViolationException]) {
        recordResult = false
      }
      this.errors += error
    })

    /* All successful results are recorded. Requirements failures are also recorded for statistical purposes. */
    if (recordResult) {
      this.statistics.addValue(TimeUtils.durationToDoubleSeconds(result.responseTime))
    }
  }

  override def toString: String = {
    val sb = new StringBuilder
    val numSamples = this.statistics.getN

    sb.append(s"samples:    $numSamples\n")

    // quiesce the reported statistics which are meaningless for a single sample
    if (numSamples == 1) {
      sb.append(f"response time:        ${this.statistics.getMin}%.3f\n")
    }
    else if (numSamples > 0) {
      sb.append(f"min:        ${this.statistics.getMin}%.3f\n")
      sb.append(f"max:        ${this.statistics.getMax}%.3f\n")
      sb.append(f"median:     ${this.statistics.getPercentile(50)}%.3f\n")
      sb.append(f"avg:        ${this.statistics.getMean}%.3f\n")
      sb.append(f"geomean:    ${this.statistics.getGeometricMean}%.3f\n")
      sb.append(f"std dev:    ${this.statistics.getStandardDeviation}%.3f\n")
      sb.append(f"skewness:   ${this.statistics.getSkewness}%.3f\n")
      sb.append(f"kurtosis:   ${this.statistics.getKurtosis}%.3f\n")
    }

    sb.toString
  }

  /**
    * @return Returns a list of all [[Throwable]] errors collected during this test execution.
    */
  def getErrors: List[Throwable] = this.errors.toList
}
