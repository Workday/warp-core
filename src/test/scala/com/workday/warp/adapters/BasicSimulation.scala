package com.workday.warp.adapters

import com.workday.warp.adapters.gatling.{GatlingJUnitRunner, WarpSimulation}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import org.junit.runner.RunWith

@RunWith(classOf[GatlingJUnitRunner])
class BasicSimulation extends WarpSimulation {
  val httpConf = http
    .baseURL("http://google.com")

  val scn = scenario("Positive Scenario")
    .exec(
      http("request_1").get("/")
    )

  setUp(scn.inject(atOnceUsers(1)).protocols(httpConf))
}
