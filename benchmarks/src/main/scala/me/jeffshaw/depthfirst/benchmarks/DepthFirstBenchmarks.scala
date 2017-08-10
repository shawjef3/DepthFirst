package me.jeffshaw.depthfirst.benchmarks

import java.util.concurrent.TimeUnit
import java.util.stream.{Stream => JavaStream}
import me.jeffshaw.depthfirst.{StackDepthFirst, StreamDepthFirst}
import org.openjdk.jmh.annotations.{State => JmhState, _}
import scala.collection.generic.CanBuildFrom

@Fork(jvmArgs = Array("-Xms7g"))
@JmhState(Scope.Thread)
class DepthFirstBenchmarks {

  Priority.set()

  @Param(Array("0", "1", "16", "256", "1024", "4096"))
  var valueCount: Int = _

  var values: Array[Int] = _

  var valuesVector: Vector[Int] = _

  var valuesList: List[Int] = _

  var valuesStream: Stream[Int] = _

  def valuesJavaStream: JavaStream[Int] =
    JavaStream.of(values: _*)

  @Param(Array("1", "2", "4", "8"))
  var iterationCount: Int = _

  @Param(Array("1", "2", "4"))
  var duplicationFactor: Int = _

  def duplicate[F[_]](x: Int)(implicit canBuildFrom: CanBuildFrom[_, Int, F[Int]]): F[Int] = {
    val builder = canBuildFrom()
    builder.sizeHint(duplicationFactor)
    for (i <- 0 until duplicationFactor)
      builder += x + i
    builder.result()
  }

  @Setup(Level.Iteration)
  def setup(): Unit = {
    values = DepthFirstBenchmarks.values.take(valueCount)
    valuesList = values.toList
    valuesStream = values.toStream
    valuesVector = values.toVector
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def classicList(): Unit = {
    var result = valuesList
    for (_ <- 0 until iterationCount) {
      result = result.flatMap(duplicate[List])
    }
    for (r <- result) r
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def stackDepthFirstList(): Unit = {
    var result = StackDepthFirst(valuesList)
    for (_ <- 0 until iterationCount) {
      result = result.flatMap(duplicate[List])
    }
    for (_ <- result) ()
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def streamDepthFirstList(): Unit = {
    var result = StreamDepthFirst(valuesList)
    for (_ <- 0 until iterationCount) {
      result = result.flatMap(duplicate[List])
    }
    for (_ <- result) ()
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def classicStream(): Unit = {
    var result = valuesStream
    for (_ <- 0 until iterationCount) {
      result = result.flatMap(duplicate[Stream])
    }
    for (r <- result) r
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def stackDepthFirstStream(): Unit = {
    var result = StackDepthFirst(valuesStream)
    for (_ <- 0 until iterationCount) {
      result = result.flatMap(duplicate[Stream])
    }
    for (r <- result) r
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def streamDepthFirstStream(): Unit = {
    var result = StreamDepthFirst(valuesStream)
    for (_ <- 0 until iterationCount) {
      result = result.flatMap(duplicate[Stream])
    }
    for (r <- result) r
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def classicVector(): Unit = {
    var result = valuesVector
    for (_ <- 0 until iterationCount) {
      result = result.flatMap(duplicate[Vector])
    }
    for (r <- result) r
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def stackDepthFirstVector(): Unit = {
    var result = StackDepthFirst(valuesVector)
    for (_ <- 0 until iterationCount) {
      result = result.flatMap(duplicate[Vector])
    }
    for (r <- result) r
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def streamDepthFirstVector(): Unit = {
    var result = StreamDepthFirst(valuesVector)
    for (_ <- 0 until iterationCount) {
      result = result.flatMap(duplicate[Vector])
    }
    for (r <- result) r
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def javaStream(): Unit = {
    var result = valuesJavaStream
    for (i <- 0 until iterationCount) {
      result = result.flatMap((x: Int) => JavaStream.of(duplicate[Array](x): _*))
    }
    result.forEach((x: Int) => x)
  }

}

object DepthFirstBenchmarks {
  val values = {
    val r = new util.Random(0L)
    Array.tabulate(4096)(_ => r.nextInt())
  }

}
