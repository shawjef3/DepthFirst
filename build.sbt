lazy val dataLocal = project.in(file("datalocal"))

lazy val benchmarks = project.in(file("benchmarks")).dependsOn(dataLocal)

lazy val examples = project.in(file("examples")).dependsOn(dataLocal)

lazy val aggregate =
  project.in(file(".")).
  aggregate(dataLocal, benchmarks, examples)
