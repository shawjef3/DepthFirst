package me.jeffshaw.depthfirst

import scala.collection.generic.CanBuildFrom
import scala.collection.{GenTraversableOnce, TraversableLike, mutable}

sealed abstract class DepthFirstList[+A] extends TraversableLike[A, DepthFirstList[A]] with Traversable[A] {

  private[depthfirst] val ops: DepthFirstList[Op]

  protected def valuesIterator: Iterator[Any]

  protected def appendOp[B](op: Op): DepthFirstList[B]

  protected def withOps[B](ops: DepthFirstList[Op]): DepthFirstList[B]

  override def flatMap[B, That](f: (A) => GenTraversableOnce[B])(implicit bf: CanBuildFrom[DepthFirstList[A], B, That]): That = {
    if (bf == DepthFirstList.CBF)
      appendOp(Op.FlatMap(f.asInstanceOf[Any => GenTraversableOnce[Any]])).asInstanceOf[That]
    else super.flatMap[B, That](f)
  }

  override def map[B, That](f: (A) => B)(implicit bf: CanBuildFrom[DepthFirstList[A], B, That]): That = {
    if (bf == DepthFirstList.CBF) {
      val build = new DepthFirstList.Buffer[B]
      for (value <- appendOp(Op.Map(f.asInstanceOf[Any => Any])).asInstanceOf[DepthFirstList[B]].toIterator)
        build += value

      build.result().asInstanceOf[That]
    } else super.map[B, That](f)
  }

  override def withFilter(p: (A) => Boolean): DepthFirstList[A] =
    appendOp(Op.Filter(p.asInstanceOf[Any => Boolean]))

  override def foreach[U](f: (A) => U): Unit =
    toIterator.foreach(f)

  def reverse: DepthFirstList[A]

  override def nonEmpty: Boolean = !isEmpty

  //TraversableLike
  override protected[this] def newBuilder: mutable.Builder[A, DepthFirstList[A]] = new DepthFirstList.Buffer[A]

  def :+[B >: A](that: B): DepthFirstList[B]
}

object DepthFirstList {
  def apply[A](values: A*): DepthFirstList[A] = {
    val builder = new Buffer[A]
    for (value <- values)
      builder += value

    builder.result()
  }

  def empty[A]: DepthFirstList[A] =
    DepthFirstNil

  /**
    * Similar to [[scala.collection.mutable.ListBuffer]], for quick building of DfList.
    * @tparam A
    */
  final class Buffer[A] extends mutable.Builder[A, DepthFirstList[A]] {

    private var start: DepthFirstList[A] = DepthFirstNil
    private var last0: DepthFirstCons[A] = _
    private var len: Int = 0

    def isEmpty: Boolean = len == 0

    override def +=(x: A): this.type = {
      if (isEmpty) {
        last0 = new DepthFirstCons(x, DepthFirstNil, DepthFirstNil)
        start = last0
      } else {
        val last1 = last0
        last0 = new DepthFirstCons(x, DepthFirstNil, DepthFirstNil)
        last1.tl = last0
      }
      len += 1
      this
    }

    def prepend(x: A): this.type = {
      val newElem = new DepthFirstCons(x, start, DepthFirstNil)
      if (isEmpty)
        last0 = newElem
      start = newElem
      len += 1
      this
    }

    override def clear(): Unit = {
      start = DepthFirstNil
      last0 = null
      len = 0
    }

    def result(): DepthFirstList[A] = start

    override def sizeHint(size: Int): Unit = ()

    override def sizeHint(coll: TraversableLike[_, _]): Unit = ()

    override def sizeHint(coll: TraversableLike[_, _], delta: Int): Unit = ()

    override def sizeHintBounded(size: Int, boundingColl: TraversableLike[_, _]): Unit = ()
  }

  object CBF extends CanBuildFrom[Any, Any, Any] {
    override def apply(from: Any) = new Buffer[Any]

    override def apply() = new Buffer[Any]
  }

  implicit def canBuildFrom[From, Elem]: CanBuildFrom[From, Elem, DepthFirstList[Elem]] = {
    CBF.asInstanceOf[CanBuildFrom[From, Elem, DepthFirstList[Elem]]]
  }
}

