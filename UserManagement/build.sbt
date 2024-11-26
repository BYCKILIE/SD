import scala.collection.Seq

name := """UserManagement"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.15"

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,

  // Postgres driver
  "org.postgresql" % "postgresql" % "42.7.3",
  "com.typesafe.play" %% "play-slick" % "5.3.0",

  // JSON parser
  "io.circe" %% "circe-core" % "0.14.9",
  "io.circe" %% "circe-generic" % "0.14.9",
  "io.circe" %% "circe-parser" % "0.14.9",

  // Password encryption in database
  "org.mindrot" % "jbcrypt" % "0.4",

  // Authentication Token generator
  "com.pauldijou" %% "jwt-play" % "5.0.0",

  // HTTP requests
  ws,
  ehcache
)
