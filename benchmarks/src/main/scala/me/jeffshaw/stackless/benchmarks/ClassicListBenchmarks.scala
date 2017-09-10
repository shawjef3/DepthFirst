package me.jeffshaw.stackless.benchmarks

import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations._

@Fork(jvmArgs = Array("-Xms7g"))
@State(Scope.Thread)
class ClassicListBenchmarks extends HighPriority {

  @Param(Array("1", "16", "128", "4096"))
  var valueCount: Int = _

  @Param(Array("1", "2", "4"))
  var duplicationFactor: Int = _

  @Param(Array("1", "2", "4", "8"))
  var iterationCount: Int = _

  var values: List[Int] = _

  @Setup(Level.Iteration)
  def setup(): Unit = {
    values = BenchmarkValues.values.take(valueCount).toList
  }

  def duplicate(x: Int): List[Int] = {
    List.fill(duplicationFactor)(x)
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def classicList(): Unit = {
    iterationCount match {
      case 1 =>
        classicList1()
      case 2 =>
        classicList2()
      case 4 =>
        classicList4()
      case 8 =>
        classicList8()
    }
  }

  def classicList1(): Unit = {
    for {
      x0 <- values
      x1 <- duplicate(x0)
    } x1
  }

  def classicList2(): Unit = {
    for {
      x0 <- values
      x1 <- duplicate(x0)
      x2 <- duplicate(x1)
    } x2
  }

  def classicList4(): Unit = {
    for {
      x0 <- values
      x1 <- duplicate(x0)
      x2 <- duplicate(x1)
      x3 <- duplicate(x2)
      x4 <- duplicate(x3)
    } x4
  }

  def classicList8(): Unit = {
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
