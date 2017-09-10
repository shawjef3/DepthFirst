package me.jeffshaw.stackless

import scala.collection.GenTraversableOnce

class Stackless[In, Out] private (
  values: GenTraversableOnce[In],
  ops: StacklessList[Op]
) extends TraversableOnce[Out] {

  def map[
    NextOut
  ](f: Out => NextOut
  ): Stackless[In, NextOut] = {
    new Stackless[In, NextOut](values, StacklessCons(Op.Map(f.asInstanceOf[Any => Any]), ops))
  }

  def flatMap[
    NextOut
  ](f: Out => GenTraversableOnce[NextOut]
  ): Stackless[In, NextOut] = {
    new Stackless[In, NextOut](values, StacklessCons(Op.FlatMap(f.asInstanceOf[Any => GenTraversableOnce[Any]]), ops))
  }

  def withFilter(f: Out => Boolean): Stackless[In, Out] = {
    new Stackless[In, Out](values, StacklessCons(Op.Filter(f.asInstanceOf[Any => Boolean]), ops))
  }

  //TraversableOnce

  override def foreach[U](f: (Out) => U): Unit = {
    val i = toIterator
    i.foreach(f)
  }

  override def isEmpty: Boolean =
    toIterator.isEmpty

  //We can't guarantee this, due to the nature of `ops`.
  override def hasDefiniteSize: Boolean =
    false

  override def seq: TraversableOnce[Out] =
    toIterator.seq

  override def forall(p: (Out) => Boolean): Boolean =
    toIterator.forall(p)

  override def exists(p: (Out) => Boolean): Boolean =
    toIterator.exists(p)

  override def find(p: (Out) => Boolean): Option[Out] =
    toIterator.find(p)

  override def copyToArray[B >: Out](xs: Array[B], start: Int, len: Int): Unit =
    toIterator.copyToArray[B](xs, start, len)

  override def toTraversable: Traversable[Out] = toIterator.toTraversable

  override def isTraversableAgain: Boolean = values.isTraversableAgain

  override def toStream: Stream[Out] = toIterator.toStream

  override def toIterator: Iterator[Out] = {
    val revOps = ops.reverse
    Stackless.iterator[In, Out](values, revOps)
  }

}

object Stackless {

  def apply[
    In
  ](values: GenTraversableOnce[In]
  ): Stackless[In, In] =
    new Stackless[In, In](values, StacklessList())

  private[stackless] def iterator[In, Out](
    values: GenTraversableOnce[In],
    ops: StacklessList[Op]
  ): Iterator[Out] = {
    if (ops.isEmpty)
      values.toIterator.asInstanceOf[Iterator[Out]]
    else {
      val op = ops.head
      val stack = Stack(op.toElem(ops.tail, values.toIterator))
      stack.iterator
    }
  }

}
