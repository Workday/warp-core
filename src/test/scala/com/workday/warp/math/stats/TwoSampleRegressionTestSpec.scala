package com.workday.warp.math.stats

import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import org.junit.Test
import org.junit.experimental.categories.Category

import scala.util.Random

class TwoSampleRegressionTestSpec extends WarpJUnitSpec {
  val alpha: Double = 0.05

  /**
    * No results should be generated if there aren't enough samples
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def notEnoughSamplesSpec(): Unit = {
    val smallSampleSize: Array[Double] = Array(1, 2, 3, 4)
    val emptyStatResults: Option[AllRegressionStatTestResults] =
      TwoSampleRegressionTest.testDifferenceOfMeans(smallSampleSize, "", smallSampleSize, "", alpha)

    emptyStatResults should be (None)
  }

  /**
    * Regression should be detected by T-Test
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def regressionSpec(): Unit = {
    val baselineNormalResponseTimes: Array[Double] = Array.fill(50)(50 + (Random.nextGaussian * 3))
    val regressedNormalResponseTimes: Array[Double] = Array.fill(50)(54 + (Random.nextGaussian * 4))

    val regressedTTestStatResults: AllRegressionStatTestResults =
      TwoSampleRegressionTest.testDifferenceOfMeans(baselineNormalResponseTimes, "", regressedNormalResponseTimes, "", alpha).get

    regressedTTestStatResults.baselineNormalityTest.pValue should be > alpha
    regressedTTestStatResults.newNormalityTest.pValue should be > alpha
    regressedTTestStatResults.regressionTest shouldBe a[TTestResult]
    regressedTTestStatResults.regressionTest.pValue should be < alpha
  }

  /**
    * No regression, also tests that one tailed test is working when new response times are less than the baseline
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def noRegressionSpec(): Unit = {
    val baselineNormalResponseTimes: Array[Double] = Array.fill(50)(50 + (Random.nextGaussian * 3))
    val improvedNormalResponseTimes: Array[Double] = Array.fill(50)(46 + (Random.nextGaussian * 4))

    val improvedTTestStatResults: AllRegressionStatTestResults =
      TwoSampleRegressionTest.testDifferenceOfMeans(baselineNormalResponseTimes, "", improvedNormalResponseTimes, "", alpha).get
    improvedTTestStatResults.baselineNormalityTest.pValue should be > alpha
    improvedTTestStatResults.newNormalityTest.pValue should be > alpha
    improvedTTestStatResults.regressionTest shouldBe a[TTestResult]
    improvedTTestStatResults.regressionTest.pValue should be > alpha
  }

  /**
    * Regression detected by U-Test
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def uTestRegressionSpec(): Unit = {
    // Test U-Test used appropriately; total samples has to be less than 30 as well to not fulfill CLT
    // Adding an outlier group separate from the main group makes the normality test fail
    val baselineNonNormalResponseTimes: Array[Double] =
      Array.fill(18)(50 + (Random.nextDouble * 3)) ++ Array.fill(8)(60 + (Random.nextDouble * 7))
    val improvedNonNormalResponseTimes: Array[Double] =
      Array.fill(18)(47 + (Random.nextDouble * 3)) ++ Array.fill(8)(57 + (Random.nextDouble * 7))
    val improvedUTestStatResults: AllRegressionStatTestResults =
      TwoSampleRegressionTest.testDifferenceOfMeans(baselineNonNormalResponseTimes, "",
                                                    improvedNonNormalResponseTimes, "",
                                                    alpha).get

    improvedUTestStatResults.baselineNormalityTest.pValue should be < alpha
    improvedUTestStatResults.newNormalityTest.pValue should be < alpha
    improvedUTestStatResults.regressionTest shouldBe a[StatTestResult]
    improvedUTestStatResults.regressionTest.pValue should be > alpha
  }

  /**
    * No regression detected by U-Test
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def uTestNoRegressionSpec(): Unit = {
    val baselineNonNormalResponseTimes: Array[Double] =
      Array.fill(18)(50 + (Random.nextDouble * 3)) ++ Array.fill(8)(60 + (Random.nextDouble * 7))
    val regressedNonNormalResponseTimes: Array[Double] =
      Array.fill(18)(53 + (Random.nextDouble * 3)) ++ Array.fill(8)(63 + (Random.nextDouble * 7))
    val regressedUTestStatResults: AllRegressionStatTestResults =
      TwoSampleRegressionTest.testDifferenceOfMeans(baselineNonNormalResponseTimes, "",
                                                    regressedNonNormalResponseTimes, "",
                                                    alpha).get

    regressedUTestStatResults.baselineNormalityTest.pValue should be < alpha
    regressedUTestStatResults.newNormalityTest.pValue should be < alpha
    regressedUTestStatResults.regressionTest shouldBe a[StatTestResult]
    regressedUTestStatResults.regressionTest.pValue should be < alpha
  }

  /**
    * Regression should be detected by two-sided T-Test
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def twoSidedRegressionSpec(): Unit = {
    val baselineNormalResponseTimes: Array[Double] = Array.fill(50)(50 + (Random.nextGaussian * 3))
    val regressedNormalResponseTimes: Array[Double] = Array.fill(50)(54 + (Random.nextGaussian * 4))

    val twoSided: AllRegressionStatTestResults =
      TwoSampleRegressionTest.testDifferenceOfMeans(baselineNormalResponseTimes, "", regressedNormalResponseTimes, "",
      alpha, isTwoSided = true).get
    val oneSided: AllRegressionStatTestResults =
      TwoSampleRegressionTest.testDifferenceOfMeans(baselineNormalResponseTimes, "", regressedNormalResponseTimes, "",
        alpha).get

    twoSided.regressionTest.pValue should be (2 * oneSided.regressionTest.pValue)
  }

  @Test
  @Category(Array(classOf[UnitTest]))
  def allRegressionStatTestResultsSpec(): Unit = {
    val baseline: Array[Double] = Array(50.47447827652541, 51.19056188274574, 46.69662195125984, 50.007577732739456, 50.73212737638565,
      48.720532737394244, 50.37628834545585, 47.430369257858835, 49.843991279948774, 52.74919901693785, 49.84939042441965,
      50.22471204583642, 52.17880361260057, 51.55835199934892, 47.63629420088256, 50.30541009636413, 48.9791243878714, 47.41514762769728,
      53.03984642449924, 53.164206231291026, 52.616586172672086, 52.461504472437994, 57.37002726210362, 52.087832335016685,
      51.566861124295166, 47.417565922523686, 49.50294294702823, 45.67148663143992, 48.24809154629195, 47.822892199809836,
      46.04215177780783, 52.53186349754487, 46.28609569648771, 48.670411676403845, 48.416855542631644, 53.03181993975837,
      51.687675493876505, 53.07885465033194, 52.09962664333855, 51.89332697906147, 46.33135020545271, 48.32966521292674,
      49.91222023766576, 56.876512520854625, 53.28057507992899, 53.42773707832225, 48.751833217314704, 48.00134951645327,
      48.33885561418202, 49.951357463993816)
    val regression: Array[Double] = Array(56.22548793696133, 52.01864730269899, 52.51257147367692, 53.2019524259662, 61.22523654366998,
      53.426509273761745, 49.256950019514704, 54.0352718806236, 48.16211793443062, 62.20226579975082, 60.51560614241627,
      58.78448522244503, 55.37978397070854, 53.647254361354015, 56.93120966627623, 60.42554241406304, 56.50461888207819,
      57.33530313830278, 51.40433045411728, 53.190330336512524, 57.84660165957101, 54.318564730831106, 54.43240347395981,
      50.667882918296144, 52.92581587390851, 52.13238299697053, 47.39016585724261, 54.60191314197977, 55.45002241477095,
      56.466882207383804, 54.11088967346992, 53.89882202487878, 55.141126836353685, 57.70086386675392, 56.647887029525215,
      49.34491951840146, 63.88031598424999, 46.21520454710136, 50.95475895636524, 59.07181121338295, 54.62086081444151, 59.59686662356923,
      55.852033139005215, 52.88763961916183, 55.02177911674332, 57.825460565849966, 57.40258070773403, 57.14611977913637,
      53.72711782865388, 51.266241878543866)

    val expectedString: String =
      """Kolmogorov-Smirnov Normality Test, p-value: 0.8732, Test Reference: https://en.wikipedia.org/wiki/Kolmogorov%E2%80%93Smirnov_test,
       | Series label: Kolmogorov-Smirnov Normality Test, p-value: 0.9948,
       | Test Reference: https://en.wikipedia.org/wiki/Kolmogorov%E2%80%93Smirnov_test, Series label: Welch's T-Test, p-value: 0.0,
       | Test Reference: https://en.wikipedia.org/wiki/Welch%27s_t-test, 95% Confidence Interval: (-∞, -3.5366)"""
        .stripMargin.replaceAll("\n", "")

    val oneSided: AllRegressionStatTestResults = TwoSampleRegressionTest.testDifferenceOfMeans(baseline, "", regression, "", alpha).get

    oneSided.toString should be (expectedString)
  }
}
