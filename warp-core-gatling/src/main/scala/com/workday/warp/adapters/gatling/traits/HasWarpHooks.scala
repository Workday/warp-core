package com.workday.warp.adapters.gatling.traits

import com.workday.warp.collectors.AbstractMeasurementCollectionController

/**
  * Created by ruiqi.wang
  */
trait HasWarpHooks {

  // TODO: Think about any additional dsl we can provide here (maybe generalize this to all adapters and not just gatling?

  implicit val controller: AbstractMeasurementCollectionController

  /**
    * Should execute before calling startMeasurement()
    */
  def beforeStart(): Unit = {}

  /**
    * Should execute after calling startMeasurement()
    */
  def afterStart(): Unit = {}

  /**
    * Should execute before calling endMeasurement()
    */
  def beforeEnd(): Unit = {}

  /**
    * Should execute after calling endMeasurement()
    */
  def afterEnd(): Unit = {}

}
