package info.batey.akka.events

import akka.actor.typed.Behavior
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.PersistenceId
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.Effect
import org.slf4j.LoggerFactory
import akka.Done

object Account {

  val log = LoggerFactory.getLogger("Account")

  // The external commands e.g. coming in over HTTP, gRPC
  sealed trait Command
  final case class Withdraw(amount: Long, ack: ActorRef[Done]) extends Command
  final case class Deposit(amount: Long, ack: ActorRef[Done]) extends Command
  final case class GetBalance(replyTo: ActorRef[Long]) extends Command

  // The events to store
  sealed trait Event
  case class Withdrawn(amount: Long) extends Event  
  case class Deposited(amount: Long) extends Event 

  final case class State(balance: Long) {
    // State x Event => State
    def applyEvent(event: Event): State = event match {
      case Withdrawn(amount) => copy(balance - amount) 
      case Deposited(amount) => copy(balance + amount)
    }
  }

  def commandHandler(state: State, command: Command): Effect[Event, State] = {
    command match {
      case Deposit(amount, ackTo) =>
        log.info("Deposit {}", amount)
        Effect.persist(Deposited(amount)).thenRun { _ =>
          // Only ack once the event is persisted
          // What happens if another command arrives before persisting?
          ackTo ! Done
        }
      case Withdraw(amount, ackTo) =>
        log.info("Withdraw {}", amount)
        // TODO validate balance!!
        Effect.persist(Withdrawn(amount)).thenRun { _ =>
          ackTo ! Done
        }
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
   * Enforced replies
   */
  def apply(accountId: String): Behavior[Command] = {
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId(accountId),
      emptyState = State(0L),
      commandHandler = commandHandler,
      // State x Event => State
      eventHandler = (state, event) => state.applyEvent(event)
    )
  }

}
