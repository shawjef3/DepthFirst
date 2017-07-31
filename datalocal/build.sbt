organization := "me.jeffshaw.depthfirst"

name := "depthfirst"

description := "A fast implementation of flatMap, map, and withFilter for large collections."

version := "0.0-M0"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.3" % Test,
  "org.scalacheck" %% "scalacheck" % "1.13.5" % Test
)

scalaVersion := "2.12.2"

crossScalaVersions := Seq(
  "2.11.11",
  "2.10.6"
)

pomExtra :=
  <url>https://github.com/shawjef3/DepthFirst</url>
  <developers>
    <developer>
      <name>Jeff Shaw</name>
      <id>shawjef3</id>
      <url>https://github.com/shawjef3/</url>
    </developer>
  </developers>
  <scm>
    <url>git@github.com:shawjef3/DepthFirst.git</url>
    <connection>scm:git:git@github.com:shawjef3/DepthFirst.git</connection>
  </scm>
  <licenses>
    <license>
      <name>GNU General Public License, Version 3</name>
      <url>http://www.gnu.org/licenses/gpl-3.0.html</url>
    </license>
  </licenses>

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true
