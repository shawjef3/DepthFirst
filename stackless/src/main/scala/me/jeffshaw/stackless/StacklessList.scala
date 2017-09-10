package me.jeffshaw.stackless

import scala.collection.generic.CanBuildFrom
import scala.collection.{GenTraversableOnce, TraversableLike, mutable}

sealed abstract class StacklessList[+A] extends TraversableLike[A, StacklessList[A]] with Traversable[A] {

  private[stackless] val ops: StacklessList[Op]

  protected def valuesIterator: Iterator[Any]

  protected def appendOp[B](op: Op): StacklessList[B]

  protected def withOps[B](ops: StacklessList[Op]): StacklessList[B]

  override def flatMap[B, That](f: (A) => GenTraversableOnce[B])(implicit bf: CanBuildFrom[StacklessList[A], B, That]): That = {
    if (bf == StacklessList.CBF)
      appendOp(Op.FlatMap(f.asInstanceOf[Any => GenTraversableOnce[Any]])).asInstanceOf[That]
    else super.flatMap[B, That](f)
  }

  override def map[B, That](f: (A) => B)(implicit bf: CanBuildFrom[StacklessList[A], B, That]): That = {
    if (bf == StacklessList.CBF) {
      val build = new StacklessList.Buffer[B]
      for (value <- appendOp(Op.Map(f.asInstanceOf[Any => Any])).asInstanceOf[StacklessList[B]].toIterator)
        build += value

      build.result().asInstanceOf[That]
    } else super.map[B, That](f)
  }

  override def withFilter(p: (A) => Boolean): StacklessList[A] =
    appendOp(Op.Filter(p.asInstanceOf[Any => Boolean]))

  override def foreach[U](f: (A) => U): Unit =
    toIterator.foreach(f)

  def reverse: StacklessList[A]

  override def nonEmpty: Boolean = !isEmpty

  //TraversableLike
  override protected[this] def newBuilder: mutable.Builder[A, StacklessList[A]] = new StacklessList.Buffer[A]

  def :+[B >: A](that: B): StacklessList[B]
}

object StacklessList {
  def apply[A](values: A*): StacklessList[A] = {
    val builder = new Buffer[A]
    for (value <- values)
      builder += value

    builder.result()
  }

  def empty[A]: StacklessList[A] =
    StacklessNil

  /**
    * Similar to [[scala.collection.mutable.ListBuffer]], for quick building of DfList.
    * @tparam A
    */
  final class Buffer[A] extends mutable.Builder[A, StacklessList[A]] {

    private var start: StacklessList[A] = StacklessNil
    private var last0: StacklessCons[A] = _
    private var len: Int = 0

    def isEmpty: Boolean = len == 0

    override def +=(x: A): this.type = {
      if (isEmpty) {
        last0 = new StacklessCons(x, StacklessNil, StacklessNil)
        start = last0
      } else {
        val last1 = last0
        last0 = new StacklessCons(x, StacklessNil, StacklessNil)
        last1.tl = last0
      }
      len += 1
      this
    }

    def prepend(x: A): this.type = {
      val newElem = new StacklessCons(x, start, StacklessNil)
      if (isEmpty)
        last0 = newElem
      start = newElem
      len += 1
      this
    }

    override def clear(): Unit = {
      start = StacklessNil
      last0 = null
      len = 0
    }

    def result(): StacklessList[A] = start

    override def sizeHint(size: Int): Unit = ()

    override def sizeHint(coll: TraversableLike[_, _]): Unit = ()

    override def sizeHint(coll: TraversableLike[_, _], delta: Int): Unit = ()

    override def sizeHintBounded(size: Int, boundingColl: TraversableLike[_, _]): Unit = ()
  }

  object CBF extends CanBuildFrom[Any, Any, Any] {
    override def apply(from: Any) = new Buffer[Any]

    override def apply() = new Buffer[Any]
  }

  implicit def canBuildFrom[From, Elem]: CanBuildFrom[From, Elem, StacklessList[Elem]] = {
    CBF.asInstanceOf[CanBuildFrom[From, Elem, StacklessList[Elem]]]
  }
}

