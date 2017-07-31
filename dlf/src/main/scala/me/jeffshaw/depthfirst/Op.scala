package me.jeffshaw.depthfirst

sealed trait Op {
  /*
  As we go through the stack, we lose type information.
   */
  def toElem(ops: List[Op], values: Iterator[Any]): Elem
}

object Op {
  case class DlfFlatMap(f: Any => DepthFirst[Any, Any]) extends Op {
    def toElem(ops: List[Op], values: Iterator[Any]): Elem =
      Elem.DlfFlatMap(f, ops, values)
  }

  case class Map(f: Any => Any) extends Op {
    override def toElem(ops: List[Op], values: Iterator[Any]): Elem =
      Elem.Map(f, ops, values)
  }

  case class FlatMap(f: Any => TraversableOnce[Any]) extends Op {
    override def toElem(ops: List[Op], values: Iterator[Any]): Elem =
      Elem.FlatMap(f, ops, values)
  }

  case class Filter(f: Any => Boolean) extends Op {
    override def toElem(ops: List[Op], values: Iterator[Any]): Elem =
      Elem.Filter(f, ops, values)
  }

}
