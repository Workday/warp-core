package com.workday.warp.collectors

/**
  * Spawns a new thread that continuously invokes the measure() method at a configurable interval.
  *
  * Useful for collecting periodic measurements throughout the duration of a test.
  *
  * Extend this class for collectors written in Java. Collectors written in scala can mix in [[ContinuousMeasurement]].
  *
  * Created by tomas.mccandless on 8/25/15.
  */
abstract class ContinuousMeasurementCollector extends AbstractMeasurementCollector with ContinuousMeasurement
