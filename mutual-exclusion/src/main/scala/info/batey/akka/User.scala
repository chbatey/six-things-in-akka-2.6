package info.batey.akka

import akka.actor.typed._
import akka.actor.typed.scaladsl._

//#actor
object User {

  final case class User(name: String, description: String)

  sealed trait Command
  case class GetUser(replyTo: ActorRef[User]) extends Command
  case class UpdateUser(name: String, description: String) extends Command

  def apply(id: String): Behavior[Command] = {

    def user(u: User): Behavior[Command] = {
      Behaviors.receiveMessage {
        case GetUser(replyTo) =>
          replyTo ! u
          Behaviors.same
        case UpdateUser(name: String, description: String) =>
          user(User(name, description))
      }
    }

    user(User("", ""))
  }
}
//#actor

//#mutable
class User {

  private var name: String = ""
  private var description: String = ""

  def updateUser(name: String, desciption: String): Unit = {
    this.name = name
    this.description = description
  }

}

//#mutable

//#imutable
final case class UserImmutable(name: String, desciption: String) 
//#imutable
