lazy val depthFirst = project.in(file("depth-first"))

lazy val benchmarks = project.in(file("benchmarks")).dependsOn(depthFirst)

lazy val examples = project.in(file("examples")).dependsOn(depthFirst)

lazy val depthFirstAggregate =
  project.in(file(".")).
  aggregate(depthFirst, benchmarks, examples)
