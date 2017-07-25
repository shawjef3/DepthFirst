package me.jeffshaw.dlf.benchmarks

import java.util.Collections
import java.util.concurrent.TimeUnit
import java.util.stream.{Collectors, IntStream}
import me.jeffshaw.dlf.{Op, State}
import org.openjdk.jmh.annotations.{State => JmhState, _}
import scala.collection.JavaConverters._

@JmhState(Scope.Thread)
class VectorFlatMapBenchmarks {

  @Param(Array("0", "1", "16", "128", "1024", "16384", "131072", "1048576", "16777216"))
  var valueCount: Int = _

  var values: Vector[Int] = _

  //Not a var, because java streams can't be reused.
  def javaValues: java.util.stream.IntStream = IntStream.of(values: _*)

  var ops: Seq[Op] = _

  @Param(Array("1", "16", "32", "64", "128"))
  var iterationCount: Int = _

  @Setup(Level.Iteration)
  def setup(): Unit = {
    values = VectorFlatMapBenchmarks.values.take(valueCount)
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
  def dlf(): Unit = {
    State.run(values, ops.head, ops.tail: _*)
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

object VectorFlatMapBenchmarks {
  val values: Vector[Int] = Vector.fill(16777216)(util.Random.nextInt())
}
