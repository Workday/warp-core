package com.workday.warp.dsl

/**
  * Enables syntax like `using only defaults measuring { ... }`. The only implementing instance should be [[defaults]].
  *
  * Created by tomas.mccandless on 6/13/16.
  */
sealed abstract class DefaultsWord


/**
  * Part of the dsl. Allows syntax like `using only defaults measuring { ... }`
  */
@DslApi
case object defaults extends DefaultsWord
