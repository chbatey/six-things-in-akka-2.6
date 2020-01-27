package info.batey.akka

import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.internal.delivery.ConsumerController
import akka.actor.typed.internal.delivery.WorkPullingProducerController
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.internal.delivery.WorkPullingProducerController.RequestNext
import akka.actor.typed.scaladsl.StashBuffer

/**
  * Basic building blocks of a distributed work manager using reliabale delivery.
  *
  * Current Main runs it all in the same ActorSystem but no changes are required to
  * run this in a cluster.
  *
  * Extensions:
  *  - Split work in SubmitWork into chunks?
  */
object WorkManager {
  sealed trait Command
  final case class SubmitWork(work: Work) extends Command
  private final case class RequestNextWrapper(next: RequestNext[WorkerCommand]) extends Command

  // Protocol for workers to implement
  sealed trait WorkerCommand
  final case class DoWork(work: Work) extends WorkerCommand

  // Service for discovering workers
  val WorkManagerServiceKey = ServiceKey[ConsumerController.Command[WorkerCommand]]("WorkManager")

  def apply(): Behavior[Command] = Behaviors.setup { ctx =>
    val producerController = ctx.spawn(
      WorkPullingProducerController[WorkerCommand]("work-manager", WorkManagerServiceKey, None),
      "producer-controller"
    )
    val requestNextAdapter = ctx.messageAdapter(RequestNextWrapper)
    producerController ! WorkPullingProducerController.Start(requestNextAdapter)

    Behaviors.withStash(1000) { stash =>
      def idle(demand: RequestNext[WorkerCommand]): Behavior[Command] = Behaviors.receiveMessage[Command] {
        case SubmitWork(work) =>
          ctx.log.info("Executing work {} to {}", work, demand)
          demand.sendNextTo ! DoWork(work)
          ctx.log.info("switching to awaiting demand")
          awaitingDemand()
        case RequestNextWrapper(next) =>
          throw new IllegalStateException(s"Received request next when idle. Old ${demand} new ${next}")
      }

      def awaitingDemand(): Behavior[Command] = Behaviors.receiveMessage[Command] {
        case sw: SubmitWork =>
          ctx.log.info("SubmitWork with no worker demand: {}", sw)
          stash.stash(sw)
          Behaviors.same
        case RequestNextWrapper(next) =>
          ctx.log.info("Demand received. Unstashing work if any. {}", next)
          ctx.log.info("switching to idle")
          stash.unstash(idle(next), 1, identity)
      }

      awaitingDemand()
    }
  }
}
