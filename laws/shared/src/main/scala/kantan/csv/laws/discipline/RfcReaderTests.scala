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

import kantan.csv.laws.RfcReaderLaws
import org.scalacheck.Prop.forAll
import org.typelevel.discipline.Laws

trait RfcReaderTests extends Laws {
  def laws: RfcReaderLaws

  def rfc4180: RuleSet =
    new DefaultRuleSet(
      name = "rfc4180",
      parent = None,
      "crlf row separator" -> forAll(laws.crlfRowSeparator),
      "lf row separator" -> forAll(laws.lfRowSeparator),
      "crlf ending" -> forAll(laws.crlfEnding),
      "lf ending" -> forAll(laws.lfEnding),
      "empty ending" -> forAll(laws.emptyEnding),
      "leading whitespace" -> forAll(laws.leadingWhitespace),
      "trailing whitespace" -> forAll(laws.trailingWhitespace),
      "trailing comma" -> forAll(laws.trailingWhitespace),
      "unnecessary double quotes" -> forAll(laws.unnecessaryDoubleQuotes),
      "unescaped double quotes" -> forAll(laws.unescapedDoubleQuotes),
      "escaped content" -> forAll(laws.escapedCells)
    )
}
