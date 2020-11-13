package com.workday.warp.controllers

import com.workday.warp.TestId
import com.workday.warp.TestIdImplicits._
import com.workday.warp.collectors.{HeapUsageCollector, WallClockTimeCollector}
import com.workday.warp.persistence.{CorePersistenceAware, Tag}
import org.junit.jupiter.api.TestInfo

/**
  * A simple concrete implementation of [[AbstractMeasurementCollectionController]] that uses a [[WallClockTimeCollector]]
  * and a [[HeapUsageCollector]].
  *
  * @param testId fully qualified name of the method being measured.
  * @param tags [[List]] of [[Tag]] that should be persisted during endMeasurementCollection.
  */
class DefaultMeasurementCollectionController(override val testId: TestId,
                                             override val tags: List[Tag])
  extends AbstractMeasurementCollectionController(testId, tags) with CorePersistenceAware {


  // boilerplate for java interop
  def this(info: TestInfo, tags: List[Tag]) = this(info.id, tags)
  def this(info: TestInfo) = this(info.id, Nil)

  this._collectors = List(new WallClockTimeCollector, new HeapUsageCollector)
}
