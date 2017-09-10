package me.jeffshaw.stackless.benchmarks

object BenchmarkValues {
  val values = {
    val r = new util.Random(0L)
    Array.tabulate(4096)(_ => r.nextInt())
  }
}
