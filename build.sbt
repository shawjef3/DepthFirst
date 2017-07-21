lazy val base = project.in(file("dlf"))

lazy val benchmarks = project.in(file("benchmarks")).dependsOn(base)

lazy val aggregate =
  project.in(file(".")).
  aggregate(base, benchmarks)

scalaVersion := "2.12.2"
