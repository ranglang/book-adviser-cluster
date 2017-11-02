name := "book-adviser-cluster"

version := "1.0"

scalaVersion := "2.12.1"

val akkaVersion = "2.5.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-distributed-data" % akkaVersion
)