package parabank

import scala.util.Random

object Data {
  
  val url = "https://parabank.parasoft.com/parabank/services/bank"
  val username = "usr"
  val password = "pass"
  
  // Cuentas existentes verificadas
  val accountIds = List(14787, 15342, 15453, 15564, 15675)
  
  // Customer IDs reales verificados
  val validCustomerIds = List(13544, 13877, 14099, 14210, 14432)
  
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


