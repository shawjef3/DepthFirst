package me.jeffshaw.dlf

import org.scalatest.FunSuite

class DlfSpec extends FunSuite {

  test("works") {
    val odds: Dlf[Vector, Int, Stream, Int] = for {
      x <- Dlf(Vector.tabulate(10)(identity) ++ Vector.tabulate(10)(identity))
      if x % 2 == 0
      even <- Set(x)
      even_ <- Stream(even)
    } yield even_ + 1

    assert(odds.results.isInstanceOf[Stream[_]])

    assertResult(Stream(1,3,5,7,9))(odds.results)
  }

}
