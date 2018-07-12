package com.workday.warp.math.stats

import org.apache.commons.math3.distribution.{FDistribution, NormalDistribution, TDistribution}
import org.apache.commons.math3.stat.StatUtils
import org.apache.commons.math3.stat.descriptive.moment.Variance
import org.apache.commons.math3.stat.descriptive.rank.Percentile
import org.apache.commons.math3.stat.inference.{KolmogorovSmirnovTest, TTest}
import org.pmw.tinylog.Logger

sealed abstract class TestType(val name: String, val referenceUrl: String)
case object KsTest extends TestType(name = "Kolmogorov-Smirnov Normality Test",
                                    referenceUrl = "https://en.wikipedia.org/wiki/Kolmogorov%E2%80%93Smirnov_test")
case object MannWhitneyUTest extends TestType(name = "Mann-Whitney U Test",
                                              referenceUrl = "https://en.wikipedia.org/wiki/Mann%E2%80%93Whitney_U_test")
case object WelchsTTest extends TestType(name = "Welch's T-Test",
                                         referenceUrl = "https://en.wikipedia.org/wiki/Welch%27s_t-test")
case object StudentsTTest extends TestType(name = "Student's T-Test",
                                           referenceUrl = "https://en.wikipedia.org/wiki/Student%27s_t-test")
case object LevenesVarienceHomogenietyTest extends TestType(name = "Levene's Variance Homogeneity Test",
                                                            referenceUrl = "https://en.wikipedia.org/wiki/Levene%27s_test")
case object BrownForsytheTest extends TestType(name = "Brown-Forsythe Variance Homogeneity Test",
                                               referenceUrl = "https://en.wikipedia.org/wiki/Brown%E2%80%93Forsythe_test")

/**
  * Represents the result of a statistical comparison test.
  *
  * @param testType
  * @param pValue
  */
abstract class GenericStatTestResult(val testType: TestType, val pValue: Double) {

  def withDecimalPrecision(value: Double, precision: Int): Double = {
    val decimalShifter: Double = math.pow(10, precision)
    math.floor(value * decimalShifter) / decimalShifter
  }
  override def toString: String = s"""${testType.name}, p-value: ${withDecimalPrecision(pValue, 4).toString},
                                     | Test Reference: ${testType.referenceUrl}""".stripMargin.replaceAll("\n", "")
}

case class StatTestResult(override val testType: TestType,
                          override val pValue: Double,
                          seriesLabel: String = "") extends GenericStatTestResult(testType, pValue) {
  override def toString: String = {
    super.toString() + s", Series label:$seriesLabel"
  }
}

case class TTestResult(override val testType: TestType,
                       override val pValue: Double,
                       upperBound: Double) extends GenericStatTestResult(testType, pValue) {
  override def toString: String = {
    super.toString() + s", 95% Confidence Interval: (-∞, ${withDecimalPrecision(upperBound, 4).toString})"
  }
}

/**
  * Wrapper class to hold all the relevant stat tests used in performing a regression test
  *
  * @param baselineNormalityTest
  * @param newNormalityTest
  * @param maybeHomoVarianceTest
  * @param regressionTest
  */
case class AllRegressionStatTestResults(baselineNormalityTest: StatTestResult,
                                        newNormalityTest: StatTestResult,
                                        maybeHomoVarianceTest: Option[StatTestResult],
                                        regressionTest: GenericStatTestResult) {
  // helper function used to print out optional line breaks for the view
  private def printMaybeTest(test: Option[StatTestResult]) : String = if (test.isDefined) s"${test.get.toString}" else ""

  override def toString: String = {
    s"""${baselineNormalityTest.toString} ${newNormalityTest.toString} ${printMaybeTest(maybeHomoVarianceTest)}
      |${regressionTest.toString}""".stripMargin.replaceAll("\n", "")
  }
}

object TwoSampleRegressionTest {
  private val SAMPLE_SIZE_LOWER_BOUND: Int = 5
  // sample size cutoff pertaining to the Central Limit Theorem
  private val CLT_N: Int = 30
  private val WEAK_NORMALITY_PVALUE_CUTOFF: Double = 0.4

