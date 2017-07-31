package me.jeffshaw.depthfirst

sealed trait Elem {
  val ops: List[Op]

  val values:Iterator[Any]
}

object Elem {

  case class DlfFlatMap(
    f: Any => DepthFirst[Any, Any],
    override val ops: List[Op],
    override val values: Iterator[Any]
  ) extends Elem

  case class Map(
    f: Any => Any,
    override val ops: List[Op],
    override val values: Iterator[Any]
  ) extends Elem

  case class FlatMap(
    f: Any => TraversableOnce[Any],
    override val ops: List[Op],
    override val values: Iterator[Any]
  ) extends Elem

  case class Filter(
    f: Any => Boolean,
    override val ops: List[Op],
    override val values: Iterator[Any]
  ) extends Elem

}
