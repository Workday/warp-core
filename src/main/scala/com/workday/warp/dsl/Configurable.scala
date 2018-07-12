package com.workday.warp.dsl

import com.workday.warp.arbiters.traits.ArbiterLike
import com.workday.warp.collectors.abstracts.AbstractMeasurementCollector

/**
  * Enables syntax like `using no arbiters`. See [[ExecutionConfig.no]].
  *
  * Created by tomas.mccandless on 4/29/16.
  */
sealed abstract class Configurable {

  /**
    * Creates an [[ExecutionConfig]] with updated configuration.
    *
    * @param config base execution configuration
    * @return a new updated [[ExecutionConfig]]
    */
  def disable(config: ExecutionConfig): ExecutionConfig
}


@DslApi
case object arbiters extends Configurable {

  /**
    * Creates an [[ExecutionConfig]] with all arbiters disabled.
    *
    * @param config base execution configuration
    * @return a new updated [[ExecutionConfig]]
    */
  override def disable(config: ExecutionConfig): ExecutionConfig = {
    config.copy(disableExistingArbiters = true, additionalArbiters = List.empty[ArbiterLike])
  }
}


@DslApi
case object collectors extends Configurable {

  /**
    * Creates an [[ExecutionConfig]] with all collectors disabled.
    *
    * @param config base execution configuration
    * @return a new updated [[ExecutionConfig]]
    */
  override def disable(config: ExecutionConfig): ExecutionConfig = {
    config.copy(disableExistingCollectors = true, additionalCollectors = List.empty[AbstractMeasurementCollector])
  }
}
