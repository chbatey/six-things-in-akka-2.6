package info.batey.akka

import akka.actor.typed.Behavior
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.PersistenceId
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import org.slf4j.LoggerFactory

object Account {

  val log = LoggerFactory.getLogger("Account")

  sealed trait Command
  final case class Withdraw(amount: Long) extends Command
  final case class Deposit(amount: Long) extends Command
  final case class GetBalance(replyTo: ActorRef[Long]) extends Command

  sealed trait Event
  case class Withdrawn(amount: Long) extends Event  
  case class Deposited(amount: Long) extends Event 

  final case class State(balance: Long) {
    def applyEvent(event: Event): State = event match {
      case Withdrawn(amount) => copy(balance - amount) 
      case Deposited(amount) => copy(balance + amount)
    }
  }

  def commandHandler(state: State, command: Command): Effect[Event, State] = {
    command match {
      case Deposit(amount) =>
        log.info("Deposit {}", amount)
        Effect.persist(Deposited(amount))
      case Withdraw(amount) =>
        log.info("Withdraw {}", amount)
        // TODO validate balance!!
        Effect.persist(Withdrawn(amount))
      case GetBalance(replyTo) =>
        log.info("Get Balance {}", state.balance)
        replyTo ! state.balance
        Effect.none
    }
  }

  /**
   * An event sourced bank account
   * 
   * Snapshotting?
   * Replying after persisting?
   * Enforced reples
   */
  def apply(accountId: String): Behavior[Command] = {

    EventSourcedBehavior[Command, Event, State](
      PersistenceId.ofUniqueId(accountId),
      State(0L),
      commandHandler = commandHandler,
      eventHandler = (state, event) => state.applyEvent(event)
    )
  }

}