  /**
    * Runs a levene test or brown-forsythe test depending on whether weakNormality is present in either group. Both tests
    * are similar mathematically, except that the former uses means and the latter uses medians.
    *
    * @param series1 a series of response times
    * @param series2 a series of response times
    * @param weakNormality whether weak normality is present in either series1 or series2
    *
    * @return the p-value associated with running either a levene's test or brown-forsythe test for variance homogeneity
    */
  private def equalVarianceTestProbability(series1: Array[Double], series2: Array[Double], weakNormality: Boolean): Double = {
    val N: Int = series1.length + series2.length
    val k: Int = 2
    val percentileCalculator: Percentile = new Percentile

    // definition of "average" is either median or mean, based on if weakNormality is true or false respectively
    def absoluteDifferenceFromAverage(x: Double, sample: Array[Double]): Double = {
      if (weakNormality) {
        Math.abs(x - percentileCalculator.evaluate(sample, 50))
      }
      else {
        Math.abs(x - StatUtils.mean(sample))
      }
    }

    def differenceSquared(x: Double, y: Double): Double = Math.pow(x - y, 2)

    val series1AbsDeviations: Array[Double] = series1 map (sample => absoluteDifferenceFromAverage(sample, series1))
    val series1MeanAbsDeviation: Double = StatUtils.mean(series1AbsDeviations)
    val series2AbsDeviations: Array[Double] = series2 map (sample => absoluteDifferenceFromAverage(sample, series2))
    val series2MeanAbsDeviation: Double = StatUtils.mean(series2AbsDeviations)
    val totalMeanAbsDeviation: Double = StatUtils.mean(series1AbsDeviations ++ series2AbsDeviations)

    val numerator: Double = (series1.length * differenceSquared(series1MeanAbsDeviation, totalMeanAbsDeviation) +
                             series2.length * differenceSquared(series2MeanAbsDeviation, totalMeanAbsDeviation)) /
                            (k - 1)
    val denominator: Double = (StatUtils.sum(series1AbsDeviations map (sampleDeviation =>
                               differenceSquared(sampleDeviation, series1MeanAbsDeviation))) +
                               StatUtils.sum(series2AbsDeviations map (sampleDeviation =>
                               differenceSquared(sampleDeviation, series2MeanAbsDeviation)))) / (N - k)

    val fDistribution: FDistribution = new FDistribution(k-1, N-k)
    val fStatistic: Double = numerator / denominator
    Logger.trace(s"F-Statistic for equal variance test: $fStatistic")
    1 - fDistribution.cumulativeProbability(fStatistic)
  }

  /** U statistic calculation comparing newSeries array average to be greater than baselineGroup array average.
    *
    * @param baselineSeries the original series
    * @param newSeries the new series
    * @return the U statistic associated with conducting the test
    */
  private def calculateMannWhitneyUStatisticOneSided(baselineSeries: Array[Double], newSeries: Array[Double]): Double = {
    (newSeries map {newSeriesSample => (baselineSeries map {baselineSample =>
      if (newSeriesSample > baselineSample) {
        1
      }
      else if (newSeriesSample == baselineSample) {
        0.5
      }
      else {
        0
      }
    }).sum
    }).sum
  }

  /** since we enforced that n1*n2>20, we can use a normal distribution approximation to obtain a p-value from the U statistic
    *
    * @param UStatistic the u statistic to get an approximated p-value for
    * @param n1 length of the first series
    * @param n2 length of the second series
    * @return p-value associated with the u statistic
    */
  private def mannWhitneyUNormalApproximationPValue(UStatistic: Double, n1: Double, n2: Double): Double = {
    val mu: Double = n1 * n2 / 2
    val variance: Double = (n1 * n2) * (n1 + n2 + 1) / 12
    val z: Double = (UStatistic - mu) / Math.sqrt(variance)
    val normalDistribution: NormalDistribution = new NormalDistribution(0, 1)

    Logger.trace(s"approximated z-value: $z")
    1 - normalDistribution.cumulativeProbability(z)
  }

