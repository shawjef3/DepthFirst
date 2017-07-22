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
      case Elem.Filter(f, fs, values)::ss =>
        if (values.hasNext) {
          val value = values.next()
          if (f(value))
            fs match {
              case nextF::remainingFs =>
                stack = nextF.toElem(remainingFs, Iterator(value))::stack

              case Nil =>
                results += value
            }
        } else stack = ss

      case Elem.FlatMap(f, fs, values)::ss =>
        if (values.hasNext) {
          val value = values.next()
          val fResults = f(value)

          fs match {
            case nextF::remainingFs =>
              stack = nextF.toElem(remainingFs, fResults.toIterator)::stack

            case Nil =>
              results ++= fResults
          }
        } else stack = ss

      case Elem.Map(f, fs, values)::ss =>
        if (values.hasNext) {
          val value = values.next()
          val result = f(value)

          fs match {
            case nextF::remainingFs =>
              stack = nextF.toElem(remainingFs, Iterator(result))::stack

            case Nil =>
              results += result
          }
        } else stack = ss

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
