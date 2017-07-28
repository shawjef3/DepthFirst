package me.jeffshaw.dlf

import org.scalacheck.Gen
import org.scalatest.FunSuite
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class StateSpec extends FunSuite with GeneratorDrivenPropertyChecks {

  test("filter success at end") {
    forAll(Gen.containerOf[Vector, Int](Gen.chooseNum(Int.MinValue, Int.MaxValue, 0))) { values =>
      val expected =
        for {
          value <- values
          if value == 0
        } yield value

      val actual = State.run[Int, Int, Vector[Int]](values, Op.Filter(x => x == 0))

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

      val actual = State.run[Int, Int, Vector[Int]](values, Op.FlatMap((x: Any) => Vector(x, x.asInstanceOf[Int] + 1)))

      assertResult(expected)(actual)
    }
  }

  test("map success at end") {
    forAll(Gen.containerOf[Vector, Int](Gen.chooseNum(Int.MinValue, Int.MaxValue))) { values =>
      val expected =
        for {
          value <- values
        } yield value + 1

      val actual = State.run[Int, Int, Vector[Int]](values, Op.Map((x: Any) => x.asInstanceOf[Int] + 1))

      assertResult(expected)(actual)
    }
  }

  test("iterator") {
    val actual = State.iterator[Int, Int](Vector(1,2,3), Op.Map((x: Any) => x.asInstanceOf[Int] + 1)).toVector

    assertResult(Vector(2,3,4))(actual)
  }

}
