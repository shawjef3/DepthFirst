package me.jeffshaw.depthfirst

import scala.collection.generic.CanBuildFrom
import scala.collection.{GenTraversableOnce, TraversableLike, mutable}

sealed abstract class DfList[+A] extends TraversableLike[A, DfList[A]] with Traversable[A] {

  private[depthfirst] val ops: DfList[Op]

  private[depthfirst] def valuesIterator: Iterator[Any]

  protected def appendOp[B](op: Op): DfList[B]

  protected[depthfirst] def withOps[B](ops: DfList[Op]): DfList[B]

  override def flatMap[B, That](f: (A) => GenTraversableOnce[B])(implicit bf: CanBuildFrom[DfList[A], B, That]): That = {
    if (bf == DfList.CBF)
      appendOp(Op.FlatMap(f.asInstanceOf[Any => GenTraversableOnce[Any]])).asInstanceOf[That]
    else super.flatMap[B, That](f)
  }

  override def map[B, That](f: (A) => B)(implicit bf: CanBuildFrom[DfList[A], B, That]): That = {
    if (bf == DfList.CBF) {
      val build = new DfList.Buffer[B]
      for (value <- appendOp(Op.Map(f.asInstanceOf[Any => Any])).asInstanceOf[DfList[B]].toIterator)
        build += value

      build.result().asInstanceOf[That]
    } else super.map[B, That](f)
  }

  override def withFilter(p: (A) => Boolean): DfList[A] =
    appendOp(Op.Filter(p.asInstanceOf[Any => Boolean]))

  override def foreach[U](f: (A) => U): Unit =
    toIterator.foreach(f)

  def reverse: DfList[A]

  override def nonEmpty = !isEmpty

  //TraversableLike
  override protected[this] def newBuilder = new DfList.Buffer[A]

  override def seq = this
}

object DfList {
  def apply[A](values: A*): DfList[A] = {
    val builder = new Buffer[A]
    for (value <- values)
      builder += value

    builder.result()
  }

  /**
    * Similar to [[scala.collection.mutable.ListBuffer]], for quick building of DfList.
    * @tparam A
    */
  final class Buffer[A] extends mutable.Builder[A, DfList[A]] {

    private var start: DfList[A] = DfNil
    private var last0: DfCons[A] = _
    private var len: Int = 0

    def isEmpty: Boolean = len == 0

    override def +=(x: A): this.type = {
      if (isEmpty) {
        last0 = new DfCons(x, DfNil, DfNil)
        start = last0
      } else {
        val last1 = last0
        last0 = new DfCons(x, DfNil, DfNil)
        last1.tl = last0
      }
      len += 1
      this
    }

    def prepend(x: A): this.type = {
      start = new DfCons(x, start, DfNil)
      len += 1
      this
    }

    override def clear(): Unit = {
      start = DfNil
      last0 = null
      len = 0
    }

    def result(): DfList[A] = start

    override def sizeHint(size: Int): Unit = ()

    override def sizeHint(coll: TraversableLike[_, _]): Unit = ()

    override def sizeHint(coll: TraversableLike[_, _], delta: Int): Unit = ()

    override def sizeHintBounded(size: Int, boundingColl: TraversableLike[_, _]): Unit = ()
  }

  object CBF extends CanBuildFrom[Any, Any, Any] {
    override def apply(from: Any) = new Buffer[Any]

    override def apply() = new Buffer[Any]
  }

  implicit def canBuildFrom[From, Elem]: CanBuildFrom[From, Elem, DfList[Elem]] = {
    CBF.asInstanceOf[CanBuildFrom[From, Elem, DfList[Elem]]]
  }
}

case object DfNil extends DfList[Nothing] {
  override protected[depthfirst] def withOps[B](ops: DfList[Op]): DfList[B] = this

  override private[depthfirst] val ops = DfNil

  override private[depthfirst] val valuesIterator = Iterator.empty

  override protected def appendOp[B](op: Op): DfList[B] = DfNil

  override def head: Nothing =
    throw new UnsupportedOperationException

  override def tail: DfList[Nothing] =
    throw new UnsupportedOperationException

  override def toIterator: Iterator[Nothing] = Iterator.empty

  override def reverse: DfList[Nothing] = this

  override val isEmpty: Boolean = true

  def ++[A](those: DfList[A]): DfList[A] = {
    those
  }

  override val toString = "DfList()"
}

class DfCons[A] private[depthfirst] (
  override val head: A,
  private[depthfirst] var tl: DfList[A],
  override private[depthfirst] val ops: DfList[Op]
) extends DfList[A] {
  dfCons =>

  override protected[depthfirst] def withOps[B](ops: DfList[Op]): DfList[B] =
    new DfCons(head, tl, ops).asInstanceOf[DfList[B]]

  override def tail: DfList[A] = {
    if (tl.isEmpty) {
      DfNil
    } else if (tl.ops eq ops)
      tl
    else new DfCons[A](tl.head, tl.asInstanceOf[DfCons[A]].tl, ops)
  }

  override private[depthfirst] def valuesIterator: Iterator[Any] =
    new Iterator[Any] {
      var here: DfList[A] = dfCons

      override def hasNext: Boolean = here.nonEmpty

      override def next(): Any = {
        if (here.isEmpty) {
          throw new UnsupportedOperationException
        } else {
          //avoiding pattern matching gives some performance
          //yes, this is a hot spot
          val hereCons = here.asInstanceOf[DfCons[A]]
          val head = here.head
          here = hereCons.tl
          head
        }
      }
    }

  override protected def appendOp[B](op: Op): DfList[B] =
    new DfCons[B](head.asInstanceOf[B], tl.asInstanceOf[DfList[B]], DfCons(op, ops))

  override def toIterator: Iterator[A] =
    StackDepthFirst.iterator(valuesIterator, ops)

  override def reverse: DfList[A] = {
    val builder = new DfList.Buffer[Any]
    for (value <- valuesIterator)
      builder.prepend(value)

    builder.result().withOps[A](ops)
  }

  override val isEmpty = false

  def ++(those: DfList[A]): DfList[A] = {
    val builder = new DfList.Buffer[A]
    for (value <- this)
      builder += value
    for (value <- those)
      builder += value

    builder.result()
  }

  override def toString() = {
    mkString("DfList(", ",", ")")
  }

  override def equals(obj: scala.Any) = {
    obj match {
      case other: DfList[Any] =>
        size == other.size && {
          val otherIterator = other.toIterator
          val thisIterator = toIterator

          otherIterator.zip(thisIterator).forall { case (x, y) => x == y }
        }

      case _ =>
        false
    }
  }
}

object DfCons {
  def apply[A](head: A, tail: DfList[A]): DfList[A] =
    new DfCons[A](head, tail, DfNil)

  def unapply[A](t: DfList[A]): Option[(A, DfList[A])] =
    t match {
      case DfNil =>
        None
      case c: DfCons[A] =>
        Some((c.head, c.tail))
    }
}
