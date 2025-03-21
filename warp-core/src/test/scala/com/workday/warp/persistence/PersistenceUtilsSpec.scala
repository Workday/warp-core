package com.workday.warp.persistence

import java.time.{Instant, LocalDate}

import slick.jdbc.MySQLProfile.api._
import com.workday.warp.junit.{UnitTest, WarpJUnitSpec}
import com.workday.warp.persistence.Tables._
import com.workday.warp.persistence.TablesLike._
import com.workday.warp.persistence.TablesLike.RowTypeClasses._
import com.workday.warp.persistence.Tables.RowTypeClasses._
import com.workday.warp.persistence.CoreIdentifierType._
import com.workday.warp.TestIdImplicits.string2TestId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.parallel.Isolated


/**
  * Created by tomas.mccandless on 10/5/16.
  */
@Isolated
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
    this.persistenceUtils.createTestExecution(this.methodSignature, Instant.now(), responseTime, threshold)
  }


  /** Truncates the schema. */
  @BeforeEach
  def truncateSchema(): Unit = {
    Connection.refresh()
    CorePersistenceUtils.dropSchema()
    CorePersistenceUtils.initSchema()
    Connection.refresh()
  }


  /** Checks that we can find or create [[Tables.Build]]. */
  @UnitTest
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
  @UnitTest
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
  @UnitTest
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
  @UnitTest
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
  @UnitTest
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


  /** Checks that we can record a [[BuildTag]], and that it properly returns the whole row including autoInc rowID */
  @UnitTest
  def recordBuildTag(): Unit = {
    val build: BuildRowLike = this.persistenceUtils.findOrCreateBuild(2077, 1, 345)
    val before: Int = this.persistenceUtils.synchronously(Tables.BuildTag.length.result)
    val buildTagRow: BuildTagRowLike = this.persistenceUtils.recordBuildTag(
      build.idBuild, "some metadata name", "some metadata tag value", isUserGenerated = true
    )
    val after: Int = this.persistenceUtils.synchronously(Tables.BuildTag.length.result)

    after should be (before + 1)

    // test that the entire row is returned properly
    buildTagRow.idBuildTag should not be 0
    buildTagRow.idBuild should be (build.idBuild)
    buildTagRow.idTagName should not be 0
    buildTagRow.value should be ("some metadata tag value")
  }


  /** Checks that we can record a [[BuildMetaTag]], and that it properly returns the whole row including autoInc rowID */
  @UnitTest
  def recordBuildMetaTag(): Unit = {
    val build: BuildRowLike = this.persistenceUtils.findOrCreateBuild(2078, 1, 345)
    val before: Int = this.persistenceUtils.synchronously(Tables.BuildMetaTag.length.result)
    val buildTagRow: BuildTagRowLike = this.persistenceUtils.recordBuildTag(
      build.idBuild, "some metadata name", "some metadata tag value", isUserGenerated = true
    )
    val buildMetaTagRow: BuildMetaTagRowLike = this.persistenceUtils.recordBuildMetaTag(
      buildTagRow.idBuildTag, "some metadata metatag name", "some metadata metatag value", isUserGenerated = true
    )
    val after: Int = this.persistenceUtils.synchronously(Tables.BuildMetaTag.length.result)

    after should be (before + 1)

    // test that the entire row is returned properly
    buildMetaTagRow.idBuildTag should not be 0
    buildMetaTagRow.idTagName should not be 0
    buildMetaTagRow.value should be ("some metadata metatag value")
  }

  /** Checks that we can record a [[TestDefinitionTagRow]], and that it properly returns the whole row including the autoInc rowID */
  @UnitTest
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
  @UnitTest
  def recordTestDefinitionMetaTag(): Unit = {
    val testExecution: TestExecutionRow = this.createTestExecution(1)
    this.persistenceUtils.recordTestDefinitionTag(testExecution.idTestDefinition, "instanceId", "755$1234")

    val before: Int = this.persistenceUtils.synchronously(Tables.TestDefinitionMetaTag.length.result)
    this.persistenceUtils.recordTestDefinitionMetaTag(1, "Key", "Value")
    val after: Int = this.persistenceUtils.synchronously(Tables.TestDefinitionMetaTag.length.result)

    after should be (before + 1)
  }

  /** Checks that we can record a [[TestDefinitionMetaTagRow]]. */
  @UnitTest
  def recordTestExecutionMetaTag(): Unit = {
    val testExecution: TestExecutionRow = this.createTestExecution(1)
    this.persistenceUtils.recordTestExecutionTag(testExecution.idTestExecution, "some name", "some value", isUserGenerated = true)

    val before: Int = this.persistenceUtils.synchronously(Tables.TestExecutionMetaTag.length.result)
    this.persistenceUtils.recordTestExecutionMetaTag(1, "Key", "Value")
    val after: Int = this.persistenceUtils.synchronously(Tables.TestExecutionMetaTag.length.result)

    after should be (before + 1)
  }


  /** Checks that we can create [[Tables.TestExecution]]. */
  @UnitTest
  def createTestExecution(): Unit = {
    // insert a test execution, should have id 1
    val testExecution: Tables.TestExecutionRow = this.persistenceUtils.createTestExecution(this.methodSignature, Instant.now(), 5.0, 6.0)
    testExecution.idTestExecution should be (1)

    // insert a few more, with different signatures, and check that we can find all the ones for a ce
    this.persistenceUtils.createTestExecution(this.methodSignature, Instant.now(), 5.0, 6.0).idTestExecution should be (2)
    this.persistenceUtils.createTestExecution(this.methodSignature, Instant.now(), 5.0, 6.0).idTestExecution should be (3)
    this.persistenceUtils.createTestExecution(this.methodSignature, Instant.now(), 5.0, 6.0)
    this.persistenceUtils.createTestExecution(this.anotherMethodSignature, Instant.now(), 5.0, 6.0)
    this.persistenceUtils.createTestExecution(this.anotherMethodSignature, Instant.now(), 5.0, 6.0)

    this.persistenceUtils.synchronously(this.persistenceUtils.readTestExecutionQuery(this.methodSignature)) should have length 4
    this.persistenceUtils.synchronously(this.persistenceUtils.readTestExecutionQuery(this.anotherMethodSignature)) should have length 2

    // there should be 6 total test executions
    this.persistenceUtils.synchronously(this.persistenceUtils.numTestExecutionsQuery) should be (6)

    val error: IllegalArgumentException = intercept[IllegalArgumentException] {
      this.persistenceUtils.createTestExecution(this.methodSignature, Instant.now(), 0.0, 1.0)
    }
    error.getMessage should be ("Zero Time recorded for this measurement, check your adapter implementation.")
  }


  @UnitTest
  def recordMeasurement(): Unit = {
    val testExecution: Tables.TestExecutionRow = this.persistenceUtils.createTestExecution(this.methodSignature, Instant.now(), 5.0, 6.0)
    this.persistenceUtils.recordMeasurement(testExecution.idTestExecution, "some measurement name", 0.1)

    this.persistenceUtils.synchronously(
      this.persistenceUtils.measurementQuery(
        CoreIdentifier(this.methodSignature, testExecution.idTestDefinition),
        -1
      )
    ).headOption should not be empty
  }


  @UnitTest
  def getResponseTimes(): Unit = {

    // insert a few test executions with different signatures
    this.persistenceUtils.createTestExecution(this.methodSignature, Instant.now(), 5.0, 6.0)
    this.persistenceUtils.createTestExecution(this.methodSignature, Instant.now(), 5.0, 6.0)
    this.persistenceUtils.createTestExecution(this.methodSignature, Instant.now(), 5.0, 6.0)
    this.persistenceUtils.createTestExecution(this.anotherMethodSignature, Instant.now(), 5.5, 6.0)
    this.persistenceUtils.createTestExecution(this.anotherMethodSignature, Instant.now(), 5.5, 6.0)

    // check that we can read back the expected lists of response time
    this.persistenceUtils.getResponseTimes(CoreIdentifier(this.methodSignature)) should be (List(5.0, 5.0, 5.0))
    this.persistenceUtils.getResponseTimes(CoreIdentifier(this.anotherMethodSignature)) should be (List(5.5, 5.5))

    // insert another test execution and check that we can read back its response time
    this.persistenceUtils.createTestExecution(this.methodSignature, Instant.now(), 5.1, 6.0)
    this.persistenceUtils.getResponseTimes(CoreIdentifier(this.methodSignature)) should be (List(5.0, 5.0, 5.0, 5.1))

    // check that we can exclude certain testcase id
    val excludedId: Int = 6
    this.persistenceUtils.getResponseTimes(CoreIdentifier(this.methodSignature), excludedId) should be (List(5.0, 5.0, 5.0))

    // check that we can exclude cases before startDateCutoff in addition to excluding certain testcase id
    val startDateLowerBound: LocalDate = LocalDate.now().minusWeeks(1)
    val someDateBeforeCutoff: Instant = Instant.EPOCH
    val someDateAfterCutoff: Instant = Instant.now()

    this.persistenceUtils.createTestExecution(this.methodSignature, someDateBeforeCutoff, 4.9, 6.0)
    this.persistenceUtils.createTestExecution(this.methodSignature, someDateAfterCutoff, 5.2, 6.0)
    this.persistenceUtils.getResponseTimes(CoreIdentifier(this.methodSignature),
                                           excludedId,
                                           startDateLowerBound) should be (List(5.0, 5.0, 5.0, 5.2))

  }


  /** Checks that we can compute average response times. */
  @UnitTest
  def average(): Unit = {
    // check average of empty list
    this.persistenceUtils.getAverageResponseTime(CoreIdentifier(this.methodSignature)).isNaN should be (true)

    this.persistenceUtils.createTestExecution(this.methodSignature, Instant.now(), 5.0, 6.0)
    this.persistenceUtils.createTestExecution(this.methodSignature, Instant.now(), 6.0, 6.0)

    this.persistenceUtils.getAverageResponseTime(CoreIdentifier(this.methodSignature)) should be (5.5 +- 0.01)
  }


  /** Checks that we can correctly read number of executions. */
  @UnitTest
  def getNumExecutions0(): Unit = {
    this.persistenceUtils.getNumExecutions(CoreIdentifier(this.methodSignature)) should be (0)
  }


  /** Checks that we can correctly read number of executions. */
  @UnitTest
  def getNumExecutions2(): Unit = {
    this.persistenceUtils.createTestExecution(this.methodSignature, Instant.now(), 6.0, 6.0)
    this.persistenceUtils.createTestExecution(this.methodSignature, Instant.now(), 6.0, 6.0)
    this.persistenceUtils.getNumExecutions(CoreIdentifier(this.methodSignature)) should be (2)
  }


  /** Checks that we can correctly read number of executions. */
  @UnitTest
  def getNumExecutions5(): Unit = {
    this.persistenceUtils.createTestExecution(this.methodSignature, Instant.now(), 6.0, 6.0)
    this.persistenceUtils.createTestExecution(this.methodSignature, Instant.now(), 6.0, 6.0)
    this.persistenceUtils.createTestExecution(this.methodSignature, Instant.now(), 6.0, 6.0)
    this.persistenceUtils.createTestExecution(this.methodSignature, Instant.now(), 6.0, 6.0)
    this.persistenceUtils.createTestExecution(this.methodSignature, Instant.now(), 6.0, 6.0)

    this.persistenceUtils.getNumExecutions(CoreIdentifier(this.methodSignature)) should be (5)
  }


  /** Checks that we can correctly compute the mode response time. */
  @UnitTest
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
  @UnitTest
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


  @UnitTest
  def overwriteTestExecutionTag(): Unit = {
    val testExecution: TestExecutionRowLike = this.createTestExecution(1)
    this.persistenceUtils.recordTestExecutionTag(
      testExecution.idTestExecution, "some name", "old tag value"
    )
    val newTag = this.persistenceUtils.recordTestExecutionTag(
      testExecution.idTestExecution, "some name", "new tag value"
    )

    val readBackTag: Option[(String, String)] = this.persistenceUtils.synchronously(
      this.persistenceUtils.testExecutionTagsQuery(testExecution.idTestExecution, newTag.idTagName)
    ).headOption

    readBackTag.get._2 should be ("new tag value")
  }


  @UnitTest
  def overwriteTestDefinitionTag(): Unit = {
    val testDefinition: TestDefinitionRowLike = this.persistenceUtils.findOrCreateTestDefinition(this.methodSignature)
    this.persistenceUtils.recordTestDefinitionTag(
      testDefinition.idTestDefinition, "some name", "old tag value"
    )
    val newTag = this.persistenceUtils.recordTestDefinitionTag(
      testDefinition.idTestDefinition, "some name", "new tag value"
    )

    val readBackTag: Option[(String, String)] = this.persistenceUtils.synchronously(
      this.persistenceUtils.testDefinitionTagsQuery(testDefinition.idTestDefinition, newTag.idTagName)
    ).headOption

    readBackTag.get._2 should be ("new tag value")
  }


  @UnitTest
  def overwriteTestDefinitionMetaTag(): Unit = {
    val testExecution: TestExecutionRow = this.createTestExecution(1)
    this.persistenceUtils.recordTestDefinitionTag(testExecution.idTestDefinition, "instanceId", "755$1234")

    // Write a TestDefinitionMetaTag, there should be one more tag than there was before
    val before: Int = this.persistenceUtils.synchronously(Tables.TestDefinitionMetaTag.length.result)
    this.persistenceUtils.recordTestDefinitionMetaTag(1, "Key", "Value")
    val afterOne: Int = this.persistenceUtils.synchronously(Tables.TestDefinitionMetaTag.length.result)

    afterOne should be (before + 1)

    // Write another with a different value (but same key!). There should still only be one, updated with a new value
    this.persistenceUtils.recordTestDefinitionMetaTag(1, "Key", "New value")
    val afterTwo: Int = this.persistenceUtils.synchronously(Tables.TestDefinitionMetaTag.length.result)

    val keyId: Int = this.persistenceUtils.findOrCreateTagName("Key").idTagName
    val updatedTag: (String, String) = this.persistenceUtils.synchronously(
      this.persistenceUtils.testDefinitionMetaTagQuery(1, keyId)
    ).headOption.get

    afterTwo should be (before + 1)
    updatedTag._2 should be ("New value")
  }


  @UnitTest
  def overwriteTestExecutionMetaTag(): Unit = {
    val testExecution: TestExecutionRow = this.createTestExecution(1)
    this.persistenceUtils.recordTestExecutionTag(testExecution.idTestExecution, "some name", "some value")

    val before: Int = this.persistenceUtils.synchronously(Tables.TestExecutionMetaTag.length.result)
    this.persistenceUtils.recordTestExecutionMetaTag(1, "Key", "Value")
    val after: Int = this.persistenceUtils.synchronously(Tables.TestExecutionMetaTag.length.result)
    after should be (before + 1)

    // There should still only be one TestExecutionMetaTag after writing with a new value
    this.persistenceUtils.recordTestExecutionMetaTag(1, "Key", "New value")
    val afterTwo: Int = this.persistenceUtils.synchronously(Tables.TestExecutionMetaTag.length.result)

    val keyId: Int = this.persistenceUtils.findOrCreateTagName("Key").idTagName
    val updatedTag: (String, String) = this.persistenceUtils.synchronously(
      this.persistenceUtils.testExecutionMetaTagQuery(1, keyId)
    ).headOption.get

    afterTwo should be (before + 1)
    updatedTag._2 should be ("New value")
  }
}
