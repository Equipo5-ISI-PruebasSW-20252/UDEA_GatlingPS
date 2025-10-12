package parabank

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import parabank.Data._

class TransactionsLoadTest extends Simulation {

  // 1 Http Conf
  val httpConf = http
    .baseUrl(url)
    .acceptHeader("application/json")
    .check(status.is(200))

  // 2 Scenarios Definition
  
  // ESCENARIO 1: Validar carga moderada (100 usuarios)
  val criterio1Scenario = scenario("Criterio 1 - 100 Users Steady")
    .feed(accountFeeder)
    .exec(http("transactions_criterio1")
      .get("/accounts/${accountId}/transactions")
      .check(status.is(200))
      .check(jsonPath("$").exists) // Valida que retorne un array (aunque esté vacío)
      .check(responseTimeInMillis.lte(2000)) // Criterio: ≤2s
    )

  // ESCENARIO 2: Validar criterio real con 200 usuarios simultáneos
  val criterio2Scenario = scenario("Criterio 2 - 200 Users Peak")
    .feed(accountFeeder)
    .exec(http("transactions_criterio2")
      .get("/accounts/${accountId}/transactions")
      .check(status.is(200))
      .check(jsonPath("$[0].id").exists)
      .check(jsonPath("$[0].accountId").exists)
      .check(jsonPath("$[0].type").in("Debit", "Credit"))
      .check(jsonPath("$[0].amount").exists)
      .check(jsonPath("$[0].date").exists)
      .check(responseTimeInMillis.lte(3000)) // Criterio HU: ≤3s
    )

  // 3 Load Scenario
  setUp(
    // CRITERIO 1: 0s a 120s - Carga base
    criterio1Scenario.inject(
      constantUsersPerSec(100).during(120 seconds)
    ),
    
    criterio2Scenario.inject(
      nothingFor(140 seconds),                        // Time out necesario para evitar el solapamiento de pruebas
      rampUsersPerSec(10).to(200).during(30 seconds), // Rampa gradual
      constantUsersPerSec(200).during(120 seconds)    // 200 usuarios simultáneos por 2 minutos
    )
  ).protocols(httpConf)
  .assertions(
    // Validaciones finales para ambos criterios
    
    // Criterio 1: Carga base, ≤2s
    details("transactions_criterio1").responseTime.percentile(95).lte(2000),
    details("transactions_criterio1").successfulRequests.percent.gte(99),
    
    // Criterio 2: HU3 - 200 usuarios simultáneos, ≤3s, error ≤1%
    details("transactions_criterio2").responseTime.percentile(95).lte(3000),
    details("transactions_criterio2").successfulRequests.percent.gte(99) // Tasa de error ≤1%
  )
}
