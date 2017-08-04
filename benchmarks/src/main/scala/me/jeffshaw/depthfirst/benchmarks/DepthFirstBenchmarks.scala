package me.jeffshaw.depthfirst.benchmarks

import java.util.concurrent.TimeUnit
import me.jeffshaw.depthfirst.DepthFirst
import org.openjdk.jmh.annotations.{State => JmhState, _}

@JmhState(Scope.Thread)
class DepthFirstBenchmarks {

  @Param(Array("0", "1","16","256","4096","65536","1048576","16777216"))
  var valueCount: Int = _

  var values: Array[Int] = _

  var valuesSeq: Seq[Int] = _

  var valuesVector: Vector[Int] = _

  var valuesList: List[Int] = _

  var valuesStream: Stream[Int] = _

  @Param(Array("1","2","4","8"))
  var iterationCount: Int = _

  @Setup(Level.Iteration)
  def setup(): Unit = {
    values = java.util.Arrays.copyOf(DepthFirstBenchmarks.values, valueCount)
    valuesSeq = values.toSeq
    valuesList = values.toList
    valuesStream = values.toStream
    valuesVector = values.toVector
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def classicVector(): Unit = {
    var result = values
    for (_ <- 1 until iterationCount) {
      result = result.flatMap(x => Vector(x))
    }
    for (r <- result) r
  }

//  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def classicList(): Unit = {
    var result = values
    for (_ <- 1 until iterationCount) {
      result = result.flatMap(x => List(x))
    }
    for (r <- result) r
  }

//  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def stream(): Unit = {
    var result = valuesStream
    for (_ <- 1 until iterationCount) {
      result = result.flatMap(x => Stream(x))
    }
    for (r <- result) r
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def depthFirstVector(): Unit = {
    var result = DepthFirst(valuesVector)
    for (_ <- 1 to iterationCount) {
      result = result.flatMap(x => Vector(x))
    }
    for (_ <- result) ()
  }

//  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def depthFirstList(): Unit = {
    var result = DepthFirst(valuesList)
    for (_ <- 1 to iterationCount) {
      result = result.flatMap(x => List(x))
    }
    for (_ <- result) ()
  }

//  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def javaStream(): Unit = {
    var result = DepthFirst(values)
    for (i <- 1 until iterationCount) {
      result = result.flatMap(x => Array(x))
    }
    for (r <- result) r
  }

}

object DepthFirstBenchmarks {
  val values: Array[Int] = Array.fill(16777216)(util.Random.nextInt())
}
