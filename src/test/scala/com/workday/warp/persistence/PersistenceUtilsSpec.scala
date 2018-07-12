package com.workday.warp.persistence

import java.sql.Timestamp
import java.time.LocalDate
import java.util

import slick.jdbc.MySQLProfile.api._
import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.persistence.Tables._
import com.workday.warp.persistence.TablesLike._
import com.workday.warp.persistence.TablesLike.RowTypeClasses._
import com.workday.warp.persistence.Tables.RowTypeClasses._
import com.workday.warp.persistence.CoreIdentifierType._
import org.junit.{Before, Test}
import org.junit.experimental.categories.Category


/**
  * Created by tomas.mccandless on 10/5/16.
  */
class PersistenceUtilsSpec extends WarpJUnitSpec with CorePersistenceAware {

  private val methodSignature: String = "com.workday.warp.product.subproduct.Class.method"
  private val anotherMethodSignature: String = this.methodSignature + "abcdef"
  private val longString: String = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTURWXYZ" +
    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTURWXYZ" +
    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTURWXYZ" +
    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTURWXYZ" +
    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTURWXYZ" +
    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTURWXYZ"


  private[this] def createTestExecution(responseTime: Double): TestExecutionRowLike = {
    val threshold: Double = responseTime + 1
    this.persistenceUtils.createTestExecution(this.methodSignature, new util.Date, responseTime, threshold)
  }


  /** Truncates the schema. */
  @Before
  def truncateSchema(): Unit = {
    Connection.refresh()
    CorePersistenceUtils.truncateSchema()
  }


  /**
    * Checks that we don't throw an exception when the schema already exists.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def doubleInit: Unit = {
    Connection.refresh()
    CorePersistenceUtils.initSchema()
    CorePersistenceUtils.initSchema()
  }


  /** Checks that we can find or create [[Tables.Build]]. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def findOrCreateBuild(): Unit = {
    this.persistenceUtils.findOrCreateBuild(2016, 1, 345).idBuild should be (1)
    this.persistenceUtils.findOrCreateBuild(2016, 1, 345).idBuild should be (1)
    this.persistenceUtils.findOrCreateBuild(2016, 1, 346).idBuild should be (2)
    this.persistenceUtils.findOrCreateBuild(2016, 1, 346).idBuild should be (2)

    val buildInfo1: BuildRowLike = this.persistenceUtils.findOrCreateBuild(2017, 1, 123)
    Thread.sleep(1500)
    val buildInfo2: BuildRowLike = this.persistenceUtils.findOrCreateBuild(2017, 1, 123)
    // even though we slept and thus used a different timestamp, they should be equal
    buildInfo1 should be (buildInfo2)
  }


  /** Checks that we can find or create [[Tables.TestDefinition]]. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def findOrCreateTestDefinition(): Unit = {
    this.persistenceUtils.findOrCreateTestDefinition(this.methodSignature).idTestDefinition should be (1)
    this.persistenceUtils.findOrCreateTestDefinition(this.methodSignature).idTestDefinition should be (1)
    this.persistenceUtils.findOrCreateTestDefinition(this.anotherMethodSignature).idTestDefinition should be (2)
    this.persistenceUtils.findOrCreateTestDefinition(this.anotherMethodSignature).idTestDefinition should be (2)

    // check that a long signature will be trimmed
    val longSignature = this.methodSignature + this.longString
    longSignature.length should be > CorePersistenceConstants.SIGNATURE_LENGTH
    this.persistenceUtils.findOrCreateTestDefinition(longSignature).methodSignature
                         .length should be (CorePersistenceConstants.SIGNATURE_LENGTH)

    // check that documentation will be updated
    this.persistenceUtils.findOrCreateTestDefinition(this.methodSignature).documentation should be (None)
    this.persistenceUtils.findOrCreateTestDefinition(this.methodSignature,
                                                   Some("documentation")).documentation should contain ("documentation")

    this.persistenceUtils.synchronously(
      this.persistenceUtils.readTestDefinitionQuery(this.methodSignature)
    ).headOption should not be empty
  }

  /** Checks that we can find or create [[Tables.MeasurementName]] with trimmed name. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def findOrCreateMeasurementName(): Unit = {
    this.persistenceUtils.findOrCreateMeasurementName("some name").idMeasurementName should be (1)
    this.persistenceUtils.findOrCreateMeasurementName("some name").idMeasurementName should be (1)
    this.persistenceUtils.findOrCreateMeasurementName("some other name").idMeasurementName should be (2)
    this.persistenceUtils.findOrCreateMeasurementName("some other name").idMeasurementName should be (2)

    // check that a long name will be trimmed
    this.longString.length should be > CorePersistenceConstants.DESCRIPTION_LENGTH
    this.persistenceUtils.findOrCreateMeasurementName(this.longString)
                         .name.length should be (CorePersistenceConstants.DESCRIPTION_LENGTH)
  }


  /** Checks that we can find or create [[Tables.TagName]] with trimmed name. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def findOrCreateTagName(): Unit = {
    this.persistenceUtils.findOrCreateTagName("some name").idTagName should be (1)
    this.persistenceUtils.findOrCreateTagName("some name").idTagName should be (1)
    this.persistenceUtils.findOrCreateTagName("some other name").idTagName should be (2)
    this.persistenceUtils.findOrCreateTagName("some other name").idTagName should be (2)

    // check that a long name will be trimmed
    this.longString.length should be > CorePersistenceConstants.DESCRIPTION_LENGTH
    this.persistenceUtils.findOrCreateTagName(this.longString)
                         .name.length should be (CorePersistenceConstants.DESCRIPTION_LENGTH)
  }


  /** Checks that we can record a [[TestExecutionTagRowLike]], and that it properly returns the whole row including the autoInc rowID */
  @Test
  @Category(Array(classOf[UnitTest]))
  def recordTestExecutionTag(): Unit = {
    val testExecution: TestExecutionRowLike = this.createTestExecution(1)

    val before: Int = this.persistenceUtils.synchronously(TestExecutionTag.length.result)
    val testExecutionTagRow: TestExecutionTagRowLike = this.persistenceUtils.recordTestExecutionTag(
      testExecution.idTestExecution, "some name", "tag value", isUserGenerated = true
    )
    val testExecutionTagRow2: TestExecutionTagRowLike = this.persistenceUtils.recordTestExecutionTag(
      testExecution.idTestExecution, "some name2", "tag value2", isUserGenerated = true
    )
    val after: Int = this.persistenceUtils.synchronously(TestExecutionTag.length.result)

    after should be (before + 2)

    // test that the entire row is returned properly
    testExecutionTagRow.idTestExecutionTag should be (1)
    testExecutionTagRow.idTestExecution should be (testExecution.idTestExecution)
    testExecutionTagRow.idTagName should be (1)
    testExecutionTagRow.value should be ("tag value")

    // test auto inc ID
    testExecutionTagRow2.idTestExecutionTag should be (2)

    this.persistenceUtils.synchronously(
      this.persistenceUtils.testExecutionTagsQuery(testExecution.idTestExecution)
    ).headOption should not be empty
  }


