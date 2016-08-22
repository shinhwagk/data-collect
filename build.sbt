name := "data-collect"

version := "1.0-alpha-2"

scalaVersion := "2.11.8"

lazy val root = (project in file(".")).enablePlugins(JavaAppPackaging)

resolvers ++= Seq("Spray Repository" at "http://dev.rtmsoft.me/nexus/content/groups/public/")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.4.9",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.9",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.9",
  "com.wingtech" % "ojdbc" % "7",
  "org.apache.logging.log4j" % "log4j-core" % "2.6.2",
  "org.apache.logging.log4j" % "log4j-api" % "2.6.2",
  "org.scalactic" %% "scalactic" % "3.0.0",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)

