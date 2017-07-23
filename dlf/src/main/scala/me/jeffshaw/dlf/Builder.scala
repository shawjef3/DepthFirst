package me.jeffshaw.dlf

import scala.collection.generic.CanBuildFrom

case class Builder[F0[_] <: Iterable[_], In, F1[_] <: Iterable[_], Out](values: F0[In], ops: List[Op] = List()) {

  def map[F2[_] <: Iterable[_], NextOut](f: Out => NextOut)(implicit canBuildFrom: CanBuildFrom[F1[Out], NextOut, F2[NextOut]]): Builder[F0, In, F2, NextOut] = {
    Builder[F0, In, F2, NextOut](values, Op.Map(f.asInstanceOf[Any => Any])::ops)
  }

  def flatMap[F2[_] <: Iterable[_], NextOut](f: Out => F2[NextOut])(implicit canBuildFrom: CanBuildFrom[F1[Out], NextOut, F2[NextOut]]): Builder[F0, In, F2, NextOut] = {
    Builder[F0, In, F2, NextOut](values, Op.FlatMap(f.asInstanceOf[Any => Iterable[Any]])::ops)
  }

  def withFilter[F2[_] <: Iterable[_]](f: Out => Boolean)(implicit canBuildFrom: CanBuildFrom[F1[Out], Out, F2[Out]]): Builder[F0, In, F2, In] = {
    Builder[F0, In, F2, In](values, Op.Filter(f.asInstanceOf[Any => Boolean])::ops)
  }

  def build()(implicit canBuildFrom: CanBuildFrom[_, Out, F1[Out]]): State[F1, In, Out] = {
    val revOps = ops.reverse
    State[F1, In, Out](revOps.head, revOps.tail: _*)
  }

  def run()(implicit canBuildFrom: CanBuildFrom[_, Out, F1[Out]]): F1[Out] = {
    build.run(values.asInstanceOf[Iterable[In]])
  }

}
