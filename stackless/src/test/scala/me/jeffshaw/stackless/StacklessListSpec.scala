package me.jeffshaw.stackless

import org.scalacheck.Gen
import org.scalatest.FunSuite
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class StacklessListSpec extends FunSuite with GeneratorDrivenPropertyChecks {

  test("map same as scala.List") {
    forAll(Gen.listOf(Gen.oneOf(Gen.posNum[Int], Gen.negNum[Int]))) { list =>
      val dfList = StacklessList(list: _*)

      assertResult(list.map(_ + 1))(dfList.map(_ + 1).toList)
    }
  }

  test("flatMap same as scala.List") {
    forAll(Gen.listOf(Gen.oneOf(Gen.posNum[Int], Gen.negNum[Int]))) { list =>
      val dfList = StacklessList(list: _*)

      val actual = dfList.flatMap(x => StacklessList(x, x))
      val actualList = actual.toList
      val expected = list.flatMap(x => List(x, x))

      assertResult(expected)(actualList)
    }
  }

  test("filter same as scala.List") {
    forAll(Gen.listOf(Gen.oneOf(Gen.posNum[Int], Gen.negNum[Int]))) { list =>
      val dfList = StacklessList(list: _*)

      val actual = dfList.filter(x => x % 2 == 0)
      val actualList = actual.toList
      val expected = list.filter(x => x % 2 == 0)

      assertResult(expected)(actualList)
    }
  }

  test("builder works") {
    forAll(Gen.listOf(Gen.oneOf(Gen.posNum[Int], Gen.negNum[Int]))) { list =>
      val dfList = StacklessList(list: _*)

      assertResult(list)(dfList.toList)
    }
  }

  test("reverse") {
    forAll(Gen.listOf(Gen.oneOf(Gen.posNum[Int], Gen.negNum[Int]))) { list =>
      val dfList = StacklessList(list: _*)

      assertResult(list.reverse)(dfList.reverse.toList)
    }
  }

  test("reverse doesn't apply ops to new tail") {
    val l = StacklessList(1, 2, 3).flatMap(elem => StacklessList(elem + 1)) :+ 3 //the last elem has no ops

    assertResult(List(3, 4, 3, 2))(l.reverse.toList)
  }

  test("++") {
    forAll(Gen.listOf(Gen.oneOf(Gen.posNum[Int], Gen.negNum[Int]))) { list0 =>
      forAll(Gen.listOf(Gen.oneOf(Gen.posNum[Int], Gen.negNum[Int]))) { list1 =>
        val dfList0 = StacklessList(list0: _*)
        val dfList0_ = dfList0.map(_ + 1)

        val list0_ = list0.map(_ + 1)

        val actual = dfList0_ ++ list1
        val expected = list0_ ++ list1

        assertResult(StacklessList(expected: _*))(actual)
      }
    }
  }

  test("op applies to tail") {
    val l = StacklessList(1, 2, 3, 4)
    val mapped = l.flatMap(x => StacklessList(x + 1))

    val expected = StacklessList(3, 4, 5)
    val actual = mapped.tail

    assertResult(expected)(actual)
  }

  test("op applies to drop") {
    val l = StacklessList(1, 2, 3, 4)
    val mapped = l.flatMap(x => StacklessList(x + 1))

    val expected = StacklessList(4, 5)
    val actual = mapped.drop(2)

    assertResult(expected)(actual)
  }

  test("op applies to take") {
    val l = StacklessList(1, 2, 3, 4)
    val mapped = l.flatMap(x => StacklessList(x + 1))

    val expected = StacklessList(2, 3)
    val actual = mapped.take(2)

    assertResult(expected)(actual)
  }

  test("map happens once") {
    val l = StacklessList(1)
    var runs = 0
    val mapped = l.map { x =>
      runs += 1
      x
    }

    for (m <- mapped) m

    assertResult(1)(runs)
  }

  test("flatMap happens once") {
    val l = StacklessList(1)
    var runs = 0
    val mapped = l.flatMap { x =>
      runs += 1
      StacklessList(x)
    }

    for (m <- mapped) m

    assertResult(1)(runs)
  }

  test("map does not mutate") {
    val l = StacklessList(1, 2, 3).map(x => x + 1).drop(2)

    assertResult(StacklessList(4))(l)
  }

  test("flatMap does not mutate") {
    val l = StacklessList(1, 2).flatMap(x => StacklessList(x + 1))
    val l2 = l.tail

    assertResult(StacklessList(2, 3))(l)
    assertResult(StacklessList(3))(l2)
  }

  test("equals") {
    val l0 = StacklessList(1,2,3)
    val l1 = StacklessList(0,1,2).flatMap(x => StacklessList(x + 1))

    assertResult(l0)(l1)
  }

}
