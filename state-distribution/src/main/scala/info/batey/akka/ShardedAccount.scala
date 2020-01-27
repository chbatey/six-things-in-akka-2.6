package info.batey.akka

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.Entity
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.actor.typed.ActorRef
import akka.cluster.sharding.typed.ShardingEnvelope

object ShardedAccount {

  val TypeKey = EntityTypeKey[Account.Command]("Account")

  def init(system: ActorSystem[_]): ActorRef[ShardingEnvelope[Account.Command]]= {
    val entity = Entity(TypeKey) { entityContext =>
      Account(entityContext.entityId)
    }
    ClusterSharding(system).init(entity)
  }

}

