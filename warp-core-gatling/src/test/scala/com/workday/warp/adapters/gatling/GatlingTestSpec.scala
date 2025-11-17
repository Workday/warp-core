package com.workday.warp.adapters.gatling

import io.gatling.core.Predef._
import io.gatling.core.structure.{PopulationBuilder, ScenarioBuilder}
import io.gatling.http.Predef._

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

/**
  * Created by tomas.mccandless on 7/1/20.
  */
class GatlingTestSpec extends WarpSimulation {

  // note that a test must implement its own assertions.
  // even though the below url does not exist, the resulting unresolvable host exception does not fail the build.
  val scn: ScenarioBuilder = scenario("Google test")
    .exec(http("Google")
      .get("https://www.google.comjdah")
      .check(status is 200))

  val pop: PopulationBuilder = scn.inject(rampUsers(1) during FiniteDuration(1, TimeUnit.SECONDS))
  setUp(pop)
 }
