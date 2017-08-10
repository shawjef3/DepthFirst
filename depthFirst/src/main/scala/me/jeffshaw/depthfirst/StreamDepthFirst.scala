package me.jeffshaw.depthfirst

import java.util.Spliterators
import java.util.stream.StreamSupport
import scala.collection.GenTraversableOnce
import scala.collection.JavaConverters._

class StreamDepthFirst[Out] private (
  inner: java.util.stream.Stream[Out]
) extends TraversableOnce[Out] {

  def map[NextOut](f: Out => NextOut): StreamDepthFirst[NextOut] =
    new StreamDepthFirst(inner.map[NextOut]((x: Out) => f(x)))

  def flatMap[
    NextOut
  ](f: Out => GenTraversableOnce[NextOut]
  ): StreamDepthFirst[NextOut] = {
    new StreamDepthFirst(inner.flatMap((x: Out) => StreamDepthFirst.toStream(f(x))))
  }

  def withFilter(f: Out => Boolean): StreamDepthFirst[Out] = {
    new StreamDepthFirst[Out](inner.filter((x: Out) => f(x)))
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

object StreamDepthFirst {

  def apply[
  In
  ](values: GenTraversableOnce[In]
  ): StreamDepthFirst[In] =
    new StreamDepthFirst[In](toStream(values))

  def toStream[T](t: GenTraversableOnce[T]): java.util.stream.Stream[T] = {
    val iterator = t.toIterator.asJava
    StreamSupport.stream(
      Spliterators.spliteratorUnknownSize(iterator, 0),
      false
    )
  }

}
