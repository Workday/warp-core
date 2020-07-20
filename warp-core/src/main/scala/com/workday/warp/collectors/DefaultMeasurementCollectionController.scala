package com.workday.warp.collectors

import com.workday.warp.persistence.{CorePersistenceAware, Tag}

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

  this._collectors = List(new WallClockTimeCollector(this.testId), new HeapUsageCollector(this.testId))
}
