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
class GatlingTestSpec extends WarpSimulation {

  val scn: ScenarioBuilder = scenario("Google test")
    .exec(http("Google")
      .get("https://www.google.comjdah")
      .check(status is 100))

  val pop: PopulationBuilder = scn.inject(rampUsers(1) during FiniteDuration(1, TimeUnit.SECONDS))
  setUp(pop)
 }
