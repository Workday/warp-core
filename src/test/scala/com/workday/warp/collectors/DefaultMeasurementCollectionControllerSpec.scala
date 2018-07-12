package com.workday.warp.collectors

import java.util.Date

import com.workday.warp.TrialResult
import com.workday.warp.arbiters.SmartNumberArbiter
import com.workday.warp.collectors.abstracts.AbstractMeasurementCollector
import com.workday.warp.common.category.UnitTest
import com.workday.warp.common.spec.WarpJUnitSpec
import com.workday.warp.common.utils.Implicits._
import com.workday.warp.persistence.Tables._
import com.workday.warp.persistence.TablesLike.RowTypeClasses._
import org.junit.{Before, Test}
import org.junit.experimental.categories.Category
import slick.jdbc.MySQLProfile.api._
import com.workday.warp.persistence.{TablesLike, Tag, _}


/**
  * Spec for [[DefaultMeasurementCollectionController]]
  *
  * Created by justin.teo on 12/14/17.
  */

class DefaultMeasurementCollectionControllerSpec extends WarpJUnitSpec with CorePersistenceAware {

  /** run a query for an execution metatag using the rowID from the trial tag and key */
  private[this] def queryTestExecutionMetaTagWithRowId(rowID: Int, key: String): Seq[(String, String)] = {
    this.persistenceUtils.synchronously(
      this.persistenceUtils.testExecutionMetaTagQuery(rowID,
        this.persistenceUtils.synchronously(this.persistenceUtils.readTagNameQuery(key)).head.idTagName)
    )
  }

  /** run a query for a definition metatag using the rowID from the definition tag and key */
  private[this] def queryTestDefinitionMetaTagWithRowId(rowID: Int, key: String): Seq[(String, String)] = {
    this.persistenceUtils.synchronously(
      this.persistenceUtils.testDefinitionMetaTagQuery(rowID,
        this.persistenceUtils.synchronously(this.persistenceUtils.readTagNameQuery(key)).head.idTagName)
    )
  }

  /** clear database before running all the persistenceUtil functions */
  @Before
  def clearDatabase(): Unit = {
    CorePersistenceUtils.dropSchema()
    Connection.refresh()
    // make sure we don't throw an exception when the schema already exists
    CorePersistenceUtils.initSchema()
    Connection.refresh()
    CorePersistenceUtils.initSchema()
  }

  /** Test various insertion behavior of tags with and without metatags for both Execution and Definition type tags */
  @Test
  @Category(Array(classOf[UnitTest]))
  def testRecordTags(): Unit = {
    val newTags: List[Tag] = List(
      // insert two execution metatags for an execution outer tag
      ExecutionTag("key1", "val1", List(
        ExecutionMetaTag("metaKey11", "metaVal11"),
        ExecutionMetaTag("metaKey12", "metaVal12"),
        // duplicate key, value: ignored success
        ExecutionMetaTag("metaKey12", "metaVal12"),
        // duplicate key, different value: exception
        ExecutionMetaTag("metaKey12", "metaVal13")
      )),

      // now test definition tag with one definition metatag
      DefinitionTag("key2", "val2", List(
        DefinitionMetaTag("metaKey21", "metaVal21"),
        // duplicate key, value: ignored success
        DefinitionMetaTag("metaKey21", "metaVal21"),
        // duplicate key, different value: exception
        DefinitionMetaTag("metaKey21", "metaVal22")
      )),

      // execution tag with no meta tags
      ExecutionTag("key3", "val3"),

      // Ignored duplicate/erroneous execution and definition tags
      ExecutionTag("key3", "val3"),
      // these meta tags should fail to insert
      ExecutionTag("key3", "val31", List(
        ExecutionMetaTag("metaKey31", "metaVal31"),
        ExecutionMetaTag("metaKey32", "metaVal32")
      )),
      DefinitionTag("key2", "val2"),
      DefinitionTag("key2", "val21")
    )

    val controller : DefaultMeasurementCollectionController = new DefaultMeasurementCollectionController(tags = newTags)
    controller.isIntrusive should be (false)
    controller.measurementInProgress should be (false)
    val tryRecordTags: List[PersistTagResult] =
              controller.recordTags(controller.tags, this.persistenceUtils.createTestExecution("1.2.3.4.5", new Date, 5, 10))

    val outerTestExecutionTagLength: Int = this.persistenceUtils.synchronously(TestExecutionTag.length.result)
    val outerTestDefinitionTagLength: Int = this.persistenceUtils.synchronously(TestDefinitionTag.length.result)
    val testExecutionMetaTagLength: Int = this.persistenceUtils.synchronously(TestExecutionMetaTag.length.result)
    val testDefinitionMetaTagLength: Int = this.persistenceUtils.synchronously(TestDefinitionMetaTag.length.result)
    val tagDescriptionLength: Int = this.persistenceUtils.synchronously(TagName.length.result)

    // test adding a total of 2 TestExecutionTags, 2 TestExecutionMetaTags, 1 DefinitionTag, 1 TestDefinitionMetaTag, 6 Tag Descriptions.
    // erroneous/redundant MetaTags and OuterTags should be ignored
    outerTestExecutionTagLength should be (2)
    testExecutionMetaTagLength should be (2)
    outerTestDefinitionTagLength should be (1)
    testDefinitionMetaTagLength should be (1)
    tagDescriptionLength should be (6)


    // test that using the rowID for the OuterTag to search for the MetaTag produces the correct key/value result
    // execution tags
    val trialTag1RowID: Int = this.persistenceUtils.synchronously(
      TestExecutionTag.filter{_.idTestExecutionTag === 1}
    ).head.idTestExecutionTag
    queryTestExecutionMetaTagWithRowId(trialTag1RowID, "metaKey11").head.value should be (("metaKey11", "metaVal11"))
    queryTestExecutionMetaTagWithRowId(trialTag1RowID, "metaKey12").head.value should be (("metaKey12", "metaVal12"))

    // should be no metatag persisted associated to this OuterTag
    val trialTag2RowID: Int = this.persistenceUtils.synchronously(
      TestExecutionTag.filter{_.idTestExecutionTag === 2}
    ).head.idTestExecutionTag
    this.persistenceUtils.synchronously(TestExecutionMetaTag.filter(_.idTestExecutionTag === trialTag2RowID))
                         .headOption should be (None)

    // definition tags
    val definitionTag1RowId: Int = this.persistenceUtils.synchronously(TestDefinitionTag.filter{_.idTestDefinitionTag === 1})
      .head.idTestDefinitionTag
    queryTestDefinitionMetaTagWithRowId(definitionTag1RowId, "metaKey21").head.value should be (("metaKey21", "metaVal21"))


    // test that erroneous description/key tag insertion throws exception
    // erroneous Tags
    tryRecordTags(4).tryTag._1.isFailure should be (true)
    tryRecordTags(6).tryTag._1.isFailure should be (true)

    // ExecutionMetaTag
    tryRecordTags(0).tryTag._2(2).tryMetaTag.isSuccess should be (true)
    tryRecordTags(0).tryTag._2(3).tryMetaTag.isFailure should be (true)

    // DefinitionMetaTag
    tryRecordTags(1).tryTag._2(1).tryMetaTag.isSuccess should be (true)
    tryRecordTags(1).tryTag._2(2).tryMetaTag.isFailure should be (true)

    // erroneous Tag preventing MetaTag insertion
    tryRecordTags(4).tryTag._2(0).tryMetaTag.isFailure should be (true)
    tryRecordTags(4).tryTag._2(1).tryMetaTag.isFailure should be (true)
  }


