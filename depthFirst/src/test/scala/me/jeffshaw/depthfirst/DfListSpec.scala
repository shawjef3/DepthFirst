package me.jeffshaw.depthfirst

import org.scalacheck.Gen
import org.scalatest.FunSuite
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class DfListSpec extends FunSuite with GeneratorDrivenPropertyChecks {

  test("map same as scala.List") {
    forAll(Gen.listOf(Gen.oneOf(Gen.posNum[Int], Gen.negNum[Int]))) { list =>
      val dfList = DfList(list: _*)

      assertResult(list.map(_ + 1))(dfList.map(_ + 1).toList)
    }
  }

  test("flatMap same as scala.List") {
    forAll(Gen.listOf(Gen.oneOf(Gen.posNum[Int], Gen.negNum[Int]))) { list =>
      val dfList = DfList(list: _*)

      val actual = dfList.flatMap(x => DfList(x, x))
      val actualList = actual.toList
      val expected = list.flatMap(x => List(x, x))

      assertResult(expected)(actualList)
    }
  }

  test("filter same as scala.List") {
    forAll(Gen.listOf(Gen.oneOf(Gen.posNum[Int], Gen.negNum[Int]))) { list =>
      val dfList = DfList(list: _*)

      val actual = dfList.filter(x => x % 2 == 0)
      val actualList = actual.toList
      val expected = list.filter(x => x % 2 == 0)

      assertResult(expected)(actualList)
    }
  }

  test("builder works") {
    forAll(Gen.listOf(Gen.oneOf(Gen.posNum[Int], Gen.negNum[Int]))) { list =>
      val dfList = DfList(list: _*)

      assertResult(list)(dfList.toList)
    }
  }

  test("reverse") {
    forAll(Gen.listOf(Gen.oneOf(Gen.posNum[Int], Gen.negNum[Int]))) { list =>
      val dfList = DfList(list: _*)

      assertResult(list.reverse)(dfList.reverse.toList)
    }
  }

  test("++") {
    forAll(Gen.listOf(Gen.oneOf(Gen.posNum[Int], Gen.negNum[Int]))) { list0 =>
      forAll(Gen.listOf(Gen.oneOf(Gen.posNum[Int], Gen.negNum[Int]))) { list1 =>
        val dfList0 = DfList(list0: _*)
        val dfList0_ = dfList0.map(_ + 1)

        val list0_ = list0.map(_ + 1)

        val actual = dfList0_ ++ list1
        val expected = list0_ ++ list1

        assertResult(expected)(actual)
      }
    }
  }

  test("op applies to tail") {
    val l = DfList(1, 2, 3, 4)
    val mapped = l.flatMap(x => DfList(x + 1))

    val expected = DfList(3, 4, 5)
    val actual = mapped.tail

    assertResult(expected)(actual)
  }

  test("op applies to drop") {
    val l = DfList(1, 2, 3, 4)
    val mapped = l.flatMap(x => DfList(x + 1))

    val expected = DfList(4, 5)
    val actual = mapped.drop(2)

    assertResult(expected)(actual)
  }

  test("op applies to take") {
    val l = DfList(1, 2, 3, 4)
    val mapped = l.flatMap(x => DfList(x + 1))

    val expected = DfList(2, 3)
    val actual = mapped.take(2)

    assertResult(expected)(actual)
  }

}
