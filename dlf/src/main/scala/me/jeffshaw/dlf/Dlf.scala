package me.jeffshaw.dlf

import scala.collection.generic.CanBuildFrom

class Dlf[F[_] <: Iterable[_], In, Out] private (
  private val values: F[In],
  private val ops: List[Op]
)(implicit innerBuilder: CanBuildFrom[_, Out, F[Out]]
) {

  lazy val results: F[Out] = {
    val revOps = ops.reverse
    State.run[F, In, Out](values.asInstanceOf[Iterable[In]], revOps.head, revOps.tail: _*)
  }

  def map[NextOut](f: Out => NextOut)(implicit innerBuilder: CanBuildFrom[_, NextOut, F[NextOut]]): Dlf[F, In, NextOut] = {
    new Dlf[F, In, NextOut](values, Op.Map(f.asInstanceOf[Any => Any]) :: ops)
  }

//  def flatMap[G[_] <: Iterable[_], NextOut](f: Out => Dlf[G, Out, NextOut])(implicit canBuildFrom: CanBuildFrom[_, NextOut, G[NextOut]]): Dlf[G, In, NextOut] = {
//    new Dlf[G, In, NextOut](values, Op.FlatMap(f.asInstanceOf[Any => Iterable[Any]]) :: ops)
//  }

  def flatMap[NextOut](f: Out => F[NextOut])(implicit canBuildFrom: CanBuildFrom[_, NextOut, F[NextOut]]): Dlf[F, In, NextOut] = {
    new Dlf[F, In, NextOut](values, Op.FlatMap(f.asInstanceOf[Any => Iterable[Any]]) :: ops)
  }

  def withFilter(f: Out => Boolean): Dlf[F, In, Out] = {
    new Dlf[F, In, Out](values, Op.Filter(f.asInstanceOf[Any => Boolean]) :: ops)
  }

}

object Dlf {

  def apply[F[_] <: Iterable[_], In](values: F[In])(implicit innerBuilder: CanBuildFrom[_, In, F[In]]): Dlf[F, In, In] =
    new Dlf[F, In, In](values, List())

}
