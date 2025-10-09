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
      .check(
        jsonPath("$.loanId").exists
          .or(jsonPath("$.id").exists)
          .or(jsonPath("$.loan_id").exists)
      )
      .check(responseTimeInMillis.lte(3000)) // Criterio: ≤3s
    )

  // ESCENARIO 2: Validar criterio real con 150 usuarios concurrentes
  val criterio2Scenario = scenario("Criterio 2 - 150 Users Peak")
    .feed(loanFeeder)
    .exec(http("loan_criterio2")
      .post("/requestLoan?customerId=${customerId}&amount=${loanAmount}&downPayment=${downPayment}&fromAccountId=${fromAccountId}")
      .check(status.in(200, 201))
      .check(jsonPath("$").exists)
      .check(
        jsonPath("$.loanId").exists
          .or(jsonPath("$.id").exists)
          .or(jsonPath("$.loan_id").exists)
      )
      .check(
        jsonPath("$.accountId").exists
          .or(jsonPath("$.account_id").exists)
      )
      .check(
        jsonPath("$.amount").exists
          .or(jsonPath("$.loanAmount").exists)
      )
      .check(
        jsonPath("$.approved").is("true")
          .or(jsonPath("$.isApproved").is("true"))
      )
      .check(responseTimeInMillis.lte(5000)) // Criterio HU: ≤5s
    )

  // 3 Load Scenario
  setUp(
    // CRITERIO 1: 0s a 120s - Carga base
    criterio1Scenario.inject(
      constantUsersPerSec(50).during(120 seconds)
    ),
    
    // CRITERIO 2: SEPARADO TEMPORALMENTE - Carga real de la HU
    criterio2Scenario.inject(
      nothingFor(140 seconds),                        // Time out necesario para evitar el solapamiento de pruebas
      rampUsersPerSec(10).to(150).during(30 seconds), // Rampa gradual
      constantUsersPerSec(150).during(120 seconds)    // 150 usuarios concurrentes por 2 minutos
    )
  ).protocols(httpConf)
  .assertions(
    // Validaciones finales para ambos criterios
    
    // Criterio 1: Carga base, ≤3s
    details("loan_criterio1").responseTime.percentile(95).lte(3000),
    details("loan_criterio1").successfulRequests.percent.gte(99),
    
    // Criterio 2: HU4 - 150 usuarios concurrentes, promedio ≤5s, éxito ≥98%
    details("loan_criterio2").responseTime.mean.lte(5000),          // Tiempo promedio
    details("loan_criterio2").responseTime.percentile(95).lte(6000), // P95 con margen
    details("loan_criterio2").successfulRequests.percent.gte(98)     // Tasa de éxito ≥98%
  )
}
