package info.batey.akka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.Cluster
import akka.actor.typed.receptionist.Receptionist
import info.batey.akka.WorkManager._
import akka.actor.typed.internal.delivery.ConsumerController
import akka.actor.typed.internal.delivery.ConsumerController.Confirmed

object Worker {

  private final case class DeliverWork(job: ConsumerController.Delivery[WorkerCommand])

  def apply(): Behavior[Nothing] = Behaviors.setup[DeliverWork] { ctx =>
    // Start a consumer controller for receiving WorkerCommands
    val consumerController = ctx.spawn(ConsumerController[WorkerCommand](true), "worker-cc")

    // Transform the WorkerCommands into this Actor's internal protocol
    val deliveryAdapter = ctx.messageAdapter[ConsumerController.Delivery[WorkerCommand]](DeliverWork)

    // Start consuming WorkerCommands via reliable delivery 
    consumerController ! ConsumerController.Start(deliveryAdapter)

    // FIXME, this is going away and registration automatic
    // Register with the cluster receptionish so that a work manager producer controller can find
    // the worker
    ctx.system.receptionist ! Receptionist.Register(WorkManagerServiceKey, consumerController)

    Behaviors.receiveMessage {
      case DeliverWork(job) =>
        ctx.log.info("Doing some hard work {} nr {}", job.msg, job.seqNr)

        // This can happen asynchronously. Reliable delivery will keep track of unconfired messages
        // and retry after a timeout
        job.confirmTo ! Confirmed(job.seqNr)
        Behaviors.same
    }
  }.narrow[Nothing]
}

