package me.jeffshaw.depthfirst.benchmarks

import java.util.concurrent.TimeUnit
import me.jeffshaw.depthfirst.DepthFirst
import org.openjdk.jmh.annotations.{State => JmhState, _}

@JmhState(Scope.Thread)
class DepthFirstBenchmarks {

  @Param(Array("0","786432","1572864","2359296","3145728","3932160","4718592"))
  var valueCount: Int = _

  var values: Array[Integer] = _

  var valuesStream: Stream[Integer] = _

  @Param(Array("1", "4", "8", "12", "16"))
  var iterationCount: Int = _

  @Setup(Level.Iteration)
  def setup(): Unit = {
    values = java.util.Arrays.copyOf(DepthFirstBenchmarks.values, valueCount)
    valuesStream = values.toStream
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def classic(): Unit = {
    var result = values
    for (_ <- 1 until iterationCount) {
      result = result.flatMap(x => Array(x))
    }
    for (r <- result) r
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def stream(): Unit = {
    var result = valuesStream
    for (_ <- 1 until iterationCount) {
      result = result.flatMap(x => Stream(x))
    }
    //force evaluation
    for (r <- result) r
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def depthFirst(): Unit = {
    var df = DepthFirst(values)
    for (_ <- 1 to iterationCount) {
      df = df.flatMap(x => Array(x))
    }
    val i = df.toIterator
    for (_ <- i) ()
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def javaStream(): Unit = {
    var result = DepthFirst(values)
    for (i <- 1 until iterationCount) {
      result = result.flatMap(x => Seq(x))
    }
    for (r <- result) r
  }

}

object DepthFirstBenchmarks {
  val values: Array[Integer] = Array.fill(4718592)(util.Random.nextInt())
}
