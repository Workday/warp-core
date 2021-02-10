---
title: "Measuring Gatling Simulations"
date: 2021-02-08T10:57:50-07:00
draft: true
weight: 72
---

"warp-core-gatling" is published as a separate jar and includes a base class "WarpSimulation" that add warp measurements to existing simulations
as before/after gatling hooks. This module also includes a (now deprecated) vintage JUnit4 runner that allows for easily executing gatling simulations along with
the rest of your JUnit test suite.

{{< highlight scala "linenos=" >}}
@RunWith(classOf[GatlingJUnitRunner])
class BasicSimulation extends WarpSimulation {
  val httpConf: HttpProtocolBuilder = http
    .baseUrl("http://google.com")

  val scn: ScenarioBuilder = scenario("Positive Scenario")
    .exec(
      http("request_1").get("/")
    )

  setUp(scn.inject(atOnceUsers(1)).protocols(httpConf))
}
{{< /highlight >}}