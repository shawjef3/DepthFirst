package me.jeffshaw.depthfirst.examples

import me.jeffshaw.depthfirst.DepthFirst

object StreamExample extends App {

  def f(name: String)(x: Int): Stream[Int] = {
    println(s"$name on $x")
    Stream(x)
  }

  val f0 = f("f0") _

  val f1 = f("f1") _

  val values = Stream.tabulate(100)(identity)

  val results =
    for {
      v <- values
      f0_ <- f0(v)
      f1_ <- f1(v)
    } yield f1_

  results.lastOption

}

object DlfExample extends App {

  def f(name: String)(x: Int): Vector[Int] = {
    println(s"$name on $x")
    Vector(x)
  }

  val f0 = f("f0") _

  val f1 = f("f1") _

  val values = Vector.tabulate(100)(identity)

  val results =
    for {
      v <- DepthFirst(values)
      f0_ <- f0(v)
      f1_ <- f1(v)
    } yield f1_

  results.toIterator.toVector

}
