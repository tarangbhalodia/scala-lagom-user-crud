organization in ThisBuild := "com.lagom.user"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test

lazy val `user-crud` = (project in file("."))
  .aggregate(`user-api`, `user-impl`)

lazy val `user-api` = (project in file("user-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `user-impl` = (project in file("user-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      lagomScaladslKafkaBroker,
      "com.sksamuel.elastic4s" %% "elastic4s-http" % "6.4.0",
      "com.sksamuel.elastic4s" %% "elastic4s-play-json" % "6.4.0",
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`user-api`)

lagomCassandraEnabled in ThisBuild := false
lagomKafkaEnabled in ThisBuild := false