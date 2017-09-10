package me.jeffshaw.stackless

import scala.collection.GenTraversableOnce

private sealed trait Elem {
  val ops: StacklessList[Op]

  val values:Iterator[Any]
}

private object Elem {

  case class Map(
    f: Any => Any,
    override val ops: StacklessList[Op],
    override val values: Iterator[Any]
  ) extends Elem

  case class FlatMap(
    f: Any => GenTraversableOnce[Any],
    override val ops: StacklessList[Op],
    override val values: Iterator[Any]
  ) extends Elem

  case class Filter(
    f: Any => Boolean,
    override val ops: StacklessList[Op],
    override val values: Iterator[Any]
  ) extends Elem

}
