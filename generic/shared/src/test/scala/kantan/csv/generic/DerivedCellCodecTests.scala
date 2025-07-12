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

package kantan.csv.generic

import kantan.codecs.Decoder
import kantan.codecs.shapeless.laws.Or
import kantan.csv.DecodeError
import kantan.csv.codecs
import kantan.csv.generic.arbitrary.*
import kantan.csv.laws.discipline.CellCodecTests
import kantan.csv.laws.discipline.DisciplineSuite

class DerivedCellCodecTests extends DisciplineSuite {
  private implicit val decoder: Decoder[String, Or[Int, Boolean], DecodeError, codecs.type] =
    DerivedCellCodecTests.decoder

  checkAll("CellCodec[Or[Int, Boolean]]", CellCodecTests[Int Or Boolean].codec[Byte, String])

}

object DerivedCellCodecTests {
  private val decoder: Decoder[String, Or[Int, Boolean], DecodeError, codecs.type] =
    CellCodecTests[Int Or Boolean].laws.decoder
}
