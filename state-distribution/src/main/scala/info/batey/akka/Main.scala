package info.batey.akka

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.actor.typed.ActorRef
import akka.Done
import info.batey.akka.events.Account
import info.batey.akka.events.Account.Deposit

object Main {
  def main(args: Array[String]): Unit = {
    val rootBehavior = Behaviors.setup[Done] { ctx =>

      val shardedActor: ActorRef[ShardingEnvelope[Account.Command]] = ShardedAccount.init(ctx.system)

      // Will be routed to the node hosting the chbatey-account
      shardedActor ! ShardingEnvelope("chbatey-account", Account.Deposit(100, ctx.self))
      shardedActor ! ShardingEnvelope("chbatey-account", Account.Deposit(200, ctx.self))

      // Automatically routed to the correct node and started if need be
      shardedActor ! ShardingEnvelope("fred-account", Account.Deposit(200, ctx.self))

      Behaviors.receiveMessage[Done] { _ =>
        ctx.log.info("Done")
        Behaviors.same
      }
    }
    val system = ActorSystem(rootBehavior, "State-Distribution")
  }
}