  /** Checks that we can record a [[TestDefinitionTagRow]], and that it properly returns the whole row including the autoInc rowID */
  @Test
  @Category(Array(classOf[UnitTest]))
  def recordTestDefinitionTag(): Unit = {
    val testExecution: TestExecutionRow = this.createTestExecution(1)
    val testExecution2: TestExecutionRow = this.createTestExecution(2)

    val before: Int = this.persistenceUtils.synchronously(TestDefinitionTag.length.result)
    val testDefinitionTagRow: TestDefinitionTagRow = this.persistenceUtils.recordTestDefinitionTag(testExecution.idTestDefinition,
                                                                                             "instanceId", "755$1234")
    val testDefinitionTagRow2: TestDefinitionTagRow = this.persistenceUtils.recordTestDefinitionTag(testExecution2.idTestDefinition,
                                                                                              "instanceId2", "755$1234")
    val after: Int = this.persistenceUtils.synchronously(TestDefinitionTag.length.result)

    after should be (before + 2)

    // test that the entire row is returned properly
    testDefinitionTagRow.idTestDefinitionTag should be (1)
    testDefinitionTagRow.idTestDefinition should be (testExecution.idTestDefinition)
    testDefinitionTagRow.idTagName should be (1)
    testDefinitionTagRow.value should be ("755$1234")

    // test auto inc ID
    testDefinitionTagRow2.idTestDefinitionTag should be (2)

    val testDefinition: Tables.TestDefinitionRow = this.persistenceUtils.synchronously(
      TestDefinition.filter(_.idTestDefinition === 1)
    ).headOption.get
    this.persistenceUtils.recordTestDefinitionTag(testDefinition, "instanceId3", "755$5678", isUserGenerated = true)
    val after2: Int = this.persistenceUtils.synchronously(TestDefinitionTag.length.result)
    after2 should be (after + 1)
  }


