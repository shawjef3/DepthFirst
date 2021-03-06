package me.jeffshaw.depthfirst

import org.scalacheck.Gen
import org.scalatest.FunSuite
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class DepthFirstSpec extends FunSuite with GeneratorDrivenPropertyChecks {

  test("works") {
    val odds = for {
      x <- DepthFirst(Vector.tabulate(10)(identity) ++ Vector.tabulate(10)(identity))
      if x % 2 == 0
      even <- Set(x)
      even_ <- Stream(even)
    } yield even_ + 1

    assertResult(Vector(1,3,5,7,9,1,3,5,7,9))(odds.toVector)
  }

  test("inner works") {

    //Fully expanded version of `actual`.
    val actual_ =
      DepthFirst[Int](
        Vector.tabulate(10)(identity)
      ).withFilter(
        x => x % 2 == 0
      ).flatMap[Int](
        (x: Int) =>
          DepthFirst[Int](
            0 to x
          ).map[Int](identity)
      )

    val actual = for {
      x <- DepthFirst(Vector.tabulate(10)(identity))
      if x % 2 == 0
      toX <- DepthFirst(0 to x)
    } yield toX

    val expected =
      for {
        x <- Vector.tabulate(10)(identity)
        if x % 2 == 0
        toX <- 0 to x
      } yield toX

    assertResult(expected)(actual.toVector)
  }


  test("filter success at end") {
    forAll(Gen.containerOf[Vector, Int](Gen.chooseNum(Int.MinValue, Int.MaxValue, 0))) { values =>
      val expected =
        for {
          value <- values
          if value == 0
        } yield value

      val actual = DepthFirst.iterator[Int, Int](values, DepthFirstList(Op.Filter(x => x == 0))).toVector

      assertResult(expected)(actual)
    }
  }

  test("flatMap success at end") {
    forAll(Gen.containerOf[Vector, Int](Gen.chooseNum(Int.MinValue, Int.MaxValue))) { values =>
      val expected =
        for {
          value <- values
          value <- Vector(value, value + 1)
        } yield value

      val actual = DepthFirst.iterator[Int, Int](values, DepthFirstList(Op.FlatMap((x: Any) => Vector(x, x.asInstanceOf[Int] + 1)))).toVector

      assertResult(expected)(actual)
    }
  }

  test("map success at end") {
    forAll(Gen.containerOf[Vector, Int](Gen.chooseNum(Int.MinValue, Int.MaxValue))) { values =>
      val expected =
        for {
          value <- values
        } yield value + 1

      val actual = DepthFirst.iterator[Int, Int](values, DepthFirstList(Op.Map((x: Any) => x.asInstanceOf[Int] + 1))).toVector

      assertResult(expected)(actual)
    }
  }

}
