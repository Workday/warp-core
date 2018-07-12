package com.workday.warp.inject.modules

import com.google.inject.{AbstractModule, Provides}
import com.workday.warp.collectors.{AbstractMeasurementCollectionController, DefaultMeasurementCollectionController}
import com.workday.warp.common.{CoreWarpProperty, WarpPropertyLike}
import com.workday.warp.logger.WriterConfig
import com.workday.warp.persistence.Tag
import org.pmw.tinylog.writers.Writer

/**
  * Defines default dependency injection bindings for [[DefaultMeasurementCollectionController]] based on the values
  * provided in the constructor.
  *
  * Created by tomas.mccandless on 11/6/17.
  *
  * @param testId will be passed to [[DefaultMeasurementCollectionController]] constructor.
  * @param tags will be passed to [[DefaultMeasurementCollectionController]] constructor.
  */
class DefaultWarpModule(override val testId: String,
                        override val tags: List[Tag]) extends AbstractModule with HasWarpBindings {

  override def configure(): Unit = { }

  /** @return an [[DefaultMeasurementCollectionController]] with configured testId and tags. */
  @Provides override def getController: AbstractMeasurementCollectionController = new DefaultMeasurementCollectionController(
    this.testId, this.tags
  )

  /** @return a [[WarpPropertyLike]] containing configuration values. */
  @Provides override def getProperty: WarpPropertyLike = CoreWarpProperty

  /** @return a [[Seq]] of additional log [[Writer]] that should be enabled. */
  @Provides override def getExtraWriters: Seq[WriterConfig] = Seq.empty
}
