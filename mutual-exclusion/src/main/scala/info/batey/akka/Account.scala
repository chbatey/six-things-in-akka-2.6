package info.batey.akka.mutual

import akka.actor.typed._
import akka.actor.typed.scaladsl._
import akka.Done


object Account {

  sealed trait Command
  final case class Withdraw(amount: Long, ack: ActorRef[WithdrawlResponse]) extends Command
  final case class Deposit(amount: Long, ack: ActorRef[Done]) extends Command
  final case class GetBalance(replyTo: ActorRef[Long]) extends Command

  sealed trait WithdrawlResponse
  case object Ack extends WithdrawlResponse
  case object InsufficientFunds extends WithdrawlResponse

  def apply(balance: Long = 0L): Behavior[Command] = {
    Behaviors.receiveMessage {
      case Withdraw(amount, ack) =>
        // Two concurrent withdrawls?
        if (balance > amount) {
          ack ! Ack 
          Account(balance - amount)
        } else {
          ack ! InsufficientFunds
          Behaviors.same
        }
      case Deposit(amount, ack) =>
        ack ! Done
        Account(balance + amount)
      case GetBalance(replyTo) =>
        replyTo ! balance
        Behaviors.same
    }
  }

}
