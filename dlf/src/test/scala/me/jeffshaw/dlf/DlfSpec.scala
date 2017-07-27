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

    println(odds.results.getClass.getCanonicalName)

    assertResult(Vector(1,3,5,7,9,1,3,5,7,9))(odds.results)
  }

  test("inner works") {

    //Fully expanded version of `odds`.
    val odds_ =
      Dlf[Int, Vector[Int]](
        Vector.tabulate(10)(identity) ++ Vector.tabulate(10)(identity)
      ).withFilter(
        x => x % 2 == 0
      ).flatMap[Int, Stream[Int]](
        (x: Int) =>
          Dlf[Int, Set[Int]](
            Set(x)
          ).flatMap[Int, Stream[Int]](
            (even: Int) => Dlf[Int, Stream[Int]](Stream(even)).map[Int, Stream[Int]](
              (even_ : Int) => even_ + 1
            )
          )
      )

    val odds = for {
      x <- Dlf(Vector.tabulate(10)(identity) ++ Vector.tabulate(10)(identity))
      if x % 2 == 0
      even <- Dlf(Set(x))
      even_ <- Dlf(Stream(even))
    } yield even_ + 1

    assertResult(Set(1,3,5,7,9))(odds.results)
  }

}
