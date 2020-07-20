package com.workday.warp.junit

import com.workday.warp.adapters.gatling.WarpSimulation
import io.gatling.app.Gatling
import io.gatling.core.Predef._
import io.gatling.core.config.{GatlingConfiguration, GatlingPropertiesBuilder}
import io.gatling.core.structure.PopulationBuilder
import org.pmw.tinylog.Logger

import scala.util.{Failure, Success, Try}

/**
  * Created by tomas.mccandless on 7/1/20.
  */
trait GatlingTestExtensionLike extends WarpSimulation {


  @UnitTest
  def run(): Unit = {
    Logger.info("running gatling")
    Try {
      val properties: GatlingPropertiesBuilder = new GatlingPropertiesBuilder
      properties.simulationClass(this.getClass.getCanonicalName)
//      properties.noReports()
      properties.resultsDirectory("build/")
      Gatling.fromMap(properties.build)
    } match {
      case Success(0) =>
        Logger.debug("Test succeeded!")
      case Success(nonZero) =>
        throw new RuntimeException(s"Simulation ended with result code $nonZero")
      case Failure(e) =>
        Logger.error(e)
        throw e
    }
  }

  def addAssertions(pop: PopulationBuilder)(implicit gatling: GatlingConfiguration): SetUp = {
    setUp(List(pop)).assertions(global(gatling).successfulRequests.percent.is(100))
  }
}


class GatlingTestExtension extends GatlingTestExtensionLike
