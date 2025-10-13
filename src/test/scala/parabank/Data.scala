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
  
  // Datos para pagos de servicios
  val payeeNames = List("Electricidad SA", "Agua Potable", "Gas Natural", "Telefonía", "Internet Corp")
  val cities = List("Medellín", "Bogotá", "Cali", "Barranquilla", "Cartagena")
  val states = List("Antioquia", "Cundinamarca", "Valle", "Atlántico", "Bolívar")
  val streets = List("Calle 10 #20-30", "Carrera 50 #40-20", "Avenida 80 #30-10", "Diagonal 75 #25-40")
  
   // Feeder para pago de servicios (usado en HU5 - PayBill)
  val payBillFeeder = Iterator.continually(Map(
    "accountId" -> accountIds(Random.nextInt(accountIds.length)),
    "amount" -> (50 + Random.nextInt(1000)),
    "payeeName" -> s"Payee ${Random.nextInt(1000)}",
    "street" -> s"Street ${Random.nextInt(100)}",
    "city" -> s"City ${Random.nextInt(50)}",
    "state" -> s"State ${Random.nextInt(50)}",
    "zipCode" -> (10000 + Random.nextInt(90000)).toString,
    "phoneNumber" -> s"555-${Random.nextInt(10000)}"
    
  ))
}


