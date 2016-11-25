name := "akka-persistence-cassandra-test"

organization := "com.github.dnvriend"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.8"

val AkkaPersistenceCassandraVersion = "0.21"
val AkkaVersion = "2.4.14"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % AkkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % AkkaVersion,
  "com.typesafe.akka" %% "akka-persistence-query-experimental" % AkkaVersion,
  "com.typesafe.akka" %% "akka-persistence-cassandra" % AkkaPersistenceCassandraVersion,
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0-M1" % Test
)

enablePlugins(PlayScala)
