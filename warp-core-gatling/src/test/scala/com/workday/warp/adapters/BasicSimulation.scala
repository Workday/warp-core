package com.workday.warp.adapters

import com.workday.warp.adapters.gatling.{GatlingJUnitRunner, WarpSimulation}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.junit.runner.RunWith
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.protocol.HttpProtocolBuilder

@RunWith(classOf[GatlingJUnitRunner])
@deprecated("use junit5", since = "4.4.0")
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
