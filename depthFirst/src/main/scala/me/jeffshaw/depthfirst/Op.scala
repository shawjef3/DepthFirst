package me.jeffshaw.depthfirst

import scala.collection.GenTraversableOnce

sealed trait Op {
  /*
  As we go through the stack, we lose type information.
   */
  private[depthfirst] def toElem(ops: List[Op], values: Iterator[Any]): Elem
}

object Op {
  case class Map(f: Any => Any) extends Op {
    override private[depthfirst] def toElem(ops: List[Op], values: Iterator[Any]): Elem =
      Elem.Map(f, ops, values)
  }

  case class FlatMap(f: Any => GenTraversableOnce[Any]) extends Op {
    override private[depthfirst] def toElem(ops: List[Op], values: Iterator[Any]): Elem =
      Elem.FlatMap(f, ops, values)
  }

  case class Filter(f: Any => Boolean) extends Op {
    override private[depthfirst] def toElem(ops: List[Op], values: Iterator[Any]): Elem =
      Elem.Filter(f, ops, values)
  }

}
