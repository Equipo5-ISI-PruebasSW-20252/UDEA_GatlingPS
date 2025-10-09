package parabank

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._ 
import parabank.Data._

class LoginTest extends Simulation{

  // 1 Http Conf
  val httpConf = http.baseUrl(url)
    .acceptHeader("application/json")
    //Verificar de forma general para todas las solicitudes
    .check(status.is(200))

  // 2 Scenarios Definition

  // ESCENARIO 1: Validar criterio de 100 usuarios concurrentes
  val criterio1Scenario = scenario("Criterio 1 - 100 Users Steady")
    .exec(http("login_criterio1")
      .get(s"/login/$username/$password")
      .check(status.is(200))
      .check(responseTimeInMillis.lte(2000)) // Criterio: ≤2s
    )

    // ESCENARIO 2: Validar criterio de 200 usuarios pico
  val criterio2Scenario = scenario("Criterio 2 - 200 Users Peak")
    .exec(http("login_criterio2")
      .get(s"/login/$username/$password")
      .check(status.is(200))
      .check(responseTimeInMillis.lte(5000)) // Criterio: ≤5s
    )

  // 3 Load Scenario
  setUp(
    // CRITERIO 1: 0s a 120s
    criterio1Scenario.inject(
      constantUsersPerSec(100).during(120 seconds)
    ),
    
    // CRITERIO 2: SEPARADO TEMPORALMENTE
    criterio2Scenario.inject(
      nothingFor(140 seconds),                        // Time out necesario para evitar el solapamiento de pruebas
      rampUsersPerSec(10).to(200).during(45 seconds), // Rampa gradual
      constantUsersPerSec(200).during(60 seconds),
    )
  ).protocols(httpConf)
  .assertions(
    //Validaciones finales para ambos criterios

    // Criterio 1: 100 usuarios concurrentes, ≤2s
    details("login_criterio1").responseTime.percentile(95).lte(2000),
    details("login_criterio1").successfulRequests.percent.gte(99),
    
    // Criterio 2: 200 usuarios pico, ≤5s
    details("login_criterio2").responseTime.percentile(95).lte(5000),
    details("login_criterio2").successfulRequests.percent.gte(95),
  )

}
