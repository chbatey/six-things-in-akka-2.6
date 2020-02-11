package info.batey.akka

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import akka.kafka.ConsumerSettings
import org.apache.kafka.clients.consumer.ConsumerConfig
import akka.actor.typed.Behavior
import akka.kafka.scaladsl.Consumer
import akka.kafka.Subscriptions
import akka.actor.typed.scaladsl.AskPattern._
import akka.cluster.sharding.typed.ShardingEnvelope
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import akka.Done
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Future
import akka.stream._
import akka.http.scaladsl._
import akka.http.scaladsl.model._
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.ActorRef
import info.batey.akka.events.Account.Withdraw
import info.batey.akka.events.Account.Deposit
import info.batey.akka.events.Account

object Main {

  // TODO how do we share the Akka one?
  val objectMapper: ObjectMapper = new ObjectMapper()
  objectMapper.registerModule(DefaultScalaModule)

  sealed trait AccountMessage
  final case class AccountWithdraw(accountId: String, amount: Long) extends AccountMessage
  final case class AccountDeposit(accountId: String, amount: Long) extends AccountMessage

  def main(args: Array[String]): Unit = {

    val guardianBehavior: Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
      implicit val timeout = Timeout(5.seconds)
      implicit val system = ctx.system
      // Start sharded actor, do this on every node
      val accounts = ShardedAccount.init(ctx.system)

      // start a kafka consumer 
      val config = ctx.system.settings.config.getConfig("akka.kafka.consumer")
      val consumerSettings =
        ConsumerSettings(config, new StringDeserializer, new ByteArrayDeserializer)
          .withBootstrapServers("localhost:9042")
          .withGroupId("group1")

      val subscriptions = Subscriptions.topics("accounts")
      // For the ask to cluster sharding

      // TODO: Wrap in a RestartSource to deal with failures
      // TODO: retry messages?
      // TODO: commitable source?
      val processKafkaTopic = Consumer
        .plainSource(consumerSettings, subscriptions)
        .mapAsync(100) { record =>
          // Parse the record with jackson
          val parsedRecord = objectMapper.readValue(record.value, classOf[AccountMessage])

          // distribute the processing via cluster sharding
          accounts.ask[Done] { ackTo =>
            parsedRecord match {
              case AccountWithdraw(accountId, amount) =>
                ShardingEnvelope(accountId, Withdraw(amount, ackTo))
              case AccountDeposit(accountId, amount) =>
                ShardingEnvelope(accountId, Deposit(amount, ackTo))
            }
          }
        }

      Behaviors.empty

    }
    val system = ActorSystem[Nothing](guardianBehavior, "Distributed-Processing")
  }

  def startGrpc(sharding: ActorRef[ShardingEnvelope[Account.Command]], mat: Materializer, frontEndPort: Int)(
      implicit system: ActorSystem[_]
  ): Future[Http.ServerBinding] = {
    val service: HttpRequest => Future[HttpResponse] =
      AccountsServiceHandler(new AccountsServiceImpl(sharding))(mat, system.toClassic)
    Http()(system.toClassic).bindAndHandleAsync(
      service,
      interface = "127.0.0.1",
      port = frontEndPort,
      connectionContext = HttpConnectionContext()
    )(mat)

  }

}
