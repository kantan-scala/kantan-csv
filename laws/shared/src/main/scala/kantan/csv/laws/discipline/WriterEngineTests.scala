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

package kantan.csv.laws.discipline

import kantan.csv.engine.WriterEngine
import kantan.csv.laws.WriterEngineLaws
import org.scalacheck.Prop.forAll
import org.typelevel.discipline.Laws

trait WriterEngineTests extends Laws {
  def laws: WriterEngineLaws

  def writerEngine: RuleSet =
    new DefaultRuleSet(
      name = "writerEngine",
      parent = None,
      "round-trip" -> forAll(laws.roundTrip),
      "quote all" -> forAll(laws.quoteAll),
      "column separator" -> forAll(laws.columnSeparator),
      "no trailing cell separator" -> forAll(laws.noTrailingSeparator),
      "crlf row separator" -> forAll(laws.crlfAsRowSeparator)
    )
}

object WriterEngineTests {
  def apply(engine: WriterEngine): WriterEngineTests =
    new WriterEngineTests {
      override def laws: WriterEngineLaws =
        WriterEngineLaws(engine)
    }
}
