package me.jeffshaw.dlf.benchmarks

import java.util.concurrent.TimeUnit
import java.util.stream.LongStream
import me.jeffshaw.dlf.{Dlf, Op}
import org.openjdk.jmh.annotations.{State => JmhState, _}

@JmhState(Scope.Thread)
class VectorLongFlatMapBenchmarks {

  @Param(Array("0", "1", "16", "128", "1024", "16384", "131072", "1048576", "16777216"))
  var valueCount: Int = _

  var values: Vector[Long] = _

  //Not a var, because java streams can't be reused.
  def javaValues: java.util.stream.LongStream = LongStream.of(values: _*)

  var ops: Seq[Op] = _

  @Param(Array("1", "16", "32", "64", "128"))
  var iterationCount: Int = _

  @Setup(Level.Iteration)
  def setup(): Unit = {
    values = VectorLongFlatMapBenchmarks.values.take(valueCount)
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
    Dlf.run[Long, Long, Vector[Long]](values, ops.head, ops.tail: _*)
  }

  //  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def javaStream(): Unit = {
    var result = javaValues
    for (i <- 1 to iterationCount) {
      result = result.flatMap((x: Long) => LongStream.of(x))
    }
    result.toArray()
  }

}

object VectorLongFlatMapBenchmarks {
  val values: Vector[Long] = Vector.fill(16777216)(util.Random.nextLong())
}
