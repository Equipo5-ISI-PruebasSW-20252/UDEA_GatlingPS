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
  val criterio1Scenario = scenario("Criterio 1 - 100 Users Steady")
    .feed(payBillFeeder)
    .exec(http("paybill_criterio1")
      .post("/billpay?accountId=${accountId}&amount=${amount}")
      .body(StringBody("""{
        "name": "${payeeName}",
        "address": {
          "street": "${street}",
          "city": "${city}",
          "state": "${state}",
          "zipCode": "${zipCode}"
        },
        "phoneNumber": "${phoneNumber}",
        "accountNumber": ${accountId}
      }""")).asJson
      .check(status.in(200, 201))
      .check(jsonPath("$").exists)
      .check(responseTimeInMillis.lte(2000))
    )

  // ESCENARIO 2: Validar criterio real con 200 usuarios concurrentes
  val criterio2Scenario = scenario("Criterio 2 - 200 Users Peak")
    .feed(payBillFeeder)
    .exec(http("paybill_criterio2")
      .post("/billpay?accountId=${accountId}&amount=${amount}")
      .body(StringBody("""{
        "name": "${payeeName}",
        "address": {
          "street": "${street}",
          "city": "${city}",
          "state": "${state}",
          "zipCode": "${zipCode}"
        },
        "phoneNumber": "${phoneNumber}",
        "accountNumber": ${accountId}
      }""")).asJson
      .check(status.in(200, 201))
      .check(jsonPath("$").exists)
      .check(responseTimeInMillis.lte(3000))
    )

  // 3 Load Scenario
  setUp(
    // CRITERIO 1: 0s a 120s - Carga base
    criterio1Scenario.inject(
      constantUsersPerSec(100).during(120 seconds)
    ),
    
    // CRITERIO 2: Carga de la HU
    criterio2Scenario.inject(
      nothingFor(140 seconds),
      rampUsersPerSec(10).to(200).during(30 seconds),
      constantUsersPerSec(200).during(120 seconds)
    )
  ).protocols(httpConf)
  .assertions(
    // Validaciones finales para ambos criterios
    details("paybill_criterio1").responseTime.percentile(95).lte(2000),
    details("paybill_criterio1").successfulRequests.percent.gte(99),
    
    details("paybill_criterio2").responseTime.percentile(95).lte(3000),
    details("paybill_criterio2").successfulRequests.percent.gte(99)
  )
}