package me.jeffshaw.depthfirst

class DepthFirst[In, Out] private (
  values: => TraversableOnce[In],
  ops: List[Op]
) extends TraversableOnce[Out] {

  def map[
    NextOut
  ](f: Out => NextOut
  ): DepthFirst[In, NextOut] = {
    new DepthFirst[In, NextOut](values, Op.Map(f.asInstanceOf[Any => Any]) :: ops)
  }

  def flatMap[
    NextOut
  ](f: Out => TraversableOnce[NextOut]
  ): DepthFirst[In, NextOut] = {
    new DepthFirst[In, NextOut](values, Op.FlatMap(f.asInstanceOf[Any => TraversableOnce[Any]]) :: ops)
  }

  def flatMap[
    NextOut
  ](f: Out => DepthFirst[Out, NextOut]
  )(implicit d: DummyImplicit
  ): DepthFirst[In, NextOut] = {
    new DepthFirst[In, NextOut](values, Op.DlfFlatMap(f.asInstanceOf[Any => DepthFirst[Any, Any]]) :: ops)
  }

  def withFilter(f: Out => Boolean): DepthFirst[In, Out] = {
    new DepthFirst[In, Out](values, Op.Filter(f.asInstanceOf[Any => Boolean]) :: ops)
  }

  //TraversableOnce

  override def foreach[U](f: (Out) => U): Unit = toIterator.foreach(f)

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
    DepthFirst.iterator[In, Out](values, revOps.head, revOps.tail: _*)
  }

}

object DepthFirst {

  def apply[
    In
  ](values: => TraversableOnce[In]
  ): DepthFirst[In, In] =
    new DepthFirst[In, In](values, List())

  def iterator[In, Out](
    values: TraversableOnce[In],
    op: Op,
    ops: Op*
  ): Iterator[Out] = {
    val stack = Stack(op.toElem(ops.toList, values.toIterator))

    new Iterator[Out] {
      var innerIterator: Iterator[Out] = Iterator()

      override def hasNext: Boolean = {
        while (!innerIterator.hasNext && !stack.isFinished) {
          stack.step()
          innerIterator = stack.valuesIterator
        }

        innerIterator.hasNext
      }

      override def next(): Out =
        innerIterator.next()
    }
  }

}
