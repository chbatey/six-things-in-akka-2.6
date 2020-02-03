package info.batey.akka

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.ActorRef
import info.batey.akka.mutual._
import scala.io.StdIn
import scala.concurrent.duration._
import akka.util.Timeout
import scala.concurrent.Future
import akka.Done
import info.batey.akka.mutual.Account.WithdrawlResponse

object Main {

  implicit val timeout = Timeout(5.seconds)

  def main(args: Array[String]): Unit = {

    val system = ActorSystem(
      Behaviors.setup[Void] { ctx =>
        implicit val system = ctx.system
        val accountOne: ActorRef[Account.Command] = ctx.spawn(Account(), "chbatey-account")
        val depositOneAck: Future[Done] = accountOne.ask(replyTo => Account.Deposit(20, replyTo))

        // Only one of these should succeed
        val withdrawAck1: Future[WithdrawlResponse] = accountOne.ask(replyTo => Account.Withdraw(11, replyTo))
        val withdrawAck2: Future[WithdrawlResponse] = accountOne.ask(replyTo => Account.Withdraw(11, replyTo))

        Behaviors.empty
      },
      "MutualExclusion"
    )
    println("Hello world")
    StdIn.readLine()
    system.terminate()
  }
}
