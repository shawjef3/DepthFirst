package me.jeffshaw.stackless.benchmarks

import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations._

@Fork(jvmArgs = Array("-Xms7g"))
@State(Scope.Thread)
class BreadthFirstBenchmarks extends HighPriority {

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
  def breadthFirstList(): Unit = {
    iterationCount match {
      case 1 =>
        breadthFirstList1()
      case 2 =>
        breadthFirstList2()
      case 4 =>
        breadthFirstList4()
      case 8 =>
        breadthFirstList8()
    }
  }

  def breadthFirstList1(): Unit = {
    values.flatMap(duplicate).map(identity)
  }

  def breadthFirstList2(): Unit = {
    val r0: List[Int] = values.flatMap(duplicate)
    val r1: List[Int] = r0.flatMap(duplicate)
    r1.foreach(identity)
  }

  def breadthFirstList4(): Unit = {
    val r0: List[Int] = values.flatMap(duplicate)
    val r1: List[Int] = r0.flatMap(duplicate)
    val r2: List[Int] = r1.flatMap(duplicate)
    val r3: List[Int] = r2.flatMap(duplicate)
    r3.foreach(identity)
  }

  def breadthFirstList8(): Unit = {
    val r0: List[Int] = values.flatMap(duplicate)
    val r1: List[Int] = r0.flatMap(duplicate)
    val r2: List[Int] = r1.flatMap(duplicate)
    val r3: List[Int] = r2.flatMap(duplicate)
    val r4: List[Int] = r3.flatMap(duplicate)
    val r5: List[Int] = r4.flatMap(duplicate)
    val r6: List[Int] = r5.flatMap(duplicate)
    val r7: List[Int] = r6.flatMap(duplicate)
    r7.foreach(identity)
  }

}
