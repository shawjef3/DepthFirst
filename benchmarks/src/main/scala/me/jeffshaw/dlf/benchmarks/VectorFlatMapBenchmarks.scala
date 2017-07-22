package me.jeffshaw.dlf.benchmarks

import java.util.concurrent.TimeUnit
import me.jeffshaw.dlf.{Op, State}
import org.openjdk.jmh.annotations.{State => JmhState, _}

@JmhState(Scope.Thread)
class VectorFlatMapBenchmarks {

  @Param(Array("0", "1", "16", "128", "1024", "16384"))
  var valueCount: Int = _

  var values: Vector[Int] = _

  @Param(Array("1", "16", "32", "64"))
  var iterationCount: Int = _

  @Setup(Level.Iteration)
  def setup(): Unit = {
    values = VectorFlatMapBenchmarks.values.take(valueCount)
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
    val op = Op.FlatMap(x => Vector(x))
    val state = State[Vector, Int, Int](op, Seq.fill(iterationCount - 1)(op): _*)
    state.run(values)
  }

}

object VectorFlatMapBenchmarks {
  val values = Vector.fill(16384)(util.Random.nextInt())
}
