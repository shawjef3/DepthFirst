lazy val dlf = project.in(file("dlf"))

lazy val benchmarks = project.in(file("benchmarks")).dependsOn(dlf)

lazy val examples = project.in(file("examples")).dependsOn(dlf)

lazy val aggregate =
  project.in(file(".")).
  aggregate(dlf, benchmarks, examples)

scalaVersion := "2.12.2"
