package me.jeffshaw.dlf

import scala.collection.generic.CanBuildFrom

class Dlf[In, G[_] <: Iterable[_], Out] private (
  private val values: Iterable[In],
  private val ops: List[Op]
)(implicit innerBuilder: CanBuildFrom[_, Out, G[Out]]
) {

  lazy val results: G[Out] = {
    val revOps = ops.reverse
    State.run[G, In, Out](values.asInstanceOf[Iterable[In]], revOps.head, revOps.tail: _*)
  }

  def map[NextOut](f: Out => NextOut)(implicit innerBuilder: CanBuildFrom[_, NextOut, G[NextOut]]): Dlf[In, G, NextOut] = {
    new Dlf[In, G, NextOut](values, Op.Map(f.asInstanceOf[Any => Any]) :: ops)
  }

  def flatMap[G0[_] <: Iterable[_], NextOut](f: Dlf.FlatMappable[Out, G0, NextOut])(implicit canBuildFrom: CanBuildFrom[_, NextOut, G[NextOut]]): Dlf[In, G, NextOut] = {
    f match {
      case Dlf.FlatMappable.D(f) =>
        new Dlf[In, G, NextOut](values, Op.DlfFlatMap(f.asInstanceOf[Any => Dlf[Any, Iterable, Any]]) :: ops)
      case Dlf.FlatMappable.F(f) =>
        new Dlf[In, G, NextOut](values, Op.FlatMap(f.asInstanceOf[Any => Iterable[Any]]) :: ops)
    }
  }

  def withFilter(f: Out => Boolean): Dlf[In, G, Out] = {
    new Dlf[In, G, Out](values, Op.Filter(f.asInstanceOf[Any => Boolean]) :: ops)
  }

}

object Dlf {

  def apply[F[_] <: Iterable[_], In](values: F[In])(implicit innerBuilder: CanBuildFrom[_, In, F[In]]): Dlf[In, F, In] =
    new Dlf[In, F, In](values.toIterable.asInstanceOf[Iterable[In]], List())

  sealed trait FlatMappable[Out, G[_] <: Iterable[_], NextOut]

  object FlatMappable {
    case class D[Out, G[_] <: Iterable[_], NextOut](f: Out => Dlf[Out, G, NextOut]) extends FlatMappable[Out, G, NextOut]

    case class F[Out, G[_] <: Iterable[_], NextOut](f: Out => G[NextOut]) extends FlatMappable[Out, G, NextOut]

    implicit def toD[Out, G[_] <: Iterable[_], NextOut](f: Out => Dlf[Out, G, NextOut]): FlatMappable[Out, G, NextOut] =
      D(f)

    implicit def toF[Out, G0[_] <: Iterable[_], NextOut](f: Out => G0[NextOut]): FlatMappable[Out, G0, NextOut] =
      F(f)

  }



}