  /** conduct a one-sided Mann-Whitney U Test comparing New Group average to be higher than Baseline Group average
    *
    * @param baselineSeries the original series
    * @param baselineSeriesSize the size of the original series
    * @param newSeries the new series
    * @param newSeriesSize the size of the new series
    * @param alpha the significance level used to reject the test
    * @return a StatTestResult storing the display info for the test and the p-value result
    */
  private def conductOneSidedMannWhitneyUTest(baselineSeries: Array[Double], baselineSeriesSize: Int,
                                              newSeries: Array[Double], newSeriesSize: Int, alpha: Double): StatTestResult = {
    val mannWhitneyUStatistic: Double = calculateMannWhitneyUStatisticOneSided(baselineSeries, newSeries)
    val mannWhitneyUTestPValue: Double = mannWhitneyUNormalApproximationPValue(mannWhitneyUStatistic, baselineSeriesSize,
                                                                               newSeriesSize)
    Logger.trace(s"Mann-Whitney U-Test statistic: $mannWhitneyUStatistic, p-value: $mannWhitneyUTestPValue")

    if (mannWhitneyUTestPValue < alpha) {
      Logger.trace("A significant regression has been detected")
    }
    else {
      Logger.trace("There is no significant regression detected")
    }

    StatTestResult(MannWhitneyUTest, mannWhitneyUTestPValue)
  }

  /** helper function for T-Test
    *
    * @param n1 size of series1
    * @param v1 variance of series1
    * @param n2 size of series2
    * @param v2 variance of series2
    * @return pooled variance of the two series
    */
  private def calculatePooledVariance(n1: Double, v1: Double, n2: Double, v2: Double): Double = {
    (n1 - 1) * v1 + (n2 - 1) * v2 / (n1 + n2 - 2)
  }

  /** helper function for T-Test
    *
    * @param v1 variance of series1
    * @param v2 variance of series2
    * @param n1 size of series1
    * @param n2 size of series2
    * @return the df to be used for the t-distribution in Welch's T-Test
    */
  private def calculateWelchsDf(v1: Double, v2: Double, n1: Double, n2: Double): Int = {
    Math.floor((((v1 / n1) + (v2 / n2)) * ((v1 / n1) + (v2 / n2))) /
               ((v1 * v1) / (n1 * n1 * (n1 - 1d)) + (v2 * v2) / (n2 * n2 * (n2 - 1d))))
      .toInt
  }

  /** helper function for T-Test
    *
    * @param baselineSeries the original series
    * @param newSeries the new series
    * @param isHomoscedastic whether both series have the same constant variance
    * @param isTwoSided whether the t-test performed should be two-sided; this affects which alternate hypothesis to use
    * @return a tuple packaging together the t-test statistic, the p-value, and the hyperlink for the view
    */
  private def tTestResults(baselineSeries: Array[Double], newSeries: Array[Double],
                           isHomoscedastic: Boolean, isTwoSided: Boolean): (Double, Double, TestType) = {
    val tTest: TTest = new TTest

    /*
     * From the documentation for TTest.tTest and TTest.homoscedasticTTest:
     * "For a one-sided, divide the returned value by 2"
     */
    val tailedTestDivisor: Double = if (isTwoSided) 1.0 else 2.0

    if (isHomoscedastic) {
      val tTestStatistic: Double = tTest.homoscedasticT(baselineSeries, newSeries)
      val tTestResult: Double = tTest.homoscedasticTTest(baselineSeries, newSeries) / tailedTestDivisor
      val tTestPValue: Double = if (tTestStatistic < 0) tTestResult else 1 - tTestResult
      (tTestStatistic, tTestPValue, StudentsTTest)
    }
    else {
      val tTestStatistic: Double = tTest.t(baselineSeries, newSeries)
      val tTestResult: Double = tTest.tTest(baselineSeries, newSeries) / tailedTestDivisor
      val tTestPValue: Double = if (tTestStatistic < 0) tTestResult else 1 - tTestResult
      (tTestStatistic, tTestPValue, WelchsTTest)
    }
  }

