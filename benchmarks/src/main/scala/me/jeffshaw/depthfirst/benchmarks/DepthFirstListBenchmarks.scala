package me.jeffshaw.depthfirst.benchmarks

import java.util.concurrent.TimeUnit
import me.jeffshaw.depthfirst.DepthFirstList
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

@Fork(jvmArgs = Array("-Xms7g"))
@State(Scope.Thread)
class DepthFirstListBenchmarks extends HighPriority {

  @Param(Array("1", "16", "128", "4096"))
  var valueCount: Int = _

  @Param(Array("1", "2", "4"))
  var duplicationFactor: Int = _

  @Param(Array("1", "2", "4", "8"))
  var iterationCount: Int = _

  var values: DepthFirstList[Int] = _

  @Setup(Level.Iteration)
  def setup(): Unit = {
    values = DepthFirstList(BenchmarkValues.values.take(valueCount): _*)
  }

  def duplicate(x: Int): DepthFirstList[Int] = {
    val builder = new DepthFirstList.Buffer[Int]
    for (_ <- 0 until duplicationFactor)
      builder += x
    builder.result()
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def depthFirstList(): Unit = {
    iterationCount match {
      case 1 =>
        depthFirstList1()
      case 2 =>
        depthFirstList2()
      case 4 =>
        depthFirstList4()
      case 8 =>
        depthFirstList8()
    }
  }

  def depthFirstList1(): Unit = {
    for {
      x0 <- values
      x1 <- duplicate(x0)
    } x1
  }

  def depthFirstList2(): Unit = {
    for {
      x0 <- values
      x1 <- duplicate(x0)
      x2 <- duplicate(x1)
    } x2
  }

  def depthFirstList4(): Unit = {
    for {
      x0 <- values
      x1 <- duplicate(x0)
      x2 <- duplicate(x1)
      x3 <- duplicate(x2)
      x4 <- duplicate(x3)
    } x4
  }

  def depthFirstList8(): Unit = {
    for {
      x0 <- values
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
