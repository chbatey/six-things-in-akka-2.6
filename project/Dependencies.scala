import sbt._

object Dependencies {

  val AkkaVersion = "2.6.1+194-f74acc4d"
  val AlpakkaKakfaVersion = "2.0.1"

  val Akka = "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
  val AkkaJackson = "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion
  val AkkaDiscovery = "com.typesafe.akka" %% "akka-discovery" % AkkaVersion
  val AkkaPersistence = "com.typesafe.akka" %% "akka-persistence-typed" % AkkaVersion
  val AkkaCluster = "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion
  val AkkaClusterSharding = "com.typesafe.akka" %% "akka-cluster-sharding-typed" % AkkaVersion
  val AlpakaKafka = "com.typesafe.akka" %% "akka-stream-kafka" % AlpakkaKakfaVersion
  val Logback = "ch.qos.logback" % "logback-classic" % "1.2.3"

  val MutualExclusionDeps = Seq(
    Akka,
    AkkaDiscovery,
    Logback,
  )

  val EventSourcingDeps = Seq(
    Akka,
    AkkaJackson,
    AkkaPersistence,
    AkkaDiscovery,
    Logback,
  )

  val WorkDistributionDeps = Seq(
    Akka,
    AkkaJackson,
    AkkaCluster,
    AkkaDiscovery,
    Logback,
  )

  val StateDistributionDeps = Seq(
    Akka,
    AkkaJackson,
    AkkaCluster,
    AkkaClusterSharding,
    AkkaDiscovery,
    Logback,
  )

  val DistributedProcessingDeps = Seq(
    Akka,
    AkkaJackson,
    AkkaCluster,
    AkkaClusterSharding,
    AkkaDiscovery,
    AlpakaKafka,
    Logback,
  )

}
