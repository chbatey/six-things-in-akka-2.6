import sbt._

object Dependencies {

  val AkkaVersion = "2.6.1+193-70b042ce+20200124-1103"

  val Akka = "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
  val AkkaDiscovery = "com.typesafe.akka" %% "akka-discovery" % AkkaVersion
  val AkkaPersistence = "com.typesafe.akka" %% "akka-persistence-typed" % AkkaVersion
  val AkkaCluster = "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion
  val AkkaClusterSharding = "com.typesafe.akka" %% "akka-cluster-sharding-typed" % AkkaVersion
  val Logback = "ch.qos.logback" % "logback-classic" % "1.2.3"

  val MutualExclusionDeps = Seq(
    Akka,
    AkkaDiscovery,
    Logback,
  )

  val EventSourcingDeps = Seq(
    Akka,
    AkkaPersistence,
    AkkaDiscovery,
    Logback,
  )

  val WorkDistributionDeps = Seq(
    Akka,
    AkkaCluster,
    AkkaDiscovery,
    Logback,
  )

  val StateDistributionDeps = Seq(
    Akka,
    AkkaCluster,
    AkkaClusterSharding,
    AkkaDiscovery,
    Logback,
  )

  val DistributedProcessingDeps = Seq(
    Akka,
    AkkaCluster,
    AkkaClusterSharding,
    AkkaDiscovery,
    Logback,
  )

}
