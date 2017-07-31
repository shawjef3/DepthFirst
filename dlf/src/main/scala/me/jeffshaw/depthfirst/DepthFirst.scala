package me.jeffshaw.depthfirst

import scala.collection.generic.CanBuildFrom

class DepthFirst[In, Out, This] private (
  private val values: TraversableOnce[In],
  private val ops: List[Op]
)(implicit val innerBuilder: CanBuildFrom[_, Out, This]
) {

  lazy val results: This = {
    val revOps = ops.reverse
    DepthFirst.run[In, Out, This](values, revOps.head, revOps.tail: _*)
  }

  def iterator: Iterator[Out] = {
    val revOps = ops.reverse
    DepthFirst.iterator[In, Out](values.toIterable, revOps.head, revOps.tail: _*)
  }

  def map[NextOut, That](f: Out => NextOut)(implicit innerBuilder: CanBuildFrom[_, NextOut, That]): DepthFirst[In, NextOut, That] = {
    new DepthFirst[In, NextOut, That](values, Op.Map(f.asInstanceOf[Any => Any]) :: ops)
  }

  def flatMap[NextOut, That](f: Out => TraversableOnce[NextOut])(implicit innerBuilder: CanBuildFrom[_, NextOut, That]): DepthFirst[In, NextOut, That] = {
    new DepthFirst[In, NextOut, That](values, Op.FlatMap(f.asInstanceOf[Any => TraversableOnce[Any]]) :: ops)
  }

  def flatMap[NextOut, That](f: Out => DepthFirst[Out, NextOut, That])(implicit innerBuilder: CanBuildFrom[_, NextOut, That], d: DummyImplicit): DepthFirst[In, NextOut, That] = {
    new DepthFirst[In, NextOut, That](values, Op.DlfFlatMap(f.asInstanceOf[Any => DepthFirst[Any, Any, That]]) :: ops)
  }

  def withFilter(f: Out => Boolean): DepthFirst[In, Out, This] = {
    new DepthFirst[In, Out, This](values, Op.Filter(f.asInstanceOf[Any => Boolean]) :: ops)
  }

}

object DepthFirst {

  def apply[In, This](values: TraversableOnce[In])(implicit innerBuilder: CanBuildFrom[_, In, This]): DepthFirst[In, In, This] =
    new DepthFirst[In, In, This](values, List())

  def run[In, Out, That](
    values: TraversableOnce[In],
    op: Op,
    ops: Op*
  )(implicit canBuildFrom: CanBuildFrom[_, Out, That]
  ): That = {
    val results = canBuildFrom()

    results ++= iterator(values, op, ops: _*)

    results.result()
  }


  def iterator[In, Out](
    values: TraversableOnce[In],
    op: Op,
    ops: Op*
  ): Iterator[Out] = {
    var stepResult = Result(List(op.toElem(ops.toList, values.toIterator)), None)

    new Iterator[Out] {
      var innerIterator: Iterator[Out] = Iterator()

      override def hasNext: Boolean = {
        while (!innerIterator.hasNext && !stepResult.isFinished) {
          stepResult = stepResult.step
          innerIterator = stepResult.valuesIterator
        }

        innerIterator.hasNext
      }

      override def next(): Out =
        innerIterator.next()
    }
  }