case object DepthFirstNil extends DepthFirstList[Nothing] {
  override protected[depthfirst] def withOps[B](ops: DepthFirstList[Op]): DepthFirstList[B] = this

  override private[depthfirst] val ops = DepthFirstNil

  override protected val valuesIterator: Iterator[Nothing] = Iterator.empty

  override protected def appendOp[B](op: Op): DepthFirstList[B] = DepthFirstNil

  override def head: Nothing =
    throw new UnsupportedOperationException

  override def tail: DepthFirstList[Nothing] =
    throw new UnsupportedOperationException

  override def toIterator: Iterator[Nothing] = Iterator.empty

  override def reverse: DepthFirstList[Nothing] = this

  override val isEmpty: Boolean = true

  def ++[A](those: DepthFirstList[A]): DepthFirstList[A] = {
    those
  }

  override def :+[B >: Nothing](that: B) = {
    DepthFirstCons(that, DepthFirstNil)
  }

  override val toString = "StacklessList()"
}

final case class DepthFirstCons[A] private[depthfirst] (
  override val head: A,
  private[depthfirst] var tl: DepthFirstList[A],
  override private[depthfirst] val ops: DepthFirstList[Op]
) extends DepthFirstList[A] {
  dfCons =>

  override protected[depthfirst] def withOps[B](ops: DepthFirstList[Op]): DepthFirstList[B] =
    new DepthFirstCons(head, tl, ops).asInstanceOf[DepthFirstList[B]]

  override def tail: DepthFirstList[A] = {
    if (tl.isEmpty) {
      DepthFirstNil
    } else if (tl.ops eq ops)
      tl
    else new DepthFirstCons[A](tl.head, tl.asInstanceOf[DepthFirstCons[A]].tl, ops)
  }

  override protected def valuesIterator: Iterator[Any] =
    new Iterator[Any] {
      var here: DepthFirstList[A] = dfCons

      override def hasNext: Boolean = here.nonEmpty

      override def next(): Any = {
        if (here.isEmpty) {
          throw new UnsupportedOperationException
        } else {
          //avoiding pattern matching gives some performance
          //yes, this is a hot spot
          val hereCons = here.asInstanceOf[DepthFirstCons[A]]
          val head = here.head
          here = hereCons.tl
          head
        }
      }
    }

  override protected def appendOp[B](op: Op): DepthFirstList[B] =
    new DepthFirstCons[B](head.asInstanceOf[B], tl.asInstanceOf[DepthFirstList[B]], DepthFirstCons(op, ops))

  override def toIterator: Iterator[A] =
    DepthFirst.iterator(valuesIterator, ops)

  override def reverse: DepthFirstList[A] = {
    var result: DepthFirstList[A] = DepthFirstNil
    var here: DepthFirstList[A] = this
    var ops: DepthFirstList[Op] = here.ops
    while (here.nonEmpty) {
      result = DepthFirstCons(here.head, result, ops)
      here = here.tail
      if (here.ops.nonEmpty && (here.ops ne ops))
        ops = here.ops
    }

    result
  }

  override val isEmpty = false

  def ++(those: DepthFirstList[A]): DepthFirstList[A] = {
    if (those.isEmpty) {
      this
    } else {
      val builder = new DepthFirstList.Buffer[A]
      for (value <- this)
        builder += value
      for (value <- those)
        builder += value

      builder.result()
    }
  }

  override def :+[B >: A](that: B): DepthFirstList[B] = {
    val b = new DepthFirstList.Buffer[B]
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
      case other: DepthFirstList[Any] =>
        val otherIterator = other.toIterator
        val thisIterator = toIterator

        val sameElems = otherIterator.zip(thisIterator).forall { case (x, y) => x == y }

        sameElems && !otherIterator.hasNext && !thisIterator.hasNext

      case _ =>
        false
    }
  }
}

object DepthFirstCons {
  def apply[A](head: A, tail: DepthFirstList[A]): DepthFirstList[A] =
    new DepthFirstCons[A](head, tail, DepthFirstNil)

  def unapply[A](t: DepthFirstList[A]): Option[(A, DepthFirstList[A])] =
    t match {
      case DepthFirstNil =>
        None
      case c: DepthFirstCons[A] =>
        Some((c.head, c.tail))
    }
}
