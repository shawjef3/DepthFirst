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
                val fResults = innerDlf.toIterator

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
