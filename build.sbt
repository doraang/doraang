lazy val akkaHttpVersion = "10.2.6"
lazy val akkaVersion = "2.6.15"

lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      organization := "me.chacham",
      scalaVersion := "2.13.4"
    )
  ),
  name := "dorrang",
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.2.5",
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % "3.2.9" % Test
  )
)