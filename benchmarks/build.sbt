enablePlugins(JmhPlugin)

organization := "me.jeffshaw.depthfirst"

name := "benchmarks"

scalaVersion := "2.12.2"

publishArtifact := false

libraryDependencies += "com.rocketfuel.sdbc" %% "postgresql-jdbc" % "2.0.2"
