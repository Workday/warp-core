package com.workday.warp.dsl

/**
  * Specifies a mode for measurement collection.
  *
  * Only applies to tests running under an [[ExecutionConfig]] with `threads > 1`. A multithreaded test running in
  * `single` mode will have an outer [[com.workday.warp.collectors.AbstractMeasurementCollectionController]] to collect measurements,
  * but all invocations running on separate threads will not be measured.
  *
  * A multithreaded test running in `multi` mode will similarly have an outer controller, but each individual invocation
  * will also have its own controller. Controllers for individual invocations will have their intrusive measurements disabled.
  *
  * Created by tomas.mccandless on 12/20/16.
  */
sealed abstract class ModeWord

/** Part of the dsl. Allows syntax like `using mode single measure { ... }`. */
@DslApi
case object single extends ModeWord

/** Part of the dsl. Allows syntax like `using mode multi measure { ... }` */
@DslApi
case object multi extends ModeWord