  /** Checks that we can record a [[TestDefinitionMetaTagRow]]. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def recordTestDefinitionMetaTag(): Unit = {
    val testExecution: TestExecutionRow = this.createTestExecution(1)
    this.persistenceUtils.recordTestDefinitionTag(testExecution.idTestDefinition, "instanceId", "755$1234")

    val before: Int = this.persistenceUtils.synchronously(Tables.TestDefinitionMetaTag.length.result)
    this.persistenceUtils.recordTestDefinitionMetaTag(1, "Key", "Value")
    val after: Int = this.persistenceUtils.synchronously(Tables.TestDefinitionMetaTag.length.result)

    after should be (before + 1)
  }

  /** Checks that we can record a [[TestDefinitionMetaTagRow]]. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def recordTestExecutionMetaTag(): Unit = {
    val testExecution: TestExecutionRow = this.createTestExecution(1)
    this.persistenceUtils.recordTestExecutionTag(testExecution.idTestExecution, "some name", "some value", isUserGenerated = true)

    val before: Int = this.persistenceUtils.synchronously(Tables.TestExecutionMetaTag.length.result)
    this.persistenceUtils.recordTestExecutionMetaTag(1, "Key", "Value")
    val after: Int = this.persistenceUtils.synchronously(Tables.TestExecutionMetaTag.length.result)

    after should be (before + 1)
  }


  /** Checks that we can create [[Tables.TestExecution]]. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def createTestExecution(): Unit = {
    // insert a test execution, should have id 1
    val testExecution: Tables.TestExecutionRow = this.persistenceUtils.createTestExecution(this.methodSignature, new util.Date, 5.0, 6.0)
    testExecution.idTestExecution should be (1)

    // insert a few more, with different signatures, and check that we can find all the ones for a ce
    this.persistenceUtils.createTestExecution(this.methodSignature, new util.Date, 5.0, 6.0).idTestExecution should be (2)
    this.persistenceUtils.createTestExecution(this.methodSignature, new util.Date, 5.0, 6.0).idTestExecution should be (3)
    this.persistenceUtils.createTestExecution(this.methodSignature, new util.Date, 5.0, 6.0)
    this.persistenceUtils.createTestExecution(this.anotherMethodSignature, new util.Date, 5.0, 6.0)
    this.persistenceUtils.createTestExecution(this.anotherMethodSignature, new util.Date, 5.0, 6.0)

    this.persistenceUtils.synchronously(this.persistenceUtils.readTestExecutionQuery(this.methodSignature)) should have length 4
    this.persistenceUtils.synchronously(this.persistenceUtils.readTestExecutionQuery(this.anotherMethodSignature)) should have length 2

    // there should be 6 total test executions
    this.persistenceUtils.synchronously(this.persistenceUtils.numTestExecutionsQuery) should be (6)

    val error: IllegalArgumentException = intercept[IllegalArgumentException] {
      this.persistenceUtils.createTestExecution(this.methodSignature, new util.Date, 0.0, 1.0)
    }
    error.getMessage should be ("Zero Time recorded for this measurement, check your adapter implementation.")
  }


  @Test
  @Category(Array(classOf[UnitTest]))
  def recordMeasurement(): Unit = {
    val testExecution: Tables.TestExecutionRow = this.persistenceUtils.createTestExecution(this.methodSignature, new util.Date, 5.0, 6.0)
    this.persistenceUtils.recordMeasurement(testExecution.idTestExecution, "some measurement name", 0.1)

    this.persistenceUtils.synchronously(
      this.persistenceUtils.measurementQuery(
        CoreIdentifier(this.methodSignature, testExecution.idTestDefinition),
        -1
      )
    ).headOption should not be empty
  }


  @Test
  @Category(Array(classOf[UnitTest]))
  def getResponseTimes(): Unit = {

    // insert a few test executions with different signatures
    this.persistenceUtils.createTestExecution(this.methodSignature, new util.Date, 5.0, 6.0)
    this.persistenceUtils.createTestExecution(this.methodSignature, new util.Date, 5.0, 6.0)
    this.persistenceUtils.createTestExecution(this.methodSignature, new util.Date, 5.0, 6.0)
    this.persistenceUtils.createTestExecution(this.anotherMethodSignature, new util.Date, 5.5, 6.0)
    this.persistenceUtils.createTestExecution(this.anotherMethodSignature, new util.Date, 5.5, 6.0)

    // check that we can read back the expected lists of response time
    this.persistenceUtils.getResponseTimes(CoreIdentifier(this.methodSignature)) should be (List(5.0, 5.0, 5.0))
    this.persistenceUtils.getResponseTimes(CoreIdentifier(this.anotherMethodSignature)) should be (List(5.5, 5.5))

    // insert another test execution and check that we can read back its response time
    this.persistenceUtils.createTestExecution(this.methodSignature, new util.Date, 5.1, 6.0)
    this.persistenceUtils.getResponseTimes(CoreIdentifier(this.methodSignature)) should be (List(5.0, 5.0, 5.0, 5.1))

    // check that we can exclude certain testcase id
    val excludedId: Int = 6
    this.persistenceUtils.getResponseTimes(CoreIdentifier(this.methodSignature), excludedId) should be (List(5.0, 5.0, 5.0))

    // check that we can exclude cases before startDateCutoff in addition to excluding certain testcase id
    val startDateLowerBound: LocalDate = LocalDate.now().minusWeeks(1)
    val someDateBeforeCutoff: Timestamp = new Timestamp(0)
    val someDateAfterCutoff: Timestamp = new Timestamp(System.currentTimeMillis())

    this.persistenceUtils.createTestExecution(this.methodSignature, someDateBeforeCutoff, 4.9, 6.0)
    this.persistenceUtils.createTestExecution(this.methodSignature, someDateAfterCutoff, 5.2, 6.0)
    this.persistenceUtils.getResponseTimes(CoreIdentifier(this.methodSignature),
                                           excludedId,
                                           startDateLowerBound) should be (List(5.0, 5.0, 5.0, 5.2))

  }


  /** Checks that we can compute average response times. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def average(): Unit = {
    // check average of empty list
    this.persistenceUtils.getAverageResponseTime(CoreIdentifier(this.methodSignature)).isNaN should be (true)

    this.persistenceUtils.createTestExecution(this.methodSignature, new util.Date, 5.0, 6.0)
    this.persistenceUtils.createTestExecution(this.methodSignature, new util.Date, 6.0, 6.0)

    this.persistenceUtils.getAverageResponseTime(CoreIdentifier(this.methodSignature)) should be (5.5 +- 0.01)
  }


  /** Checks that we can correctly read number of executions. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def getNumExecutions0(): Unit = {
    this.persistenceUtils.getNumExecutions(CoreIdentifier(this.methodSignature)) should be (0)
  }


  /** Checks that we can correctly read number of executions. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def getNumExecutions2(): Unit = {
    this.persistenceUtils.createTestExecution(this.methodSignature, new util.Date, 6.0, 6.0)
    this.persistenceUtils.createTestExecution(this.methodSignature, new util.Date, 6.0, 6.0)
    this.persistenceUtils.getNumExecutions(CoreIdentifier(this.methodSignature)) should be (2)
  }


  /** Checks that we can correctly read number of executions. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def getNumExecutions5(): Unit = {
    this.persistenceUtils.createTestExecution(this.methodSignature, new util.Date, 6.0, 6.0)
    this.persistenceUtils.createTestExecution(this.methodSignature, new util.Date, 6.0, 6.0)
    this.persistenceUtils.createTestExecution(this.methodSignature, new util.Date, 6.0, 6.0)
    this.persistenceUtils.createTestExecution(this.methodSignature, new util.Date, 6.0, 6.0)
    this.persistenceUtils.createTestExecution(this.methodSignature, new util.Date, 6.0, 6.0)

    this.persistenceUtils.getNumExecutions(CoreIdentifier(this.methodSignature)) should be (5)
  }


  /** Checks that we can correctly compute the mode response time. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def mode(): Unit = {
    this.createTestExecution(1)
    this.createTestExecution(2)
    this.createTestExecution(3)
    this.createTestExecution(4)
    this.createTestExecution(5)
    this.createTestExecution(5)
    this.persistenceUtils.getModeResponseTime(CoreIdentifier(this.methodSignature)) should be (5.0)

    this.createTestExecution(4)
    this.createTestExecution(4)
    this.persistenceUtils.getModeResponseTime(CoreIdentifier(this.methodSignature)) should be (4.0)

    this.createTestExecution(3)
    this.createTestExecution(3)
    this.createTestExecution(3)
    this.persistenceUtils.getModeResponseTime(CoreIdentifier(this.methodSignature)) should be (3.0)

    this.createTestExecution(2)
    this.createTestExecution(2)
    this.createTestExecution(2)
    this.createTestExecution(2)
    this.persistenceUtils.getModeResponseTime(CoreIdentifier(this.methodSignature)) should be (2.0)
  }


  /** Checks that we can correctly compute the median response time. */
  @Test
  @Category(Array(classOf[UnitTest]))
  def median(): Unit = {
    this.createTestExecution(1)
    this.createTestExecution(2)
    this.createTestExecution(3)
    this.createTestExecution(4)
    this.createTestExecution(5)
    this.persistenceUtils.getMedianResponseTime(CoreIdentifier(this.methodSignature)) should be (3.0)

    this.createTestExecution(5)
    this.createTestExecution(5)
    this.persistenceUtils.getMedianResponseTime(CoreIdentifier(this.methodSignature)) should be (4.0)

    this.createTestExecution(6)
    this.persistenceUtils.getMedianResponseTime(CoreIdentifier(this.methodSignature)) should be (4.5)
  }
}
