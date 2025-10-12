package parabank

import scala.util.Random

object Data {
  val url = "https://parabank.parasoft.com/parabank/services/bank"
  val username = "john"
  val password = "demo"
  
  val accountIds = List(19671, 19782, 19893, 20004, 20115)
  
  // HU3
  val accountFeeder = Iterator.continually(Map(
    "accountId" -> accountIds(Random.nextInt(accountIds.length))
  ))
  }
