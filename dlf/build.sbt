organization := "me.jeffshaw.dlf"

name := "base"

version := "1.0"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.2.14",
  "org.scalatest" %% "scalatest" % "3.0.3" % Test,
  "org.scalacheck" %% "scalacheck" % "1.13.5" % Test
)

scalaVersion := "2.12.2"
