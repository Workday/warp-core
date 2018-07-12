package com.workday.warp.dsl

import com.workday.warp.arbiters.traits.ArbiterLike
import com.workday.warp.collectors.abstracts.AbstractMeasurementCollector

/**
  * Holds the instance on which `only these` was invoked. The next chained function call should be `collectors` or
  * `arbiters`, which return a copy of the wrapped [[ExecutionConfig]] appropriately reconfigured.
  *
  * Created by tomas.mccandless on 5/17/16.
  */
class ResultOfOnlyThese(val config: ExecutionConfig) {

  /**
    * Part of the dsl. Enables syntax like `using only these collectors { ... }`
    *
    * @param collectors code block returning a collection of [[AbstractMeasurementCollector]].
    * @return a copy of the wrapped [[ExecutionConfig]] with existing collectors disabled and additional collectors set.
    */
  @DslApi
  def collectors(collectors: => Iterable[AbstractMeasurementCollector]): ExecutionConfig = {
    this.config.copy(disableExistingCollectors = true, additionalCollectors = collectors.toList)
  }


  /**
    * Part of the dsl. Enables syntax like `using only these arbiters { ... }`
    *
    * @param arbiters code block returning a collection of [[ArbiterLike]].
    * @return a copy of the wrapped [[ExecutionConfig]] with existing arbiters disabled and additional arbiters set.
    */
  @DslApi
  def arbiters(arbiters: => Iterable[ArbiterLike]): ExecutionConfig = {
    this.config.copy(disableExistingArbiters = true, additionalArbiters = arbiters.toList)
  }
}
