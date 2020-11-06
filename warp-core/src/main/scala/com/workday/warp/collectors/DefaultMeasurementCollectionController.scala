package com.workday.warp.collectors

import com.workday.warp.TestId
import com.workday.warp.TestIdImplicits._
import com.workday.warp.persistence.{CorePersistenceAware, Tag}
import org.junit.jupiter.api.TestInfo

/**
  * A simple concrete implementation of [[AbstractMeasurementCollectionController]] that uses a [[WallClockTimeCollector]]
  * and a [[HeapUsageCollector]].
  *
  * @param testId fully qualified name of the method being measured.
  * @param tags [[List]] of [[Tag]] that should be persisted during endMeasurementCollection.
  */
// TODO probably dont want default args here
class DefaultMeasurementCollectionController(override val testId: TestId = TestId.empty,
                                             override val tags: List[Tag] = Nil)
  extends AbstractMeasurementCollectionController(testId, tags) with CorePersistenceAware {


  // boilerplate for java interop
  def this(info: TestInfo, tags: List[Tag]) = this(info.testId, tags)
  def this(info: TestInfo) = this(info.testId, Nil)

  this._collectors = List(new WallClockTimeCollector(this.testId), new HeapUsageCollector(this.testId))
}
