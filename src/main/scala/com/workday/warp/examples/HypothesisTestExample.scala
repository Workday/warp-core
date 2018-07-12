package com.workday.warp.examples

import java.util.Date

import com.workday.warp.TrialResult
import com.workday.warp.dsl.{defaults, using}
import com.workday.warp.math.stats.{AllRegressionStatTestResults, TwoSampleRegressionTest}
import com.workday.warp.persistence.{Connection, CorePersistenceAware, CorePersistenceUtils, ExecutionTag}

/**
  * Hypothesis Testing example
  *
  * Created by vignesh.kalidas on 4/27/18.
  */
object HypothesisTestExample extends CorePersistenceAware {
  case class HypothesisTestResult(baselineAvg: Double, newSeriesAvg: Double, pValue: Double, testName: String)

  /**
    * µ1: Average performance of a single database operation
    * µ2: Average performance of multiple database operations
    * Ho: µ1 = µ2. The average performance of a single database operation is the same as the average performance of
    *     multiple database operations
    * Ha: µ1 < µ2. The average performance of a single database operation is faster than the average performance of
    *     multiple database operations
    *
    * @return useful metrics from the Hypothesis Test: the average for each series, the p-value, and the name of the
    *         statistical test performed
    */
  def hypothesisTestSpec(): HypothesisTestResult = {
    val methodSignature: String = "com.workday.warp.product.subproduct.Class.method"

    /*
     * To ensure neither the previous state of the database nor the other experiment interferes with the imminent
     * experiment, we drop and reinitialize the schema.
     */
    CorePersistenceUtils.dropSchema()
    CorePersistenceUtils.initSchema()
    Connection.refresh()
    val baselineResults: Seq[TrialResult[_]] = using only defaults warmups 4 invocations 256 tags {
      List(ExecutionTag("db-experiment", "baseline"))
    } measure {
      for (i <- 1 to 20) {
        this.persistenceUtils.findOrCreateMeasurementName(i.toString)
      }
    }

    CorePersistenceUtils.dropSchema()
    CorePersistenceUtils.initSchema()
    Connection.refresh()
    val newSeriesResults: Seq[TrialResult[_]] = using only defaults warmups 4 invocations 256 tags {
      List(ExecutionTag("db-experiment", "newSeries"))
    } measure {
      for (i <- 1 to 20) {
        val execution = this.persistenceUtils.createTestExecution(methodSignature, new Date, i + 1, 500)
        this.persistenceUtils.recordTestExecutionTag(execution.idTestExecution, "some name", "tagValue")
        this.persistenceUtils.recordMeasurement(execution.idTestExecution, "some measurement name", 0.1)
      }
    }

    // Retrieve the results of timing the experiments
    val baselineTimes: Array[Double] = baselineResults.map(_.maybeTestExecution.get.responseTime * 1000).toArray
    val newSeriesTimes: Array[Double] = newSeriesResults.map(_.maybeTestExecution.get.responseTime * 1000).toArray
    val statTestResult: Option[AllRegressionStatTestResults] = TwoSampleRegressionTest.testDifferenceOfMeans(
      baselineTimes, "baseline", newSeriesTimes, "newSeries"
    )

    val baselineAvg: Double = baselineTimes.sum / baselineTimes.length
    val newSeriesAvg: Double = newSeriesTimes.sum / newSeriesTimes.length
    val pValue: Double = statTestResult.get.regressionTest.pValue
    val test: String = statTestResult.get.regressionTest.testType.name

    /*
     * If this were a unit test, instead of returning a compilation of intermediate results, you could provide an
     * assertion:`pValue should be < 0.05`
     */
    HypothesisTestResult(baselineAvg, newSeriesAvg, pValue, test)
  }
}