  /** helper function for T-Test, used to construct a confidence interval
    *
    * @param baselineSeriesVariance variance of the baseline series
    * @param baselineSeriesSize size of the baseline series
    * @param newSeriesVariance variance of the new series
    * @param newSeriesSize size of the new series
    * @param isHomoscedastic whether both series have the same constant variance
    * @param alpha significance level used to reject the test
    * @return a tuple packaging the critical t-value for the given alpha and the standard error
    */
  private def getTDistributionParameters(baselineSeriesVariance: Double, baselineSeriesSize: Int, newSeriesVariance: Double,
                                         newSeriesSize: Int, isHomoscedastic: Boolean, alpha: Double): (Double, Double) = {
    // degrees of freedom and the standard error use different formulas based on students and welchs tests
    if (isHomoscedastic) {
      val df: Int = baselineSeriesSize + newSeriesSize - 2
      val tDist: TDistribution = new TDistribution(df)
      val standardError: Double = Math.sqrt(calculatePooledVariance(baselineSeriesSize, baselineSeriesVariance,
                                            newSeriesSize, newSeriesVariance))
      (tDist.inverseCumulativeProbability(1 - alpha), standardError)
    }
    else {
      val df: Int = calculateWelchsDf(baselineSeriesVariance, newSeriesVariance, baselineSeriesSize, newSeriesSize)
      val tDist: TDistribution = new TDistribution(df)
      val standardError: Double = Math.sqrt((baselineSeriesVariance / baselineSeriesSize) + (newSeriesVariance / newSeriesSize))
      (tDist.inverseCumulativeProbability(1 - alpha), standardError)
    }
  }

  /**
    * Conduct one sided t-test, either Student's or Welch's depending on if homoscedasticity of variances is assumed or
    * not respectively.
    *
    * @param baselineSeries the original series
    * @param baselineSeriesSize the size of the original series
    * @param newSeries the new series
    * @param newSeriesSize the size of the new series
    * @param alpha the significance level used to reject the test
    * @param isHomoscedastic whether both series have the same constant variance
    * @param isTwoSided whether the t-test performed should be two-sided; this affects which alternate hypothesis to use
    * @return TTestResult with the display info, the p-value, and hte upperBound for the one sided confidence interval
    */
  private def conductTTest(baselineSeries: Array[Double], baselineSeriesSize: Int, newSeries: Array[Double],
                           newSeriesSize: Int, alpha: Double, isHomoscedastic: Boolean,
                           isTwoSided: Boolean): TTestResult = {
    val (tTestStatistic, tTestPValue, testType): (Double, Double, TestType) = tTestResults(baselineSeries,
                                                                                                 newSeries,
                                                                                                 isHomoscedastic,
                                                                                                 isTwoSided)
    Logger.trace(s"T-Test Statistic: $tTestStatistic, p-value: $tTestPValue")

    // calculate confidence interval
    val baselineSeriesVariance: Double = StatUtils.variance(baselineSeries)
    val newSeriesVariance: Double = StatUtils.variance(newSeries)

    val (criticalTValue, standardError): (Double, Double) =
      getTDistributionParameters(baselineSeriesVariance, baselineSeriesSize, newSeriesVariance, newSeriesSize,
                                 isHomoscedastic, alpha)

    val differenceInMeans: Double = StatUtils.mean(baselineSeries) - StatUtils.mean(newSeries)
    val upperBound: Double = differenceInMeans + criticalTValue * standardError
    Logger.trace(s"one-sided T-Test Confidence Interval: (-inf, $upperBound)")

    if (tTestPValue < alpha) {
      Logger.trace("A significant regression has been detected")
    }
    else {
      Logger.trace("There is no significant regression detected")
    }

    TTestResult(testType, tTestPValue, upperBound)
  }

  /** conduct a Kolmogorov-SmirnovTest to test null hypothesis that a series comes from a normal distribution
    *
    * @param series the series to compare to a normal distribution
    * @return the p-value associated with testing the above hypothesis
    */
  private def normalityKSTest(series: Array[Double]): Double = {
    val normalityTest: KolmogorovSmirnovTest = new KolmogorovSmirnovTest
    normalityTest.kolmogorovSmirnovTest(new NormalDistribution, StatUtils.normalize(series))
  }