  /**
    * Checks various ways of ending measurement.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def endMeasurement(): Unit = {
    val controller: AbstractMeasurementCollectionController = new DefaultMeasurementCollectionController()

    // shouldn't get any results if measurement is not already in progress
    controller.endMeasurementCollection() should be (TrialResult.empty)

    controller.beginMeasurementCollection()
    val result: TrialResult[_] = controller.endMeasurementCollection(2 seconds, 3 seconds)
    result.maybeResponseTime should be (Some(2 seconds))
    result.maybeThreshold should be (Some(3 seconds))

    controller.beginMeasurementCollection()
    val resultOptionThreshold: TrialResult[_] = controller.endMeasurementCollection(2 seconds, Option(3 seconds))
    resultOptionThreshold.maybeResponseTime should be (Some(2 seconds))
    resultOptionThreshold.maybeThreshold should be (Some(3 seconds))

    controller.beginMeasurementCollection()
    val resultNegativeDuration: TrialResult[_] = controller.endMeasurementCollection(-1 seconds)
    resultNegativeDuration.maybeResponseTime should be (Some(1 milli))
  }


  /**
    * Checks disabling arbiters.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def disableArbiters(): Unit = {
    val controller: AbstractMeasurementCollectionController = new DefaultMeasurementCollectionController()
    controller.registerArbiter(new SmartNumberArbiter())
    controller.disableArbiters()
    controller.arbiters.foreach { arbiter => arbiter.isEnabled should be (false) }
  }

  /**
    * Checks disabling intrusive collectors.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def disableIntrusiveCollectors(): Unit = {
    val controller: AbstractMeasurementCollectionController = new DefaultMeasurementCollectionController()

    controller.registerCollector(new AbstractMeasurementCollector {
      override val isIntrusive: Boolean = true

      override def startMeasurement(): Unit = { }

      override def stopMeasurement[T: TablesLike.TestExecutionRowLikeType](maybeTestExecution: Option[T]): Unit = { }

    })

    controller.disableIntrusiveCollectors()
    controller.intrusiveCollectors.foreach { collector => collector.isEnabled should be (false) }
  }


  /**
    * Checks that we correctly handle exceptions thrown by collectors.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def exceptionsHandled(): Unit = {
    val controller: AbstractMeasurementCollectionController = new DefaultMeasurementCollectionController()

    // use a collector that throws an exception during start
    controller.registerCollector(new AbstractMeasurementCollector {
      override def startMeasurement(): Unit = { throw new RuntimeException("error starting measurement") }

      override def stopMeasurement[T: TablesLike.TestExecutionRowLikeType](maybeTestExecution: Option[T]): Unit = { }
    })

    controller.beginMeasurementCollection()
    controller.endMeasurementCollection()

    controller.disableCollectors()
    // use a collector that throws an exception during stop
    controller.registerCollector(new AbstractMeasurementCollector {
      override def startMeasurement(): Unit = { }

      override def stopMeasurement[T: TablesLike.TestExecutionRowLikeType](maybeTestExecution: Option[T]): Unit = {
        throw new RuntimeException("error stopping measurement")

      }
    })

    controller.beginMeasurementCollection()
    controller.endMeasurementCollection()
  }


  /**
    * Checks registering collectors and arbiters when a measurement is in progress.
    */
  @Test
  @Category(Array(classOf[UnitTest]))
  def register(): Unit = {
    val controller : DefaultMeasurementCollectionController = new DefaultMeasurementCollectionController()
    controller.beginMeasurementCollection()

    // registration calls should not be successful when there is already a measurement in progress.
    controller.registerCollector(new ResponseTimeCollector(controller.testId)) should be (false)
    controller.registerArbiter(new SmartNumberArbiter) should be (false)

    controller.endMeasurementCollection()
  }
}
