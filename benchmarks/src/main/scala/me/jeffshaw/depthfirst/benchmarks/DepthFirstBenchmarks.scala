package me.jeffshaw.depthfirst.benchmarks

import java.util.concurrent.TimeUnit
import me.jeffshaw.depthfirst.DepthFirst
import org.openjdk.jmh.annotations.{State => JmhState, _}
import scala.collection.generic.CanBuildFrom

@Fork(jvmArgs = Array("-Xms7g"))
@JmhState(Scope.Thread)
class DepthFirstBenchmarks extends HighPriority {

  @Param(Array("1", "16", "128", "4096"))
  var valueCount: Int = _

  var values: Array[Int] = _
collection.mutable.ListBuffer
  var valuesVector: Vector[Int] = _

  var valuesList: List[Int] = _

  @Param(Array("1", "2", "4", "8"))
  var iterationCount: Int = _

  @Param(Array("1", "2", "4"))
  var duplicationFactor: Int = _

  def duplicate[F[_]](x: Int)(implicit canBuildFrom: CanBuildFrom[_, Int, F[Int]]): F[Int] = {
    val builder = canBuildFrom()
    builder.sizeHint(duplicationFactor)
    var i = 0
    while (i < duplicationFactor) {
      builder += x
      i += 1
    }
    builder.result()
  }

  @Setup(Level.Iteration)
  def setup(): Unit = {
    values = BenchmarkValues.values.take(valueCount)
    valuesList = values.toList
    valuesVector = values.toVector
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
      x0 <- valuesList
      x1 <- duplicate[List](x0)
    } x1
  }

  def classicList2(): Unit = {
    for {
      x0 <- valuesList
      x1 <- duplicate[List](x0)
      x2 <- duplicate[List](x1)
    } x2
  }

  def classicList4(): Unit = {
    for {
      x0 <- valuesList
      x1 <- duplicate[List](x0)
      x2 <- duplicate[List](x1)
      x3 <- duplicate[List](x2)
      x4 <- duplicate[List](x3)
    } x4
  }

  def classicList8(): Unit = {
    for {
      x0 <- valuesList
      x1 <- duplicate[List](x0)
      x2 <- duplicate[List](x1)
      x3 <- duplicate[List](x2)
      x4 <- duplicate[List](x3)
      x5 <- duplicate[List](x4)
      x6 <- duplicate[List](x5)
      x7 <- duplicate[List](x6)
      x8 <- duplicate[List](x7)
    } x8
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
      x0 <- DepthFirst(valuesList)
      x1 <- duplicate[List](x0)
    } x1
  }

  def depthFirstList2(): Unit = {
    for {
      x0 <- DepthFirst(valuesList)
      x1 <- duplicate[List](x0)
      x2 <- duplicate[List](x1)
    } x2
  }

  def depthFirstList4(): Unit = {
    for {
      x0 <- DepthFirst(valuesList)
      x1 <- duplicate[List](x0)
      x2 <- duplicate[List](x1)
      x3 <- duplicate[List](x2)
      x4 <- duplicate[List](x3)
    } x4
  }

  def depthFirstList8(): Unit = {
    for {
      x0 <- DepthFirst(valuesList)
      x1 <- duplicate[List](x0)
      x2 <- duplicate[List](x1)
      x3 <- duplicate[List](x2)
      x4 <- duplicate[List](x3)
      x5 <- duplicate[List](x4)
      x6 <- duplicate[List](x5)
      x7 <- duplicate[List](x6)
      x8 <- duplicate[List](x7)
    } x8
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def classicVector(): Unit = {
    iterationCount match {
      case 1 =>
        classicVector1()
      case 2 =>
        classicVector2()
      case 4 =>
        classicVector4()
      case 8 =>
        classicVector8()
    }
  }

  def classicVector1(): Unit = {
    for {
      x0 <- valuesVector
      x1 <- duplicate[Vector](x0)
    } x1
  }

  def classicVector2(): Unit = {
    for {
      x0 <- valuesVector
      x1 <- duplicate[Vector](x0)
      x2 <- duplicate[Vector](x1)
    } x2
  }

  def classicVector4(): Unit = {
    for {
      x0 <- valuesVector
      x1 <- duplicate[Vector](x0)
      x2 <- duplicate[Vector](x1)
      x3 <- duplicate[Vector](x2)
      x4 <- duplicate[Vector](x3)
    } x4
  }

  def classicVector8(): Unit = {
    for {
      x0 <- valuesVector
      x1 <- duplicate[Vector](x0)
      x2 <- duplicate[Vector](x1)
      x3 <- duplicate[Vector](x2)
      x4 <- duplicate[Vector](x3)
      x5 <- duplicate[Vector](x4)
      x6 <- duplicate[Vector](x5)
      x7 <- duplicate[Vector](x6)
      x8 <- duplicate[Vector](x7)
    } x8
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  def depthFirstVector(): Unit = {
    iterationCount match {
      case 1 =>
        depthFirstVector1()
      case 2 =>
        depthFirstVector2()
      case 4 =>
        depthFirstVector4()
      case 8 =>
        depthFirstVector8()
    }
  }

  def depthFirstVector1(): Unit = {
    for {
      x0 <- DepthFirst(valuesVector)
      x1 <- DepthFirst(duplicate[Vector](x0))
    } x1
  }

  def depthFirstVector2(): Unit = {
    for {
      x0 <- DepthFirst(valuesVector)
      x1 <- duplicate[Vector](x0)
      x2 <- duplicate[Vector](x1)
    } x2
  }

  def depthFirstVector4(): Unit = {
    for {
      x0 <- DepthFirst(valuesVector)
      x1 <- duplicate[Vector](x0)
      x2 <- duplicate[Vector](x1)
      x3 <- duplicate[Vector](x2)
      x4 <- duplicate[Vector](x3)
    } x4
  }

  def depthFirstVector8(): Unit = {
    for {
      x0 <- DepthFirst(valuesVector)
      x1 <- duplicate[Vector](x0)
      x2 <- duplicate[Vector](x1)
      x3 <- duplicate[Vector](x2)
      x4 <- duplicate[Vector](x3)
      x5 <- duplicate[Vector](x4)
      x6 <- duplicate[Vector](x5)
      x7 <- duplicate[Vector](x6)
      x8 <- duplicate[Vector](x7)
    } x8
  }

}
