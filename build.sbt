import Dependencies._

name := "six-things-in-akka-2.6"

ThisBuild / version := "1.0"
ThisBuild / scalaVersion := "2.13.1"

lazy val root = (project in file("."))
                   .aggregate(mutualExclusion, eventSourcing, workDistribution, stateDistribution, distributedProcessing)

lazy val mutualExclusion = (project in file("mutual-exclusion"))
  .settings(
    libraryDependencies ++= MutualExclusionDeps
  )
  .enablePlugins(AkkaGrpcPlugin)


lazy val eventSourcing = (project in file("event-sourcing"))
  .settings(
    libraryDependencies ++= EventSourcingDeps
  )
  .enablePlugins(AkkaGrpcPlugin)

lazy val workDistribution = (project in file("work-distribution"))
  .settings(
    libraryDependencies ++= WorkDistributionDeps
  )
  .enablePlugins(AkkaGrpcPlugin)

lazy val stateDistribution = (project in file("state-distribution"))
  .settings(
    libraryDependencies ++= StateDistributionDeps
  )
  .dependsOn(eventSourcing)
  .enablePlugins(AkkaGrpcPlugin)

lazy val distributedProcessing = (project in file("distributed-processing"))
  .settings(
    libraryDependencies ++= DistributedProcessingDeps
  )
  .dependsOn()
  .enablePlugins(AkkaGrpcPlugin)
