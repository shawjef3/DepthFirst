package me.jeffshaw.dlf

import scala.collection.generic.CanBuildFrom

class Dlf[F0[_] <: Iterable[_], In, F1[_] <: Iterable[_], Out] private (
  private val values: F0[In],
  private val ops: List[Op]
)(implicit innerBuilder: CanBuildFrom[_, Out, F1[Out]]
) {

  private def build(): State[F1, In, Out] = {
    val revOps = ops.reverse
    State[F1, In, Out](revOps.head, revOps.tail: _*)
  }

  lazy val results: F1[Out] = {
    build().run(values.asInstanceOf[Iterable[In]])
  }

  def map[F2[_] <: Iterable[_], NextOut](f: Out => NextOut)(implicit canBuildFrom: CanBuildFrom[_, NextOut, F2[NextOut]]): Dlf[F0, In, F2, NextOut] = {
    new Dlf[F0, In, F2, NextOut](values, Op.Map(f.asInstanceOf[Any => Any]) :: ops)
  }

  def flatMap[F2[_] <: Iterable[_], NextOut](f: Out => F2[NextOut])(implicit canBuildFrom: CanBuildFrom[_, NextOut, F2[NextOut]]): Dlf[F0, In, F2, NextOut] = {
    new Dlf[F0, In, F2, NextOut](values, Op.FlatMap(f.asInstanceOf[Any => Iterable[Any]]) :: ops)
  }

  def withFilter[F2[_] <: Iterable[_]](f: Out => Boolean)(implicit canBuildFrom: CanBuildFrom[_, Out, F2[Out]]): Dlf[F0, In, F2, Out] = {
    new Dlf[F0, In, F2, Out](values, Op.Filter(f.asInstanceOf[Any => Boolean]) :: ops)
  }

}

object Dlf {

  def apply[F0[_] <: Iterable[_], In](values: F0[In])(implicit innerBuilder: CanBuildFrom[_, In, F0[In]]): Dlf[F0, In, F0, In] =
    new Dlf[F0, In, F0, In](values, List())

}
