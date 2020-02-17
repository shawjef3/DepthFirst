package me.jeffshaw.depthfirst.benchmarks

import java.util.concurrent.TimeUnit
import me.jeffshaw.depthfirst.DepthFirst
import org.openjdk.jmh.annotations.{State => JmhState, _}
import org.openjdk.jmh.infra.Blackhole

@Fork(jvmArgs = Array("-Xms7g"))
@JmhState(Scope.Thread)
class StreamBenchmarks {

  @Param(Array("1", "16", "128", "4096"))
  var valueCount: Int = _

  var valuesStream: Stream[Int] = _

  @Param(Array("1", "2", "4", "8"))
  var iterationCount: Int = _

  @Param(Array("1", "2", "4"))
  var duplicationFactor: Int = _

  def duplicate(x: Int): Stream[Int] = {
    Stream.fill(duplicationFactor)(x)
  }

  @Setup(Level.Iteration)
  def setup(): Unit = {
    valuesStream = BenchmarkValues.values.take(valueCount).toStream
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def stream(): Unit = {
    iterationCount match {
      case 1 =>
        stream1()
      case 2 =>
        stream2()
      case 4 =>
        stream4()
      case 8 =>
        stream8()
    }
  }

  def stream1(): Unit = {
    for {
      x0 <- valuesStream
      x1 <- duplicate(x0)
    } x1
  }

  def stream2(): Unit = {
    for {
      x0 <- valuesStream
      x1 <- duplicate(x0)
      x2 <- duplicate(x1)
    } x2
  }

  def stream4(): Unit = {
    for {
      x0 <- valuesStream
      x1 <- duplicate(x0)
      x2 <- duplicate(x1)
      x3 <- duplicate(x2)
      x4 <- duplicate(x3)
    } x4
  }

  def stream8(): Unit = {
    for {
      x0 <- valuesStream
      x1 <- duplicate(x0)
      x2 <- duplicate(x1)
      x3 <- duplicate(x2)
      x4 <- duplicate(x3)
      x5 <- duplicate(x4)
      x6 <- duplicate(x5)
      x7 <- duplicate(x6)
      x8 <- duplicate(x7)
    } x8
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def depthFirstStream(): Unit = {
    iterationCount match {
      case 1 =>
        depthFirstStream1()
      case 2 =>
        depthFirstStream2()
      case 4 =>
        depthFirstStream4()
      case 8 =>
        depthFirstStream8()
    }
  }

  def depthFirstStream1(): Unit = {
    for {
      x0 <- DepthFirst(valuesStream)
      x1 <- duplicate(x0)
    } x1
  }

  def depthFirstStream2(): Unit = {
    for {
      x0 <- DepthFirst(valuesStream)
      x1 <- duplicate(x0)
      x2 <- duplicate(x1)
    } x2
  }

  def depthFirstStream4(): Unit = {
    for {
      x0 <- DepthFirst(valuesStream)
      x1 <- duplicate(x0)
      x2 <- duplicate(x1)
      x3 <- duplicate(x2)
      x4 <- duplicate(x3)
    } x4
  }

  def depthFirstStream8(): Unit = {
    for {
      x0 <- DepthFirst(valuesStream)
      x1 <- duplicate(x0)
      x2 <- duplicate(x1)
      x3 <- duplicate(x2)
      x4 <- duplicate(x3)
      x5 <- duplicate(x4)
      x6 <- duplicate(x5)
      x7 <- duplicate(x6)
      x8 <- duplicate(x7)
    } x8
  }

}
