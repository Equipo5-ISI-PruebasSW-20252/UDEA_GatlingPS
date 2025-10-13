package parabank

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import parabank.Data._

class PayBillLoadTest extends Simulation {

  // 1 Http Conf
  val httpConf = http
    .baseUrl(url)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  // 2 Scenarios Definition
  
  // ESCENARIO 1: Validar carga moderada (100 usuarios)
  val criterio1Scenario = scenario("Criterio 1 - 200 Users Steady")
    .feed(csv("billpay-data.csv").circular) // Feeder desde CSV
    .exec(http("paybill_criterio1")
      .post("/billpay")
      .queryParam("accountId", "${accountId}")
      .queryParam("amount", "${amount}")
      .check(status.in(200, 201))
      .check(jsonPath("$").exists)
    )

  // 3 Load Scenario
  setUp(
    // CRITERIO 1: Carga de la HU
    criterio2Scenario.inject(
      rampUsersPerSec(10).to(200).during(30 seconds),
      constantUsersPerSec(200).during(120 seconds)
    )
  ).protocols(httpConf)
  .assertions(
    // Validaciones finales para ambos criterios
    global.responseTime.percentile(95).lte(3000), 
    global.successfulRequests.percent.gte(99) 
  )
}