case object StacklessNil extends StacklessList[Nothing] {
  override protected[stackless] def withOps[B](ops: StacklessList[Op]): StacklessList[B] = this

  override private[stackless] val ops = StacklessNil

  override protected val valuesIterator: Iterator[Nothing] = Iterator.empty

  override protected def appendOp[B](op: Op): StacklessList[B] = StacklessNil

  override def head: Nothing =
    throw new UnsupportedOperationException

  override def tail: StacklessList[Nothing] =
    throw new UnsupportedOperationException

  override def toIterator: Iterator[Nothing] = Iterator.empty

  override def reverse: StacklessList[Nothing] = this

  override val isEmpty: Boolean = true

  def ++[A](those: StacklessList[A]): StacklessList[A] = {
    those
  }

  override def :+[B >: Nothing](that: B) = {
    StacklessCons(that, StacklessNil)
  }

  override val toString = "StacklessList()"
}

final case class StacklessCons[A] private[stackless] (
  override val head: A,
  private[stackless] var tl: StacklessList[A],
  override private[stackless] val ops: StacklessList[Op]
) extends StacklessList[A] {
  dfCons =>

  override protected[stackless] def withOps[B](ops: StacklessList[Op]): StacklessList[B] =
    new StacklessCons(head, tl, ops).asInstanceOf[StacklessList[B]]

  override def tail: StacklessList[A] = {
    if (tl.isEmpty) {
      StacklessNil
    } else if (tl.ops eq ops)
      tl
    else new StacklessCons[A](tl.head, tl.asInstanceOf[StacklessCons[A]].tl, ops)
  }

  override protected def valuesIterator: Iterator[Any] =
    new Iterator[Any] {
      var here: StacklessList[A] = dfCons

      override def hasNext: Boolean = here.nonEmpty

      override def next(): Any = {
        if (here.isEmpty) {
          throw new UnsupportedOperationException
        } else {
          //avoiding pattern matching gives some performance
          //yes, this is a hot spot
          val hereCons = here.asInstanceOf[StacklessCons[A]]
          val head = here.head
          here = hereCons.tl
          head
        }
      }
    }

  override protected def appendOp[B](op: Op): StacklessList[B] =
    new StacklessCons[B](head.asInstanceOf[B], tl.asInstanceOf[StacklessList[B]], StacklessCons(op, ops))

  override def toIterator: Iterator[A] =
    Stackless.iterator(valuesIterator, ops)

  override def reverse: StacklessList[A] = {
    var result: StacklessList[A] = StacklessNil
    var here: StacklessList[A] = this
    var ops: StacklessList[Op] = here.ops
    while (here.nonEmpty) {
      result = StacklessCons(here.head, result, ops)
      here = here.tail
      if (here.ops.nonEmpty && (here.ops ne ops))
        ops = here.ops
    }

    result
  }

  override val isEmpty = false

  def ++(those: StacklessList[A]): StacklessList[A] = {
    if (those.isEmpty) {
      this
    } else {
      val builder = new StacklessList.Buffer[A]
      for (value <- this)
        builder += value
      for (value <- those)
        builder += value

      builder.result()
    }
  }

  override def :+[B >: A](that: B): StacklessList[B] = {
    val b = new StacklessList.Buffer[B]
    for (elem <- this)
      b += elem
    b += that
    b.result()
  }

  override def toString = {
    mkString("StacklessList(", ",", ")")
  }

  override def equals(obj: scala.Any) = {
    obj match {
      case other: StacklessList[Any] =>
        val otherIterator = other.toIterator
        val thisIterator = toIterator

        val sameElems = otherIterator.zip(thisIterator).forall { case (x, y) => x == y }

        sameElems && !otherIterator.hasNext && !thisIterator.hasNext

      case _ =>
        false
    }
  }
}

object StacklessCons {
  def apply[A](head: A, tail: StacklessList[A]): StacklessList[A] =
    new StacklessCons[A](head, tail, StacklessNil)

  def unapply[A](t: StacklessList[A]): Option[(A, StacklessList[A])] =
    t match {
      case StacklessNil =>
        None
      case c: StacklessCons[A] =>
        Some((c.head, c.tail))
    }
}
