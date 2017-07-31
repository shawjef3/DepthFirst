package me.jeffshaw.depthfirst.benchmarks

import scala.collection.mutable.Buffer
import java.util.concurrent.TimeUnit
import me.jeffshaw.depthfirst.{DepthFirst, Op}
import org.openjdk.jmh.annotations.{State => JmhState, _}

@JmhState(Scope.Thread)
class BufferFlatMapBenchmarks {

  @Param(Array("0", "1", "1000", "5000", "10000", "50000", "100000", "1000000", "2000000", "3000000"))
  var valueCount: Int = _

  var values: Buffer[Int] = _

  @Param(Array("1", "3", "5", "10", "15", "20"))
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
    DepthFirst.run[Int, Int, Buffer[Int]](values, op, Seq.fill(iterationCount - 1)(op): _*)
  }

}

object BufferFlatMapBenchmarks {
  val values = Buffer.fill(16777216)(util.Random.nextInt())
}
