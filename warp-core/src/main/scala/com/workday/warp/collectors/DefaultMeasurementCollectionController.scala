package com.workday.warp.collectors

import com.workday.warp.junit.HasTestId
import com.workday.warp.junit.TestIdConverters._
import com.workday.warp.persistence.{CorePersistenceAware, Tag}
import org.junit.jupiter.api.TestInfo

/**
  * A simple concrete implementation of [[AbstractMeasurementCollectionController]] that uses a [[WallClockTimeCollector]]
  * and a [[HeapUsageCollector]].
  *
  * @param testId fully qualified name of the method being measured.
  * @param tags [[List]] of [[Tag]] that should be persisted during endMeasurementCollection.
  */
class DefaultMeasurementCollectionController(override val testId: String = Defaults.testId,
                                             override val tags: List[Tag] = Defaults.tags)
  extends AbstractMeasurementCollectionController(testId, tags) with CorePersistenceAware {


  // boilerplate for java interop
  def this(info: TestInfo, tags: List[Tag]) = this(info.testId, tags)
  def this(info: TestInfo) = this(info.testId)
  def this(hasTestId: HasTestId, tags: List[Tag]) = this(hasTestId.testId, tags)
  def this(hasTestId: HasTestId) = this(hasTestId.testId)

  this._collectors = List(new WallClockTimeCollector(this.testId), new HeapUsageCollector(this.testId))
}
