package me.jeffshaw.dlf

import scala.collection.generic.CanBuildFrom

class Dlf[F[_] <: Iterable[_], In, G[_] <: Iterable[_], Out] private (
  private val values: F[In],
  private val ops: List[Op]
)(implicit innerBuilder: CanBuildFrom[_, Out, G[Out]]
) {

  lazy val results: G[Out] = {
    val revOps = ops.reverse
    State.run[G, In, Out](values.asInstanceOf[Iterable[In]], revOps.head, revOps.tail: _*)
  }

  def map[NextOut](f: Out => NextOut)(implicit innerBuilder: CanBuildFrom[_, NextOut, G[NextOut]]): Dlf[F, In, G, NextOut] = {
    new Dlf[F, In, G, NextOut](values, Op.Map(f.asInstanceOf[Any => Any]) :: ops)
  }

  def flatMap[G0[_] <: Iterable[_], NextOut](f: Dlf.FlatMappable[G, Out, G0, NextOut])(implicit canBuildFrom: CanBuildFrom[_, NextOut, G0[NextOut]]): Dlf[F, In, G0, NextOut] = {
    f match {
      case Dlf.FlatMappable.D(f) =>
        new Dlf[F, In, G0, NextOut](values, Op.DlfFlatMap(f.asInstanceOf[Any => Dlf[Iterable, Any, Iterable, Any]]) :: ops)
      case Dlf.FlatMappable.F(f) =>
        new Dlf[F, In, G0, NextOut](values, Op.FlatMap(f.asInstanceOf[Any => Iterable[Any]]) :: ops)
    }
  }

  def withFilter(f: Out => Boolean): Dlf[F, In, G, Out] = {
    new Dlf[F, In, G, Out](values, Op.Filter(f.asInstanceOf[Any => Boolean]) :: ops)
  }

}

object Dlf {

  def apply[F[_] <: Iterable[_], In](values: F[In])(implicit innerBuilder: CanBuildFrom[_, In, F[In]]): Dlf[F, In, F, In] =
    new Dlf[F, In, F, In](values, List())

  sealed trait FlatMappable[G[_] <: Iterable[_], Out, G0[_] <: Iterable[_], NextOut]

  object FlatMappable {
    case class D[G[_] <: Iterable[_], Out, G0[_] <: Iterable[_], NextOut](f: Out => Dlf[G, Out, G0, NextOut]) extends FlatMappable[G, Out, G0, NextOut]

    case class F[G[_] <: Iterable[_], Out, G0[_] <: Iterable[_], NextOut](f: Out => G0[NextOut]) extends FlatMappable[G, Out, G0, NextOut]

    implicit def toD[F[_] <: Iterable[_], Out, G[_] <: Iterable[_], NextOut](f: Out => Dlf[F, Out, G, NextOut]): FlatMappable[F, Out, G, NextOut] =
      D(f)

    implicit def toF[G[_] <: Iterable[_], Out, G0[_] <: Iterable[_], NextOut](f: Out => G0[NextOut]): FlatMappable[G, Out, G0, NextOut] =
      F(f)

  }

}
