package parabank

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import parabank.Data._

class TransferTest extends Simulation {

  // 1 Http Conf
  val httpConf = http.baseUrl(url)
    .acceptHeader("application/json")
    //Verificar de forma general para todas las solicitudes
    .check(status.is(200))

  // 2 Scenarios Definition
  val transferScenario = scenario("Transferencias Simultáneas")
    .feed(csv("transfersData.csv").circular) // Cargar el feeder desde el archivo CSV
    .exec(http("transferencia")
      .post("/transfer")
      .queryParam("fromAccountId", "${fromAccount}")
      .queryParam("toAccountId", "${toAccount}")
      .queryParam("amount", "${amount}")
      .check(status.is(200))
    )

  // 3 Load Scenario
  setUp(
    transferScenario.inject(
      constantUsersPerSec(150).during(60 seconds) // 150 transacciones por segundo
    )
  ).protocols(httpConf)
  .assertions(
    global.successfulRequests.percent.gte(99) // Al menos 99% de éxito en las transacciones
  )
}
