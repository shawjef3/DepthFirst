package me.jeffshaw.dlf

import org.scalatest.FunSuite

class DlfSpec extends FunSuite {

  test("works") {
    val odds = for {
      x <- Dlf(Vector.tabulate(10)(identity) ++ Vector.tabulate(10)(identity))
      if x % 2 == 0
      even <- Set(x)
      even_ <- Stream(even)
    } yield even_ + 1

    assertResult(Vector(1,3,5,7,9,1,3,5,7,9))(odds.results)
  }

  test("inner works") {

    //Fully expanded version of `actual`.
    val actual_ =
      Dlf[Int, Vector[Int]](
        Vector.tabulate(10)(identity)
      ).withFilter(
        x => x % 2 == 0
      ).flatMap[Int, Vector[Int]](
        (x: Int) =>
          Dlf[Int, Set[Int]](
            0 to x
          ).map[Int, Vector[Int]](identity)
      )

    val actual = for {
      x <- Dlf(Vector.tabulate(10)(identity))
      if x % 2 == 0
      toX <- Dlf(0 to x)
    } yield toX

    val expected =
      for {
        x <- Vector.tabulate(10)(identity)
        if x % 2 == 0
        toX <- 0 to x
      } yield toX

    assertResult(expected)(actual.results)
  }

}
