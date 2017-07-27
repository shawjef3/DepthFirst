package me.jeffshaw.dlf

import scala.collection.GenTraversable
import scala.collection.generic.{CanBuildFrom, HasNewBuilder}

class Dlf[In, Out, This] private (
  private val values: GenTraversable[In],
  private val ops: List[Op]
)(implicit val innerBuilder: CanBuildFrom[_, Out, This]
) {

  lazy val results: This = {
    val revOps = ops.reverse
    State.run[In, Out, This](values, revOps.head, revOps.tail: _*)
  }

  def map[NextOut, That](f: Out => NextOut)(implicit innerBuilder: CanBuildFrom[_, NextOut, That]): Dlf[In, NextOut, That] = {
    new Dlf[In, NextOut, That](values, Op.Map(f.asInstanceOf[Any => Any])::ops)
  }

  def flatMap[NextOut, That](f: Out => GenTraversable[NextOut])(implicit innerBuilder: CanBuildFrom[_, NextOut, That]): Dlf[In, NextOut, That] = {
    new Dlf[In, NextOut, That](values, Op.FlatMap(f.asInstanceOf[Any => GenTraversable[Any]])::ops)
  }

  def flatMap[NextOut, That](f: Out => Dlf[Out, NextOut, That])(implicit innerBuilder: CanBuildFrom[_, NextOut, That], d: DummyImplicit): Dlf[In, NextOut, That] = {
    new Dlf[In, NextOut, That](values, Op.DlfFlatMap(f.asInstanceOf[Any => Dlf[Any, Any, That]])::ops)
  }

  def withFilter(f: Out => Boolean): Dlf[In, Out, This] = {
    new Dlf[In, Out, This](values, Op.Filter(f.asInstanceOf[Any => Boolean]) :: ops)
  }

}

object Dlf {

  def apply[In, This](values: GenTraversable[In])(implicit innerBuilder: CanBuildFrom[_, In, This]): Dlf[In, In, This] =
    new Dlf[In, In, This](values, List())

}
