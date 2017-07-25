package me.jeffshaw.dlf

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable

object State {
  def run[F[_], In, Out](
    values: Iterable[In],
    op: Op,
    ops: Op*
  )(implicit canBuildFrom: CanBuildFrom[_, Out, F[Out]]
  ): F[Out] = {
    /*
    A benchmark between List and Vector showed that List is faster.
     */
    var stack = List(op.toElem(ops.toList, values.toIterator))

    /*
    As we go through the stack, we lose type information.
     */
    val results = canBuildFrom().asInstanceOf[mutable.Builder[Any, F[Any]]]

    while (stack.nonEmpty)
      stack = step(stack, results)

    results.result().asInstanceOf[F[Out]]
  }

  def step[F[_]](stack: List[Elem], results: mutable.Builder[Any, F[Any]]): List[Elem] = {
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
                  results ++= fResults
                  stack
              }

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
