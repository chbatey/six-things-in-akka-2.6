package info.batey.akka

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.actor.typed.ActorRef
import info.batey.akka.Account.Deposit

object Main {
  def main(args: Array[String]): Unit = {
    val rootBehavior = Behaviors.setup[Nothing] { ctx =>

      val shardedActor: ActorRef[ShardingEnvelope[Account.Command]] = ShardedAccount.init(ctx.system)

      shardedActor ! ShardingEnvelope("chbatey-accout", Deposit(100))
      shardedActor ! ShardingEnvelope("chbatey-accout", Deposit(200))

      Behaviors.empty[Nothing]
    }
    val system = ActorSystem[Nothing](rootBehavior, "StateDistribution")
  }
}
