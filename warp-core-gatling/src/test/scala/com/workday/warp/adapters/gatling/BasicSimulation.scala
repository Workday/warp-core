package com.workday.warp.adapters.gatling

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder

class BasicSimulation extends WarpSimulation {

  override val maybeDocumentation: Option[String] = Option("an example gatling simulation")

  val httpConf: HttpProtocolBuilder = http
    .baseUrl("http://google.com")

  val scn: ScenarioBuilder = scenario("Positive Scenario")
    .exec(
      http("request_1").get("/")
    )

  setUp(scn.inject(atOnceUsers(1)).protocols(httpConf))
}
