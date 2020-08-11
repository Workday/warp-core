package com.workday.warp.dsl

/**
  * Enables syntax like `using only these collectors`. The only implementing instance should be [[these]].
  *
  * Created by tomas.mccandless on 5/17/16.
  */
sealed abstract class TheseWord

/** Part of the dsl. Allows syntax like `using only these collectors { ... }` or `using only these arbiters { ... }`. */
@DslApi
case object these extends TheseWord
