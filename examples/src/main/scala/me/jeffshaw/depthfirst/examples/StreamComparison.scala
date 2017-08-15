package me.jeffshaw.depthfirst.examples

import me.jeffshaw.depthfirst.StackDepthFirst

/*
Two objects, that when executed, shows the evaluation order of Stream and DepthFirst.
 */

object VectorExample extends App {

  def f(name: String)(x: String): Vector[String] = {
    println(s"$name on $x")
    Vector(x, x + "dup")
  }

  val f0 = f("f0") _

  val f1 = f("f1") _

  val values = Vector.tabulate(2)(i => i.toString)

  for {
    v <- values
    f0_ <- f0(v)
    f1_ <- f1(f0_)
  } println("yield " + f1_)

}

object StreamExample extends App {

  def f(name: String)(x: String): Vector[String] = {
    println(s"$name on $x")
    Vector(x, x + "dup")
  }

  def y(x: String): String = {
    println("yield " + x)
    x
  }

  val f0 = f("f0") _

  val f1 = f("f1") _

  val values = Stream.tabulate(100)(i => i.toString)

  val results =
    for {
      v <- values
      f0_ <- f0(v)
      f1_ <- f1(f0_)
    } yield y(f1_)

  results.lastOption

}

object DepthFirstExample extends App {

  def f(name: String)(x: String): Vector[String] = {
    println(s"$name on $x")
    Vector(x, x + "dup")
  }

  def y(x: String): String = {
    println("yield " + x)
    x
  }

  val f0 = f("f0") _

  val f1 = f("f1") _

  val values = Vector.tabulate(100)(i => i.toString)

  val results =
    for {
      v <- StackDepthFirst(values)
      f0_ <- f0(v)
      f1_ <- f1(f0_)
    } yield y(f1_)

  results.toIterator.foreach(x => ())

}
