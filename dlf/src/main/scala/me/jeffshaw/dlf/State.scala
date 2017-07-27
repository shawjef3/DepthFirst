package me.jeffshaw.dlf

import scala.collection.generic.CanBuildFrom
import scala.collection.{GenTraversable, mutable}

object State {
  def run[In, Out, That](
    values: GenTraversable[In],
    op: Op,
    ops: Op*
  )(implicit canBuildFrom: CanBuildFrom[_, Out, That]
  ): That = {
    /*
    A benchmark between List and Vector showed that List is faster.
     */
    var stack = List(op.toElem(ops.toList, values.toIterator))

    /*
    As we go through the stack, we lose type information.
     */
    val results = canBuildFrom()

    while (stack.nonEmpty)
      stack = step(stack, results.asInstanceOf[mutable.Builder[Any, That]])

    results.result()
  }

  def step[That](stack: List[Elem], results: mutable.Builder[Any, That]): List[Elem] = {
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
                    nextF.toElem(remainingFs, Iterator(value))::stack

                  case Nil =>
                    results += value
                    stack
                } else stack

            case Elem.FlatMap(f, fs, _) =>
              val fResults = f(value)

              fs match {
                case nextF::remainingFs =>
                  nextF.toElem(remainingFs, fResults.toIterator)::stack

                case Nil =>
                  results ++= fResults.toIterator
                  stack
              }

            case Elem.DlfFlatMap(f, innerOps, innerValues) =>
              //todo: make the stack List[List[Elem]]
              ???

            case Elem.Map(f, fs, _) =>
              val result = f(value)

              fs match {
                case nextF::remainingFs =>
                  nextF.toElem(remainingFs, Iterator(result))::stack

                case Nil =>
                  results += result
                  stack
              }

          }

        } else ss

      case Nil =>
        Nil
    }
  }

  def stream[In, Out](
    values: Iterable[In],
    op: Op,
    ops: Op*
  ): Stream[Out] = {
    var stack = List(op.toElem(ops.toList, values.toIterator))

    def step(): Stream[Any] =
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
                      Stream()
                    case Nil =>
                      Stream(value)
                  }
                else Stream()

              case Elem.FlatMap(f, fs, _) =>
                val fResults = f(value)

                fs match {
                  case nextF::remainingFs =>
                    stack = nextF.toElem(remainingFs, fResults.toIterator)::stack
                    Stream()
                  case Nil =>
                    fResults.toStream
                }

              case Elem.DlfFlatMap(f, innerOps, innerValues) =>
                //todo: make the stack List[List[Elem]]
                ???

              case Elem.Map(f, fs, _) =>
                val result = f(value)

                fs match {
                  case nextF::remainingFs =>
                    stack = nextF.toElem(remainingFs, Iterator(result))::stack
                    Stream()
                  case Nil =>
                    Stream(result)
                }

            }

          } else {
            stack = ss
            Stream()
          }

        case Nil =>
          Stream()
      }

    Stream.continually(step()).takeWhile(_ => stack.nonEmpty).flatten.asInstanceOf[Stream[Out]]
  }
}
