package parabank

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import parabank.Data._

class LoanLoadTest extends Simulation {

  // 1 Http Conf
  val httpConf = http
    .baseUrl(url)
    .acceptHeader("application/json")
    .check(status.in(200, 201))

  // 2 Scenarios Definition
  
  // ESCENARIO 1: Validar carga moderada (50 usuarios)
  val criterio1Scenario = scenario("Criterio 1 - 50 Users Steady")
    .feed(loanFeeder)
    .exec(http("loan_criterio1")
      .post("/requestLoan?customerId=${customerId}&amount=${loanAmount}&downPayment=${downPayment}&fromAccountId=${fromAccountId}")
      .check(status.in(200, 201))
      .check(jsonPath("$").exists)
      .check(responseTimeInMillis.lte(3000))
    )

  // ESCENARIO 2: Validar criterio real con 150 usuarios concurrentes
  val criterio2Scenario = scenario("Criterio 2 - 150 Users Peak")
    .feed(loanFeeder)
    .exec(http("loan_criterio2")
      .post("/requestLoan?customerId=${customerId}&amount=${loanAmount}&downPayment=${downPayment}&fromAccountId=${fromAccountId}")
      .check(status.in(200, 201))
      .check(jsonPath("$").exists)
      .check(responseTimeInMillis.lte(5000))
    )

  // 3 Load Scenario
  setUp(
    // CRITERIO 1: 0s a 120s - Carga base
    criterio1Scenario.inject(
      constantUsersPerSec(50).during(120 seconds)
    ),
    
    // CRITERIO 2: Carga de la HU
    criterio2Scenario.inject(
      nothingFor(140 seconds),
      rampUsersPerSec(10).to(150).during(30 seconds),
      constantUsersPerSec(150).during(120 seconds)
    )
  ).protocols(httpConf)
  .assertions(
    // Validaciones finales para ambos criterios
    details("loan_criterio1").responseTime.percentile(95).lte(3000),
    details("loan_criterio1").successfulRequests.percent.gte(99),
    
    details("loan_criterio2").responseTime.mean.lte(5000),
    details("loan_criterio2").responseTime.percentile(95).lte(6000),
    details("loan_criterio2").successfulRequests.percent.gte(98)
  )
}