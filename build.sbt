organization := "org.andmed"
name := "parallel-calc"

scalaVersion := "2.12.1"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.0" withSources()
)

// Enable assertions
fork in run := true
javaOptions in run += "-ea"
