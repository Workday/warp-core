package com.workday.warp.junit

import java.util.concurrent.TimeUnit

import com.workday.warp.adapters.gatling.{GatlingJUnitRunner, WarpSimulation}
import io.gatling.core.Predef._
import io.gatling.core.structure.{PopulationBuilder, ScenarioBuilder}
import io.gatling.http.Predef._
import org.junit.runner.RunWith

import scala.concurrent.duration.FiniteDuration

/**
  * Created by tomas.mccandless on 7/1/20.
  */
@RunWith(classOf[GatlingJUnitRunner])
@deprecated("use junit5", since = "4.4.0")
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
