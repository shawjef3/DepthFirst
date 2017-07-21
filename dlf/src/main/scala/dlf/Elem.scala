package dlf

sealed trait Elem {

  val ops: List[Op]

  val values: Either[Any, Iterator[Any]]

}

object Elem {

  case class Map(
    f: Any => Any,
    override val ops: List[Op],
    override val values: Either[Any, Iterator[Any]]
  ) extends Elem

  case class FlatMap(
    f: Any => Traversable[Any],
    override val ops: List[Op],
    override val values: Either[Any, Iterator[Any]]
  ) extends Elem

  case class Filter(
    f: Any => Boolean,
    override val ops: List[Op],
    override val values: Either[Any, Iterator[Any]]
  ) extends Elem

}
