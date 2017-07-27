package me.jeffshaw.dlf.benchmarks

import scala.collection.mutable.Buffer
import java.util.concurrent.TimeUnit
import me.jeffshaw.dlf.{State, Op}
import org.openjdk.jmh.annotations.{State => JmhState, _}

@JmhState(Scope.Thread)
class BufferFlatMapBenchmarks {

  @Param(Array("0", "1", "16", "128", "1024", "16384", "131072", "1048576", "16777216"))
  var valueCount: Int = _

  var values: Buffer[Int] = _

  @Param(Array("1", "16", "32", "64", "128"))
  var iterationCount: Int = _

  @Setup(Level.Iteration)
  def setup(): Unit = {
    values = BufferFlatMapBenchmarks.values.take(valueCount)
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def classic(): Unit = {
    var result = values
    for (i <- 1 to iterationCount) {
      result = result.flatMap(x => Buffer(x))
    }
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def dlf(): Unit = {
    val op = Op.FlatMap(x => Buffer(x))
    State.run[Int, Int, Buffer[Int]](values, op, Seq.fill(iterationCount - 1)(op): _*)
  }

}

object BufferFlatMapBenchmarks {
  val values = Buffer.fill(16777216)(util.Random.nextInt())
}
