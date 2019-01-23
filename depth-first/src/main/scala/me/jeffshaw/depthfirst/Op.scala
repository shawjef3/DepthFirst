package me.jeffshaw.depthfirst

import scala.collection.GenTraversableOnce

private[depthfirst] sealed trait Op {
  /*
  As we go through the stack, we lose type information.
   */
  def toElem(ops: DepthFirstList[Op], values: Iterator[Any]): Elem
}

private object Op {
  case class Map(f: Any => Any) extends Op {
    override def toElem(ops: DepthFirstList[Op], values: Iterator[Any]): Elem =
      Elem.Map(f, ops, values)
  }

  case class FlatMap(f: Any => GenTraversableOnce[Any]) extends Op {
    override def toElem(ops: DepthFirstList[Op], values: Iterator[Any]): Elem =
      Elem.FlatMap(f, ops, values)
  }

  case class Filter(f: Any => Boolean) extends Op {
    override def toElem(ops: DepthFirstList[Op], values: Iterator[Any]): Elem =
      Elem.Filter(f, ops, values)
  }

}
