package com.workday.warp.inject.modules

import com.google.inject.Provides
import com.workday.warp.TestId
import com.workday.warp.config.WarpPropertyLike
import com.workday.warp.controllers.AbstractMeasurementCollectionController
import com.workday.warp.logger.WriterConfig
import com.workday.warp.persistence.Tag

/**
  * This trait should be mixed in and overridden with values that can be passed to create instances of
  * [[AbstractMeasurementCollectionController]]
  *
  * Additional bindings should be provided for [[WarpPropertyLike]].
  *
  * Created by tomas.mccandless on 11/7/17.
  */
trait HasWarpBindings {

  val testId: TestId
  // TODO consider making this a Seq
  val tags: List[Tag]

  /** @return a [[AbstractMeasurementCollectionController]]. Subclasses should override this to provide their own bindings. */
  @Provides def getController: AbstractMeasurementCollectionController

  /** @return a [[WarpPropertyLike]]. Subclasses should override this to provide their own bindings. */
  @Provides def getProperty: WarpPropertyLike

  /** @return a [[Seq]] of additional log [[Writer]] that should be enabled. */
  @Provides def getExtraWriters: Seq[WriterConfig]
}
