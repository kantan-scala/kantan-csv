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

package kantan.codecs

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class EncoderCompanionTests extends AnyFunSuite with ScalaCheckPropertyChecks with Matchers {
  object codec
  object Companion extends EncoderCompanion[String, codec.type]

  test("EncoderCompanion.from should be equivalent to Encoder.from") {
    forAll { (f: Int => String, i: Int) =>
      Encoder.from(f).encode(i) should be(Companion.from(f).encode(i))
    }
  }
}
