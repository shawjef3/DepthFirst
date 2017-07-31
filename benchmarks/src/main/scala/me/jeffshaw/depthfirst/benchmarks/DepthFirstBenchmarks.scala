package me.jeffshaw.depthfirst.benchmarks

import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import me.jeffshaw.depthfirst.{DepthFirst, Op}
import org.openjdk.jmh.annotations.{State => JmhState, _}

@JmhState(Scope.Thread)
class DepthFirstBenchmarks {

  @Param(Array("0", "1", "1000", "5000", "10000", "50000", "100000", "1000000", "2000000", "3000000"))
  var valueCount: Int = _

  var values: Vector[Int] = _

  var valuesStream: Stream[Int] = _

  //Not a var, because java streams can't be reused.
  def javaValues: java.util.stream.IntStream = IntStream.of(values: _*)

  var ops: Seq[Op] = _

  @Param(Array("1", "3", "5", "10", "15", "20"))
  var iterationCount: Int = _

  @Setup(Level.Iteration)
  def setup(): Unit = {
    values = DepthFirstBenchmarks.values.take(valueCount)
    valuesStream = values.toStream
    ops = Seq.fill(iterationCount)(Op.FlatMap(x => Vector(x)))
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
  def dlf(): Unit = {
    DepthFirst.iterator[Int, Int](values, ops.head, ops.tail: _*).toVector
  }

//  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def javaStream(): Unit = {
    var result = javaValues
    for (i <- 1 to iterationCount) {
      result = result.flatMap((x: Int) => IntStream.of(x))
    }
    result.toArray()
  }

}

object DepthFirstBenchmarks {
  val values: Vector[Int] = Vector.fill(16777216)(util.Random.nextInt())
}
