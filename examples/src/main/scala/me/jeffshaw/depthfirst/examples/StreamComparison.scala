package me.jeffshaw.depthfirst.examples

import me.jeffshaw.depthfirst.StackDepthFirst

/*
Two objects, that when executed, shows the evaluation order of Stream and DepthFirst.
 */

object VectorExample extends App {

  def f(name: String)(x: String): Vector[String] = {
    val resultString = s"$name($x)"
    println(resultString)
    Vector(x, resultString)
  }

  def y(x: String): String = {
    println("yield " + x)
    x
  }

  val f0 = f("f0") _

  val f1 = f("f1") _

  val values = Vector.tabulate(2)(_.toString)

  for {
    v <- values
    f0_ <- f0(v)
    f1_ <- f1(f0_)
  } y(f1_)

}

object StreamExample extends App {

  def f(name: String)(x: String): Stream[String] = {
    val resultString = s"$name($x)"
    println(resultString)
    Stream(x, resultString)
  }

  val f0 = f("f0") _

  val f1 = f("f1") _

  val values = Stream.tabulate(2)(_.toString)

  val results =
    for {
      v <- values
      f0_ <- f0(v)
      f1_ <- f1(f0_)
    } yield VectorExample.y(f1_)

  results.lastOption

}

object DepthFirstExample extends App {

  def f(name: String)(x: String): Vector[String] = {
    val resultString = s"$name($x)"
    println(resultString)
    Vector(x, resultString)
  }

  val f0 = f("f0") _

  val f1 = f("f1") _

  val values = Vector.tabulate(2)(_.toString)

  val results =
    for {
      v <- StackDepthFirst(values)
      f0_ <- f0(v)
      f1_ <- f1(f0_)
    } yield VectorExample.y(f1_)

  results.toIterator.foreach(x => ())

}

object JavaStreamExample extends App {

  import java.util.function.Function
  import java.util.stream._

  def f(name: String)(x: String): Stream[String] = {
    val resultString = s"$name($x)"
    println(resultString)
    Stream.of(x, resultString)
  }

  val f0: Function[String, Stream[String]] = f("f0") _

  val f1: Function[String, Stream[String]] = f("f1") _

  val values = Seq.tabulate(2)(_.toString)

  //Java does not support forEach in embedded flatMaps.
  val results =
    Stream.of(values: _*).
      flatMap(f0).
      flatMap(f1).
      forEach(VectorExample.y)

}
