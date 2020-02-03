package info.batey.akka.traditional

/**
 * Without event sourcing this data model would be replicated in the
 * database
 */
class Account(val accountId: String, val balance: Long) {
  def depoit(amount: Long): Account = new Account(accountId, balance + amount)
  def withdraw(amount: Long): Account = new Account(accountId, balance - amount)
}

