package info.batey.akka

import info.batey.akka._
import scala.concurrent.Future
import akka.actor.typed.ActorRef
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.ActorSystem
import akka.util.Timeout
import scala.concurrent.duration._
import info.batey.akka.events.Account
import info.batey.akka.events.Account.GetBalance

class AccountsServiceImpl(accounts: ActorRef[ShardingEnvelope[Account.Command]])(implicit val system: ActorSystem[_])
    extends AccountsService {

  implicit val timeout = Timeout(5.seconds)
  import system.executionContext

  override def getAccountBalance(in: GetAccountBalanceRequest): Future[GetAccountBalanceReply] = {
    accounts
      .ask[Long](balance => ShardingEnvelope(in.accountId, GetBalance(balance)))
      .map { balance =>
        GetAccountBalanceReply(balance)
      }

  }
}
