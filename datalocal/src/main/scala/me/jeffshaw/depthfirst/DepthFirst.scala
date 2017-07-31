package me.jeffshaw.depthfirst

import java.util.Spliterator
import java.util.function.Consumer
import java.util.stream.StreamSupport
import scala.collection.JavaConverters._

class DepthFirst[Out] private (
  inner: java.util.stream.Stream[Out]
) extends TraversableOnce[Out] {

  def map[NextOut](f: Out => NextOut): DepthFirst[NextOut] =
    new DepthFirst(inner.map[NextOut]((x: Out) => f(x)))

  def flatMap[
    NextOut
  ](f: Out => TraversableOnce[NextOut]
  ): DepthFirst[NextOut] = {
    new DepthFirst(inner.flatMap((x: Out) => DepthFirst.toStream(f(x))))
  }

  def withFilter(f: Out => Boolean): DepthFirst[Out] = {
    new DepthFirst[Out](inner.filter((x: Out) => f(x)))
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

  override def isTraversableAgain: Boolean = false

  override def toStream: Stream[Out] = toIterator.toStream

  override def toIterator: Iterator[Out] = {
    inner.iterator().asScala
  }

}

object DepthFirst {

  def apply[
    In
  ](values: TraversableOnce[In]
  ): DepthFirst[In] =
    new DepthFirst[In](toStream(values))

  def toStream[T](t: TraversableOnce[T]): java.util.stream.Stream[T] = {
    StreamSupport.stream(
      new Spliterator[T] {
        val i = t.toIterator
        @volatile var advances = 0L

        override def characteristics(): Int = 0

        override def trySplit(): Spliterator[T] = null

        override def tryAdvance(action: Consumer[_ >: T]): Boolean =
          if (i.hasNext) {
            action.accept(i.next())
            advances += 1L
            true
          } else false

        override def estimateSize(): Long =
          if (t.hasDefiniteSize)
            t.size - advances
          else Long.MaxValue
      },
      false
    )
  }

  def main(args: Array[String]): Unit = {
    val iterationCount = 20
    val values = Array.fill(10000000)(0)
    var df = DepthFirst(values)
    for (i <- 1 to iterationCount) {
      df = df.flatMap(x => Array(x))
    }
    val i = df.toIterator
    for (_ <- i) ()
  }

}
