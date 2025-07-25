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

package kantan.codecs.laws.discipline

import kantan.codecs.laws.OptionalLaws
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll
import org.typelevel.discipline.Laws

trait OptionalTests[A] extends Laws {
  def laws: OptionalLaws[A]

  implicit def arbA: Arbitrary[A]

  def optional: RuleSet =
    new SimpleRuleSet("optional", "empty uniqueness" -> forAll(laws.emptyUniqueness))
}

object OptionalTests {
  def apply[A: Arbitrary: OptionalLaws]: OptionalTests[A] =
    new OptionalTests[A] {
      override val laws: OptionalLaws[A] = implicitly[OptionalLaws[A]]
      override val arbA: Arbitrary[A] = implicitly[Arbitrary[A]]
    }
}