  /** choose either student's or welch's t-test based on the result of a variance equality test
    *
    * @param baselineSeries the original series
    * @param baselineSeriesNormalityPValue the normality p-value of the original series
    * @param baselineSeriesSize the size of the original series
    * @param newSeries the new series
    * @param newSeriesNormalityPValue the normality p-value of the new series
    * @param newSeriesSize the size of the new series
    * @param alpha the significance level to reject the test
    * @return the result of the homogeneity check and also the result of the t-test chosen packaged into a 2-tuple
    */
  private def chooseAndConductTTest(baselineSeries: Array[Double], baselineSeriesNormalityPValue: Double,
                                    baselineSeriesSize: Int, newSeries: Array[Double], newSeriesNormalityPValue: Double,
                                    newSeriesSize: Int, alpha: Double,
                                    isTwoSided: Boolean): (Option[StatTestResult], GenericStatTestResult) = {
    // use brown-forsythe test if normality isn't particularly evident, and levene's test if it is
    val (equalVariancePValue, testType): (Double, TestType) =
      if (normalityCheck(baselineSeriesNormalityPValue, baselineSeriesSize, newSeriesNormalityPValue, newSeriesSize,
                         WEAK_NORMALITY_PVALUE_CUTOFF)) {
        Logger.trace("Somewhat weak normality; Using Browns-Forsythe test for variance equality")
        val brownForsythePValue: Double = equalVarianceTestProbability(baselineSeries, newSeries, weakNormality = true)
        (brownForsythePValue, BrownForsytheTest)
      }
      else {
        Logger.trace("Somewhat strong normality present; Using Levene's test for variance equality")
        val levenePValue: Double = equalVarianceTestProbability(baselineSeries, newSeries, weakNormality = false)
        (levenePValue, LevenesVarienceHomogenietyTest)
      }
    Logger.trace(s"p-value: $equalVariancePValue")
    val homoVarianceTest: StatTestResult = StatTestResult(testType, equalVariancePValue)

    // if equal variance assumed, use homoscedastic t-test; else uses Welch's t-test
    val regressionTest: GenericStatTestResult = if (equalVariancePValue < alpha) {
      Logger.trace("equal variances rejected; using Welch's t-test")
      conductTTest(baselineSeries, baselineSeriesSize, newSeries, newSeriesSize, alpha,
                                  isHomoscedastic = false, isTwoSided)
    }

    else {
      Logger.trace("equal variances detected; using Student's t-test")
      conductTTest(baselineSeries, baselineSeriesSize, newSeries, newSeriesSize, alpha,
                                  isHomoscedastic = true, isTwoSided)
    }

    (Some(homoVarianceTest), regressionTest)
  }

  /** helper function to test if either CLT or the result of the KS test succeeds for both a baseline and new series
    *
    * @param baselineSeriesNormalityPValue normality p-value of original series
    * @param baselineSeriesSize size of original series
    * @param newSeriesNormalityPValue normality p-value of new series
    * @param newSeriesSize size of new series
    * @param pValueCutoff the cutoff at which to determine if normality is sufficiently present
    * @return the result of the check, either true or false
    */
  private def normalityCheck(baselineSeriesNormalityPValue: Double, baselineSeriesSize: Int, newSeriesNormalityPValue: Double,
                             newSeriesSize: Int, pValueCutoff: Double): Boolean = {
    if (baselineSeriesSize >= CLT_N) {
      Logger.trace("baseline group fulfills CLT; can disregard normality tests.")
    }
    if (newSeriesSize >= CLT_N) {
      Logger.trace("new group fulfills CLT; can disregard normality tests.")
    }

    (baselineSeriesSize < CLT_N && baselineSeriesNormalityPValue < pValueCutoff) ||
    (newSeriesSize < CLT_N && newSeriesNormalityPValue < pValueCutoff)
  }

  /** helper function to test if the variance of the two given arrays are both 0; if so a t-test cannot be used
    *
    * @param series1 first series of doubles
    * @param series2 second series of doubles
    * @return whether or not both variances are equal to 0
    */
  private def isBothZeroVariance(series1: Array[Double], series2: Array[Double]): Boolean = {
    val varianceCalculator: Variance = new Variance
    varianceCalculator.evaluate(series1) == 0 && varianceCalculator.evaluate(series2) == 0
  }

