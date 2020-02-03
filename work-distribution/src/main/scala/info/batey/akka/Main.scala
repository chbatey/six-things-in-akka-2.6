package info.batey.akka

import akka.actor.typed.scaladsl._
import akka.actor.typed._
import akka.cluster.typed.Cluster
import scala.io.StdIn
import info.batey.akka.WorkManager.SubmitWork

/**
 * The basic building blocks for distributed processing.
 * A full working example in the akka samples repo: https://github.com/akka/akka-samples
 * 
 */
object Main {

  val NrWorkers = 5

  def main(args: Array[String]): Unit = {

    val rootBehavior = Behaviors.setup[Nothing] { ctx =>
      val manager = ctx.spawn(WorkManager(), "work-manager")
      (0 until NrWorkers) foreach { i =>
        ctx.spawn[Nothing](Worker(), s"worker-$i")
      }

      manager ! SubmitWork(Work("Really important work"))
      manager ! SubmitWork(Work("Super important work"))
      manager ! SubmitWork(Work("Event better work"))
      manager ! SubmitWork(Work("The last piece of work"))

      Behaviors.empty
    }

    val system = ActorSystem[Nothing](rootBehavior, "WorkDistribution")
    StdIn.readLine("press <enter> to terminate")
    system.terminate()
  }
}
