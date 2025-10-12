package parabank

import scala.util.Random

object Data {
  val url = "https://parabank.parasoft.com/parabank/services/bank"
  val username = "john"
  val password = "demo"
  
  val accountIds = List(19671, 19782, 19893, 20004, 20115)
  
  val accountFeeder = Iterator.continually(Map(
    "accountId" -> accountIds(Random.nextInt(accountIds.length))
  ))
  
  val loanFeeder = Iterator.continually(Map(
    "customerId" -> (12000 + Random.nextInt(500)).toString,
    "loanAmount" -> (1000 + Random.nextInt(50000)),
    "downPayment" -> (100 + Random.nextInt(5000)),
    "fromAccountId" -> accountIds(Random.nextInt(accountIds.length))
  ))
}
