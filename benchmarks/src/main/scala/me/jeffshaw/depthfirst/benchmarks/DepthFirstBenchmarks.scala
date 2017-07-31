package me.jeffshaw.depthfirst.benchmarks

import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import me.jeffshaw.depthfirst.DepthFirst
import org.openjdk.jmh.annotations.{State => JmhState, _}

@JmhState(Scope.Thread)
class DepthFirstBenchmarks {

  @Param(Array("0", "1", "1000", "5000", "10000", "50000", "100000", "1000000", "2000000", "3000000"))
  var valueCount: Int = _

  var values: Array[Int] = _

  var valuesStream: Stream[Int] = _

  var valuesBoxedArray: Array[Integer] = _

  //Not a var, because java streams can't be reused.
  def javaValues: java.util.stream.IntStream = IntStream.of(values: _*)

  def javaBoxedValues: java.util.stream.Stream[Integer] =
    java.util.Arrays.stream(valuesBoxedArray)

  @Param(Array("1", "3", "5", "10", "15", "20"))
  var iterationCount: Int = _

  @Setup(Level.Iteration)
  def setup(): Unit = {
    values = DepthFirstBenchmarks.values.take(valueCount)
    valuesStream = values.toStream
    valuesBoxedArray = values.map(new Integer(_))
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def classic(): Unit = {
    var result = values
    for (i <- 1 to iterationCount) {
      result = result.flatMap(x => Vector(x))
    }
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def stream(): Unit = {
    var result = valuesStream
    for (i <- 1 to iterationCount) {
      result = result.flatMap(x => Stream(x))
    }
    //force evaluation
    result.lastOption
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def depthFirst(): Unit = {
    var df = DepthFirst(values)
    for (i <- 1 to iterationCount) {
      df = df.flatMap(x => Vector(x))
    }
    val i = df.toIterator
    for (_ <- i) ()
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def javaStream(): Unit = {
    var result = javaValues
    for (i <- 1 to iterationCount) {
      result = result.flatMap((x: Int) => IntStream.of(x))
    }
    result.toArray()
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def javaBoxedStream(): Unit = {
    var result = javaBoxedValues
    for (i <- 1 to iterationCount) {
      result = result.flatMap((x: Integer) => java.util.stream.Stream.of[Integer](x))
    }
    result.forEach(_ => ())
  }

}

object DepthFirstBenchmarks {
  val values: Array[Int] = Array.fill(16777216)(util.Random.nextInt())
}