  /**
    * Attempt to conduct a test with the following hypotheses:
    *
    * H0: µ1 == µ2
    * Ha: µ1 < µ2 (if one-sided t-test)
    * Ha: µ1 != µ2 (if two-sided t-test)
    * µ1 refers to the baselineSeries and µ2 refers to the newSeries
    *
    * When using a one-sided test, it implies the new series on average has longer response times than the baseline,
    * i.e. a regression.
    * This is achieved either using a Mann-Whitney U test if normality assumption of either/both groups fail. Otherwise,
    * either a Student's t-test or Welch's t-test may be used, depending on if we choose based on variance homogeneity
    * between the two groups. Otherwise Welch's t-test is the default to use, which is considered powerful enough.
    *
    * @param baselineSeries the 'original' series of response times
    * @param baselineSeriesLabel the name used for the baseline series
    * @param newSeries the 'new' series of response times that we are testing to be a regression of the baseline
    * @param newSeriesLabel the name used for the new series
    * @param alpha significance level used to reject null hypotheses
    * @param checkVarianceHomogeneity can be used to decide between using a student's or welch's t-test. However, it is
    *                                 generally accepted that using welch's t-test immediately is sufficiently powerful;
    *                                 there is also a research paper indicating that power is decreased if the type of t-test
    *                                 is chosen off of a homogeneity check. Thus, this value is defaulted to false.
    * @param isTwoSided whether the t-test performed should be two-sided; this affects which alternate hypothesis to use
    *
    * @return An optional RegressionStatTestResults object, holding the specific test results used. Will be set to None
    *         if there is not enough data to begin the test.
    */
  def testDifferenceOfMeans(baselineSeries: Array[Double], baselineSeriesLabel: String, newSeries: Array[Double],
                            newSeriesLabel: String, alpha: Double = 0.05, checkVarianceHomogeneity: Boolean = false,
                            isTwoSided: Boolean = false):
                            Option[AllRegressionStatTestResults] = {
    val baselineSeriesSize: Int = baselineSeries.length
    val newSeriesSize: Int = newSeries.length

    // somewhat arbitrary cutoff; mann whitney U test normal approximation does require that n1*n2>20 though
    if (baselineSeriesSize < SAMPLE_SIZE_LOWER_BOUND || newSeriesSize < SAMPLE_SIZE_LOWER_BOUND) {
      Logger.trace("Not enough sample data in either/both sets, exiting")
      None
    }
    else {
      Logger.trace(s"Alpha level of $alpha used.")

      // test for normality of the samples
      val baselineSeriesNormalityPValue: Double = normalityKSTest(baselineSeries)
      val newSeriesNormalityPValue: Double = normalityKSTest(newSeries)
      Logger.trace(s"baseline group normality p-value: $baselineSeriesNormalityPValue, " +
        s"new group normality p-value: $newSeriesNormalityPValue")

      val baselineNormalityTest: StatTestResult = StatTestResult(KsTest, baselineSeriesNormalityPValue, baselineSeriesLabel)
      val newNormalityTest: StatTestResult = StatTestResult(KsTest, newSeriesNormalityPValue, newSeriesLabel)

      // if normality assumption isn't fulfilled (either by CLT or above tests), use non-parametric test (mann-whitney U test)
      val (maybeHomoVarianceTest, regressionTest): (Option[StatTestResult], GenericStatTestResult) =
        (normalityCheck(baselineSeriesNormalityPValue, baselineSeriesSize, newSeriesNormalityPValue, newSeriesSize, alpha)
          || isBothZeroVariance(baselineSeries, newSeries)) match {
          case true =>
            Logger.trace("cannot use t-test either because normality assumptions failed or both series variances are zero, " +
              "using mann-whitney U Test")
            (None, conductOneSidedMannWhitneyUTest(baselineSeries, baselineSeriesSize, newSeries, newSeriesSize, alpha))

          // t-test normality condition satisfied
          case false if checkVarianceHomogeneity =>
            Logger.trace("Normality assumption satisfied with variance homogeneity; proceeding with t-test.")
            chooseAndConductTTest(baselineSeries, baselineSeriesNormalityPValue, baselineSeriesSize, newSeries,
              newSeriesNormalityPValue, newSeriesSize, alpha, isTwoSided)
          case false =>
            Logger.trace("Normality assumption satisfied without variance homogeneity; proceeding with t-test.")
            (None, conductTTest(baselineSeries, baselineSeriesSize, newSeries, newSeriesSize, alpha, isHomoscedastic = false,
              isTwoSided))
      }

      Some(AllRegressionStatTestResults(baselineNormalityTest, newNormalityTest, maybeHomoVarianceTest, regressionTest))
    }
  }
}
