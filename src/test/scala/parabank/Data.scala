package parabank

import scala.util.Random

object Data {
  
  val url = "https://parabank.parasoft.com/parabank/services/bank"
  val username = "john"
  val password = "demo"
  
  // Cuentas existentes verificadas
  val accountIds = List(19671, 19782, 19893, 20004, 20115)
  
  // Customer IDs reales verificados
  val validCustomerIds = List(18206, 18317, 18428, 18539, 18650)
  
  val accountFeeder = Iterator.continually(Map(
    "accountId" -> accountIds(Random.nextInt(accountIds.length))
  ))
  
  // Feeder para solicitudes de préstamos con datos reales
  val loanFeeder = Iterator.continually(Map(
    "customerId" -> validCustomerIds(Random.nextInt(validCustomerIds.length)).toString,
    "loanAmount" -> (1000 + Random.nextInt(50000)),
    "downPayment" -> (100 + Random.nextInt(5000)),
    "fromAccountId" -> accountIds(Random.nextInt(accountIds.length))
  ))
  
  
}


