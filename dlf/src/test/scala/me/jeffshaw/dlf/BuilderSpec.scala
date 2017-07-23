package me.jeffshaw.dlf

import org.scalatest.FunSuite

class BuilderSpec extends FunSuite {

  test("works") {
    val x = for {
      x <- Builder[Vector, Int, Set, Int](Vector.tabulate(10)(identity) ++ Vector.tabulate(10)(identity))
      if x % 2 == 0
    } yield x+1

    assertResult(Set(1,3,5,7,9))(x.run())
  }

}
