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

package kantan.csv.cats

import cats.Eq
import cats.data.EitherT
import cats.instances.all.*
import cats.laws.discipline.ContravariantTests
import cats.laws.discipline.MonadErrorTests
import cats.laws.discipline.SemigroupKTests
import cats.laws.discipline.SemigroupalTests.Isomorphisms
import kantan.csv.DecodeError
import kantan.csv.RowDecoder
import kantan.csv.RowEncoder
import kantan.csv.cats.arbitrary.*
import kantan.csv.cats.equality.*
import kantan.csv.laws.discipline.DisciplineSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Gen

class RowCodecInstancesTests extends DisciplineSuite {
  override implicit val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(sizeRange = 4)

  // Limits the size of rows to 10 - using the default size makes these tests prohibitively long in some contexts
  // (in particular, travis will timeout on the scala.js execution of these tests).
  implicit def arbSeq[A: Arbitrary]: Arbitrary[Seq[A]] =
    Arbitrary(Gen.choose(0, 4).flatMap(n => Gen.listOfN(n, implicitly[Arbitrary[A]].arbitrary)))

  // cats doesn't provide an Eq[Seq] instance, mostly because Seq isn't a very meaningfull type.
  implicit def seqEq[A: Eq]: Eq[Seq[A]] =
    Eq.by(_.toList)

  // For some reason, these are not derived automatically. I *think* it's to do with the various codecs being type
  // aliases for types with many holes, but this is slightly beyond me.
  implicit val eqRowEitherT: Eq[EitherT[RowDecoder, DecodeError, Int]] = EitherT.catsDataEqForEitherT
  implicit val rowIso: Isomorphisms[RowDecoder] = Isomorphisms.invariant

  checkAll("RowDecoder", SemigroupKTests[RowDecoder].semigroupK[Int])
  checkAll("RowDecoder", MonadErrorTests[RowDecoder, DecodeError].monadError[Int, Int, Int])
  checkAll("RowEncoder", ContravariantTests[RowEncoder].contravariant[Int, Int, Int])

}
