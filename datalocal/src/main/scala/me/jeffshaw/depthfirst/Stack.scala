package me.jeffshaw.depthfirst

private class Stack[Out] private (
  /*
  A benchmark between List and Vector showed that List is faster.
   */
  private var stack: List[Elem]
) {
  private var maybeValues: Option[Stack.Value[Out]] = None

  def isFinished: Boolean = stack.isEmpty

  def valuesIterator: Iterator[Out] = maybeValues.map(_.toIterator).getOrElse(Iterator())

  def step(): Unit = {
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
                    stack = nextF.toElem(remainingFs, Iterator(value))::stack
                    maybeValues = None

                  case Nil =>
                    maybeValues = Some(Stack.Value.One(value.asInstanceOf[Out]))
                } else {
                maybeValues = None
              }

            case Elem.FlatMap(f, fs, _) =>
              val fResults = f(value)

              fs match {
                case nextF::remainingFs =>
                  stack = nextF.toElem(remainingFs, fResults.toIterator)::stack
                  maybeValues = None

                case Nil =>
                  maybeValues = Some(Stack.Value.Many(fResults.asInstanceOf[TraversableOnce[Out]]))
              }

            case Elem.DlfFlatMap(f, fs, _) =>
              val innerDlf = f(value)
              val fResults = innerDlf.toIterator

              fs match {
                case nextF::remainingFs =>
                  stack = nextF.toElem(remainingFs, fResults)::stack
                  maybeValues = None

                case Nil =>
                  maybeValues = Some(Stack.Value.Many(fResults.toTraversable.asInstanceOf[TraversableOnce[Out]]))
              }

            case Elem.Map(f, fs, _) =>
              val result = f(value)

              fs match {
                case nextF::remainingFs =>
                  stack = nextF.toElem(remainingFs, Iterator(result))::stack
                  maybeValues = None

                case Nil =>
                  maybeValues = Some(Stack.Value.One(result.asInstanceOf[Out]))
              }

          }

        } else {
          stack = ss
          maybeValues = None
        }

      case Nil =>
        maybeValues = None
    }
  }
}

private object Stack {
  def apply[Out](elem: Elem): Stack[Out] = {
    new Stack(List(elem))
  }

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
