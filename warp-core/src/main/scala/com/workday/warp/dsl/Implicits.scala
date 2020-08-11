package com.workday.warp.dsl

import com.workday.warp.arbiters.traits.ArbiterLike
import com.workday.warp.collectors.abstracts.AbstractMeasurementCollector
import com.workday.warp.persistence.Tag

/**
  * Created by tomas.mccandless on 5/6/16.
  */
object Implicits {

  /** @return `arbiter` wrapped in a [[List]] */
  implicit def arbiter2ListArbiter(arbiter: ArbiterLike): List[ArbiterLike] = List(arbiter)


  /** @return `collector` wrapped in a [[List]] */
  implicit def collector2ListCollector(collector: AbstractMeasurementCollector): List[AbstractMeasurementCollector] = List(collector)

  /** @return single `tag` wrapped in a [[List]] */
  implicit def tag2TagList(tag: Tag): List[Tag] = List[Tag](tag)
}
