package me.jeffshaw.dlf

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable

case class State[F[_], In, Out](
  op: Op,
  ops: Op*
)(implicit canBuildFrom: CanBuildFrom[_, Out, F[Out]]) {

  /*
  A benchmark between List and Vector showed that List is faster.
   */
  var stack: List[Elem] = Nil

  /*
  As we go through the stack, we lose type information.
   */
  val results = canBuildFrom().asInstanceOf[mutable.Builder[Any, F[Any]]]

  def step(): Unit = {
    stack match {
      case Elem.Filter(f, fs, Left(value))::ss =>
        if (f(value))
          fs match {
            case nextF::remainingFs =>
              stack = nextF.toElem(remainingFs, Iterator(value))::ss

            case Nil =>
              results += value
              stack = ss
          } else stack = ss

      case Elem.Filter(f, fs, Right(remaining))::ss =>
        if (remaining.hasNext)
          stack = Elem.Filter(f, fs, Left(remaining.next))::stack
        else stack = ss

      case Elem.FlatMap(f, fs, Left(value))::ss =>
        val fResults = f(value)

        fs match {
          case nextF::remainingFs =>
            stack = nextF.toElem(remainingFs, fResults.toIterator)::ss

          case Nil =>
            results ++= fResults
            stack = ss
        }

      case Elem.FlatMap(f, fs, Right(remaining))::ss =>
        if (remaining.hasNext)
          stack = Elem.FlatMap(f, fs, Left(remaining.next))::stack
        else stack = ss

      case Elem.Map(f, fs, Left(value))::ss =>
        val result = f(value)

        fs match {
          case nextF::remainingFs =>
            stack = nextF.toElem(remainingFs, Iterator(result))::ss

          case Nil =>
            results += result
            stack = ss
        }

      case Elem.Map(f, fs, Right(remaining))::ss =>
        if (remaining.hasNext)
          stack = Elem.Map(f, fs, Left(remaining.next))::stack
        else stack = ss

      case Nil =>

    }
  }

  def run(values: Iterable[In]): F[Out] = {
    stack = List(op.toElem(ops.toList, values.toIterator))

    while(stack.nonEmpty)
      step()

    results.result().asInstanceOf[F[Out]]
  }

}
