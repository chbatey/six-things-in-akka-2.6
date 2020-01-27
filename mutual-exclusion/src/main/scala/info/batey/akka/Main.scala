package info.batey.akka

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorRef

object Main {

  def main(args: Array[String]): Unit = {

    val sytem = ActorSystem(
      Behaviors.setup[Void] { ctx =>
        val userOne: ActorRef[User.Command] = ctx.spawn(User("user1"), "user1")
        userOne.tell(User.UpdateUser("Christopher", "Member of Akka core team"))
        Behaviors.empty
      },
      "MutualExclusion"
    )
    println("Hello world")
  }
}
