name := "scala-trace-entryexit"

organization := "com.github.alexeyr"

version := "0.1"

scalaVersion := "2.11.8"

resolvers += Resolver.sonatypeRepo("releases")

scalacOptions ++= Seq("-feature", "-deprecation", "-Xlint", "-language:experimental.macros")

autoCompilerPlugins := true

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.slf4j" % "slf4j-api" % "1.7.21" % "optional",
  "ch.qos.logback" % "logback-classic" % "1.1.7" % "test",
  "org.apache.logging.log4j" % "log4j-api" % "2.6" % "optional",
  "org.apache.logging.log4j" % "log4j-core" % "2.6" % "test",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)
