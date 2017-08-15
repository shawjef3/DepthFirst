package me.jeffshaw.depthfirst.benchmarks

import java.util.concurrent.TimeUnit
import me.jeffshaw.depthfirst.DfList
import org.openjdk.jmh.annotations._
import scala.collection.generic.CanBuildFrom
import scala.collection.mutable

@Fork(jvmArgs = Array("-Xms7g"))
@State(Scope.Thread)
class DfListBenchmarks {
  Priority.set()

  @Param(Array("4096"))
  var valueCount: Int = _

  @Param(Array("4"))
  var duplicationFactor: Int = _

  @Param(Array("8"))
  var iterationCount: Int = _
  
  var values: mutable.WrappedArray[Int] = _

  var valuesList: List[Int] = _

  var valuesDfList: DfList[Int] = _

  @Setup(Level.Iteration)
  def setup(): Unit = {
    values = DfListBenchmarks.values.take(valueCount)
    valuesList = values.toList
    valuesDfList = DfList(values: _*)
  }

  def duplicate[F[_]](x: Int)(implicit canBuildFrom: CanBuildFrom[_, Int, F[Int]]): F[Int] = {
    val builder = canBuildFrom()
    builder.sizeHint(duplicationFactor)
    for (i <- 0 until duplicationFactor)
      builder += x + i
    builder.result()
  }

  def duplicateDf(x: Int): DfList[Int] = {
    val builder = new DfList.Buffer[Int]
    for (i <- 0 until duplicationFactor)
      builder += x + i
    builder.result()
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

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def dfList(): Unit = {
    iterationCount match {
      case 1 =>
        dfList1()
      case 2 =>
        dfList2()
      case 4 =>
        dfList4()
      case 8 =>
        dfList8()
    }
  }

  def classicList1(): Unit = {
    for {
      x0 <- valuesList
      x1 <- duplicate(x0)
    } x1
  }

  def classicList2(): Unit = {
    for {
      x0 <- valuesList
      x1 <- duplicate(x0)
      x2 <- duplicate(x1)
    } x2
  }

  def classicList4(): Unit = {
    for {
      x0 <- valuesList
      x1 <- duplicate(x0)
      x2 <- duplicate(x1)
      x3 <- duplicate(x2)
      x4 <- duplicate(x3)
    } x4
  }

  def classicList8(): Unit = {
    for {
      x0 <- valuesList
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

  def dfList1(): Unit = {
    for {
      x0 <- valuesDfList
      x1 <- duplicateDf(x0)
    } x1
  }

  def dfList2(): Unit = {
    for {
      x0 <- valuesDfList
      x1 <- duplicateDf(x0)
      x2 <- duplicateDf(x1)
    } x2
  }

  def dfList4(): Unit = {
    for {
      x0 <- valuesDfList
      x1 <- duplicateDf(x0)
      x2 <- duplicateDf(x1)
      x3 <- duplicateDf(x2)
      x4 <- duplicateDf(x3)
    } x4
  }

  def dfList8(): Unit = {
    for {
      x0 <- valuesDfList
      x1 <- duplicateDf(x0)
      x2 <- duplicateDf(x1)
      x3 <- duplicateDf(x2)
      x4 <- duplicateDf(x3)
      x5 <- duplicateDf(x4)
      x6 <- duplicateDf(x5)
      x7 <- duplicateDf(x6)
      x8 <- duplicateDf(x7)
    } x8
  }

}

object DfListBenchmarks {
  val values = {
    val r = new util.Random(0L)
    Array.tabulate(4096)(_ => r.nextInt())
  }
}
