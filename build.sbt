lazy val stackless = project.in(file("stackless"))

lazy val benchmarks = project.in(file("benchmarks")).dependsOn(stackless)

lazy val examples = project.in(file("examples")).dependsOn(stackless)

lazy val depthFirstAggregate =
  project.in(file(".")).
  aggregate(stackless, benchmarks, examples)
