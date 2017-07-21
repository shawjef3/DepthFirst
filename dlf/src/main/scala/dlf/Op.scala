package dlf

sealed trait Op {
  def toElem(ops: List[Op], values: Iterator[Any]): Elem
}

object Op {
  case class Map(f: Any => Any) extends Op {
    override def toElem(ops: List[Op], values: Iterator[Any]): Elem =
      Elem.Map(f, ops, Right(values))
  }
  case class FlatMap(f: Any => Traversable[Any]) extends Op {
    override def toElem(ops: List[Op], values: Iterator[Any]): Elem =
      Elem.FlatMap(f, ops, Right(values))
  }
  case class Filter(f: Any => Boolean) extends Op {
    override def toElem(ops: List[Op], values: Iterator[Any]): Elem =
      Elem.Filter(f, ops, Right(values))
  }
}