  private case class Result[Out](
    /*
    A benchmark between List and Vector showed that List is faster.
     */
    stack: List[Elem],
    maybeValues: Option[Result.Value[Out]]
  ) {
    def isFinished: Boolean = stack.isEmpty

    def valuesIterator: Iterator[Out] = maybeValues.map(_.toIterator).getOrElse(Iterator())

    def step: Result[Out] = {
      stack match {
        case head::ss =>
          val values = head.values

          if (values.hasNext) {
            val value = values.next()

            head match {
              case Elem.Filter(f, fs, _) =>
                if (f(value))
                  fs match {
                    case nextF::remainingFs =>
                      Result(
                        stack = nextF.toElem(remainingFs, Iterator(value))::stack,
                        maybeValues = None
                      )

                    case Nil =>
                      Result(
                        stack = stack,
                        maybeValues = Some(Result.Value.One(value.asInstanceOf[Out]))
                      )
                  } else {
                  Result(
                    stack = stack,
                    maybeValues = None
                  )
                }

              case Elem.FlatMap(f, fs, _) =>
                val fResults = f(value)

                fs match {
                  case nextF::remainingFs =>
                    Result(
                      stack = nextF.toElem(remainingFs, fResults.toIterator)::stack,
                      maybeValues = None
                    )

                  case Nil =>
                    Result(
                      stack = stack,
                      maybeValues = Some(Result.Value.Many(fResults.asInstanceOf[TraversableOnce[Out]]))
                    )
                }

              case Elem.DlfFlatMap(f, fs, _) =>
                val innerDlf = f(value)
                val fResults = innerDlf.iterator

                fs match {
                  case nextF::remainingFs =>
                    Result(
                      stack = nextF.toElem(remainingFs, fResults)::stack,
                      maybeValues = None
                    )

                  case Nil =>
                    Result(
                      stack = stack,
                      maybeValues = Some(Result.Value.Many(fResults.toTraversable.asInstanceOf[TraversableOnce[Out]]))
                    )
                }

              case Elem.Map(f, fs, _) =>
                val result = f(value)

                fs match {
                  case nextF::remainingFs =>
                    Result(
                      stack = nextF.toElem(remainingFs, Iterator(result))::stack,
                      maybeValues = None
                    )

                  case Nil =>
                    Result(
                      stack = stack,
                      maybeValues = Some(Result.Value.One(result.asInstanceOf[Out]))
                    )
                }

            }

          } else Result(stack = ss, maybeValues = None)

        case Nil =>
          Result(stack = Nil, maybeValues = None)
      }
    }
  }

  private object Result {
    sealed trait Value[Out] extends TraversableOnce[Out]

    object Value {
      case class One[Out](result: Out) extends Value[Out] {
        override def foreach[U](f: (Out) => U): Unit = f(result)

        override def isEmpty: Boolean = false

        override def hasDefiniteSize: Boolean = true

        override def seq: TraversableOnce[Out] = Seq(result)

        override def forall(p: (Out) => Boolean): Boolean = p(result)

        override def exists(p: (Out) => Boolean): Boolean = p(result)

        override def find(p: (Out) => Boolean): Option[Out] =
          Some(result).filter(p)

        override def copyToArray[B >: Out](xs: Array[B], start: Int, len: Int): Unit =
          if (len > 0)
            xs(start) = result

        override def toTraversable: Traversable[Out] = Traversable(result)

        override def isTraversableAgain: Boolean = true

        override def toStream: Stream[Out] = Stream(result)

        override def toIterator: Iterator[Out] = Iterator(result)
      }

      case class Many[Out](results: TraversableOnce[Out]) extends Value[Out] {
        override def foreach[U](f: (Out) => U): Unit = results.foreach(f)

        override def isEmpty: Boolean = results.isEmpty

        override def hasDefiniteSize: Boolean = results.hasDefiniteSize

        override def seq: TraversableOnce[Out] = results.seq

        override def forall(p: (Out) => Boolean): Boolean = results.forall(p)

        override def exists(p: (Out) => Boolean): Boolean = results.exists(p)

        override def find(p: (Out) => Boolean): Option[Out] = results.find(p)

        override def copyToArray[B >: Out](xs: Array[B], start: Int, len: Int): Unit = results.copyToArray[B](xs, start, len)

        override def toTraversable: Traversable[Out] = results.toTraversable

        override def isTraversableAgain: Boolean = results.isTraversableAgain

        override def toStream: Stream[Out] = results.toStream

        override def toIterator: Iterator[Out] = results.toIterator
      }
    }
  }

}
