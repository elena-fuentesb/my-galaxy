import sbt.Keys.{libraryDependencies, scalaVersion}

ThisBuild / version := "0.1.0-SNAPSHOT"

val http4sVersion = "0.23.10"
val circeVersion = "0.14.1"

lazy val root = (project in file("."))
  .settings(
    scalaVersion := "3.1.1",
    name := "my-galaxy-art",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,

      // Logging
      "ch.qos.logback" % "logback-classic" % "1.2.10",

      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,

      // Firebase
      "com.google.firebase" % "firebase-admin" % "8.1.0",

      "joda-time" % "joda-time" % "2.10.13",

      "com.github.andyglow" %% "typesafe-config-scala" % "2.0.0"
    )
  )