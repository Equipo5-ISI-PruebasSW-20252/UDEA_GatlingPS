package parabank

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import parabank.Data._

class BillPaymentLoadTest extends Simulation {

  // 1 Http Conf
  val httpConf = http
    .baseUrl(url)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .check(status.in(200, 201))

  // 2 Scenario Definition
  
  // Escenario: 200 usuarios concurrentes realizando pagos
  val billPaymentScenario = scenario("HU5 - Pago de Servicios Alta Concurrencia")
    .feed(billPaymentFeeder)
    .exec(http("bill_payment")
      .post("/billpay?accountId=${accountId}&amount=${amount}")
      .body(StringBody(
        """{
          "name": "${payeeName}",
          "address": {
            "street": "${street}",
            "city": "${city}",
            "state": "${state}",
            "zipCode": "${zipCode}"
          },
          "phoneNumber": "${phoneNumber}",
          "accountNumber": "${payeeAccountNumber}"
        }"""
      )).asJson
      .check(status.in(200, 201))
      .check(jsonPath("$").exists)
      .check(responseTimeInMillis.lte(3000))
    )
    .pause(1, 3) // Pausa entre pagos para simular comportamiento real

  // 3 Load Scenario
  setUp(
    billPaymentScenario.inject(
      // Rampa progresiva hasta 200 usuarios
      rampUsersPerSec(10).to(200).during(30 seconds),
      // Mantiene 200 usuarios concurrentes por 2 minutos
      constantUsersPerSec(200).during(120 seconds)
    )
  ).protocols(httpConf)
  .assertions(
    // Criterio: Tiempo de respuesta ≤ 3 segundos
    global.responseTime.max.lte(3000),
    global.responseTime.mean.lte(2000),
    global.responseTime.percentile(95).lte(3000),
    
    // Criterio: Tasa de errores ≤ 1%
    global.successfulRequests.percent.gte(99),
    
    // Validación adicional: No duplicaciones
    global.failedRequests.count.lte(20) // Máximo 1% de fallos en ~2000 requests
  )
}
