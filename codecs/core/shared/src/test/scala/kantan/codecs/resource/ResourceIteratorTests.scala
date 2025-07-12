/*
 * Copyright 2015 Nicolas Rinaudo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kantan.codecs.resource

import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalatest.EitherValues.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

@SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.While"))
class ResourceIteratorTests
    extends AnyFunSuite with ScalaCheckPropertyChecks with Matchers with VersionSpecificResourceIteratorTests {
  // - Tools -----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  case class FailingIterator[A](it: Iterator[A], index: Int) {
    def resourceIterator: ResourceIterator[A] =
      new ResourceIterator[A] {
        var i = 0

        def checkFail(): Unit = {
          if(i == index) sys.error("failed")
          i += 1
        }

        override def readNext(): A = {
          checkFail()
          it.next()
        }

        override def checkNext: Boolean = {
          checkFail()
          it.hasNext
        }
        override def release() =
          ()
      }
  }

  implicit def arbFailingIterator[A: Arbitrary]: Arbitrary[FailingIterator[A]] =
    Arbitrary {
      for {
        as <- Gen.nonEmptyListOf(implicitly[Arbitrary[A]].arbitrary)
        index <- Gen.choose(0, 2 * (as.length - 1))
      } yield FailingIterator(as.iterator, index)
    }

  def closedWhenEmpty[A](r: ResourceIterator[A]): Boolean = {
    var closed = false
    val r2 = r.withClose(() => closed = true)
    while(r2.hasNext) r2.next()
    closed
  }

  // - Tests -----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  test("the empty resource iterator should have its close method called") {
    closedWhenEmpty(ResourceIterator.empty) should be(true)
  }

  test("drop should drop the expected number of elements") {
    forAll { (is: List[Int], n: Int) =>
      ResourceIterator(is*).drop(n).toList should be(is.drop(n))
    }
  }

  test("safe 'dropped' iterators should close properly and not leak exceptions") {
    forAll { (is: FailingIterator[Int], n: Int) =>
      closedWhenEmpty(is.resourceIterator.drop(n).safe(new Throwable("eos"))(identity)) should be(true)
    }
  }

  test("'dropWhiled' iterators should have their close method called when empty") {
    forAll { (is: List[Int], f: Int => Boolean) =>
      closedWhenEmpty(ResourceIterator(is*).dropWhile(f)) should be(true)
    }
  }

  test("safe 'dropWhiled' iterators should close properly and not leak exceptions") {
    forAll { (is: FailingIterator[Int], f: Int => Boolean) =>
      closedWhenEmpty(is.resourceIterator.dropWhile(f).safe(new Throwable("eos"))(identity)) should be(true)
    }
  }

  test("'dropped' iterators should have their close method called when empty") {
    forAll { (is: List[Int], n: Int) =>
      closedWhenEmpty(ResourceIterator(is*).drop(n)) should be(true)
    }
  }

  test("dropWhile should drop the expected elements") {
    forAll { (is: List[Int], f: Int => Boolean) =>
      ResourceIterator(is*).dropWhile(f).toList should be(is.dropWhile(f))
    }
  }

  test("takeWhile should take the expected elements") {
    forAll { (is: List[Int], f: Int => Boolean) =>
      ResourceIterator(is*).takeWhile(f).toList should be(is.takeWhile(f))
    }
  }

  test("'takeWhiled' iterators should have their close method called when empty") {
    forAll { (is: List[Int], f: Int => Boolean) =>
      closedWhenEmpty(ResourceIterator(is*).takeWhile(f)) should be(true)
    }
  }

  test("safe 'takeWhiled' iterators should close properly and not leak exceptions") {
    forAll { (is: FailingIterator[Int], f: Int => Boolean) =>
      closedWhenEmpty(is.resourceIterator.takeWhile(f).safe(new Throwable("eos"))(identity)) should be(true)
    }
  }

  test("take should take the expected number of elements") {
    forAll { (is: List[Int], n: Int) =>
      ResourceIterator(is*).take(n).toList should be(is.take(n))
    }
  }

  test("'taken' iterators should have their close method called when empty") {
    forAll { (is: List[Int], n: Int) =>
      closedWhenEmpty(ResourceIterator(is*).take(n)) should be(true)
    }
  }

  test("safe 'taken' iterators should close properly and not leak exceptions") {
    forAll { (is: FailingIterator[Int], n: Int) =>
      closedWhenEmpty(is.resourceIterator.take(n).safe(new Throwable("eos"))(identity)) should be(true)
    }
  }

  test("iterators created by take should fail when too many elements are requested") {
    forAll { (is: List[Int], n: Int) =>
      val res = ResourceIterator(is*).take(n)
      while(res.hasNext) res.next()
      intercept[NoSuchElementException](res.next())
      ()
    }
  }

  test("collect should behave as expected") {
    forAll { (is: List[Int], f: Int => Option[String]) =>
      val partial = Function.unlift(f)
      ResourceIterator(is*).collect(partial).toList should be(is.collect(partial))
    }
  }

  test("iterators created by collect should fail when too many elements are requested") {
    forAll { (is: List[Int], f: Int => Option[String]) =>
      val res = ResourceIterator(is*).collect(Function.unlift(f))
      while(res.hasNext) res.next()
      intercept[NoSuchElementException](res.next())
      ()
    }
  }

  test("map should behave as expected") {
    forAll { (is: List[Int], f: Int => String) =>
      ResourceIterator(is*).map(f).toList should be(is.map(f))
    }
  }

  test("mapResult should behave as expected") {
    forAll { (is: List[Either[Boolean, Int]], f: Int => String) =>
      ResourceIterator(is*).mapResult(f).toList should be(is.map(_.map(f)))
    }
  }

  test("flatMap should behave as expected") {
    forAll { (is: List[Int], f: Int => List[String]) =>
      ResourceIterator(is*).flatMap(f.andThen(ss => ResourceIterator(ss*))).toList should be(is.flatMap(f))
    }
  }

  test("emap should behave as expected") {
    forAll { (is: List[Either[Boolean, Int]], f: Int => Either[Boolean, String]) =>
      ResourceIterator(is*).emap(f).toList should be(is.map(_.flatMap(f)))
    }
  }

  test("filter should behave as expected") {
    forAll { (is: List[Int], f: Int => Boolean) =>
      ResourceIterator(is*).filter(f).toList should be(is.filter(f))
    }
  }

  test("withFilter should behave as expected") {
    forAll { (is: List[Int], f: Int => Boolean) =>
      ResourceIterator(is*).withFilter(f).toList should be(is.iterator.withFilter(f).toList)
    }
  }

  test("filterResult should behave as expected") {
    forAll { (is: List[Either[Boolean, Int]], f: Int => Boolean) =>
      ResourceIterator(is*).filterResult(f).toList should be(is.filter(_.exists(f)))
    }
  }

  test("filtered iterators should close when empty") {
    forAll { (is: List[Int], f: Int => Boolean) =>
      closedWhenEmpty(ResourceIterator(is*).filter(f)) should be(true)
    }
  }

  test("the empty iterator should have a definite size") {
    ResourceIterator.empty.hasDefiniteSize should be(true)
  }

  test("the empty iterator should not have a next element") {
    ResourceIterator.empty.hasNext should be(false)
  }

  test("the empty iterator should throw when next is called") {
    intercept[NoSuchElementException](ResourceIterator.empty.next())
    ()
  }

  test("non-empty iterators should not have a definite size") {
    forAll { (i: Int, is: List[Int]) =>
      ResourceIterator(i :: is: _*).hasDefiniteSize should be(false)
    }
  }

  test("forall should behave as expected") {
    forAll { (is: List[Int], f: Int => Boolean) =>
      ResourceIterator(is*).forall(f) should be(is.forall(f))
    }
  }

  test("isEmpty should behave as expected") {
    forAll { (is: List[Int]) =>
      ResourceIterator(is*).isEmpty should be(is.isEmpty)
    }
  }

  test("find should behave as expected") {
    forAll { (is: List[Int], f: Int => Boolean) =>
      ResourceIterator(is*).find(f) should be(is.find(f))
    }
  }

  test("exists should behave as expected") {
    forAll { (is: List[Int], f: Int => Boolean) =>
      ResourceIterator(is*).exists(f) should be(is.exists(f))
    }
  }

  test("isTraversableAgain should return false") {
    forAll { (is: List[Int]) =>
      ResourceIterator(is*).isTraversableAgain should be(false)
    }
  }

  test("withClose should register the close function properly") {
    forAll { (is: List[Int]) =>
      closedWhenEmpty(ResourceIterator(is*)) should be(true)
    }
  }

  test("a safe iterator should wrap 'next on empty' errors") {
    forAll { (is: List[Int]) =>
      val error: Throwable = new NoSuchElementException

      val res = ResourceIterator(is*).safe(error)(identity)
      closedWhenEmpty(res) should be(true)
      res.next().left.value should be(error)
    }
  }

  test("a safe iterator should wrap errors in next") {
    forAll { (is: FailingIterator[Int]) =>
      var closed = false

      val res = is.resourceIterator
        .withClose(() => closed = true)
        .safe(new Throwable("eos"))(identity)

      while(res.hasNext)
        if(res.next().isLeft) res.hasNext should be(false)
      closed should be(true)
    }
  }

  test("A closed iterator should not have next elements") {
    forAll { (is: List[Int]) =>
      val res = ResourceIterator(is*)
      res.close()
      res.hasNext should be(false)
    }
  }

  test("toList should behave as expected") {
    forAll { (is: List[Int]) =>
      ResourceIterator(is*).toList should be(is)
    }
  }

  test("toBuffer should behave as expected") {
    forAll { (is: List[Int]) =>
      ResourceIterator(is*).toBuffer should be(is.toBuffer)
    }
  }

  test("toIndexedSeq should behave as expected") {
    forAll { (is: List[Int]) =>
      ResourceIterator(is*).toIndexedSeq should be(is.toIndexedSeq)
    }
  }

  test("toIterable should behave as expected") {
    forAll { (is: List[Int]) =>
      ResourceIterator(is*).toIterable should be(is.to(Iterable))
    }
  }

  test("toSeq should behave as expected") {
    forAll { (is: List[Int]) =>
      ResourceIterator(is*).toSeq should be(is.toSeq)
    }
  }

  test("toSet should behave as expected") {
    forAll { (is: List[Int]) =>
      ResourceIterator(is*).toSet should be(is.toSet)
    }
  }

  test("toVector should behave as expected") {
    forAll { (is: List[Int]) =>
      ResourceIterator(is*).toVector should be(is.toVector)
    }
  }
}
