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

package kantan.codecs.enumeratum.values

import kantan.codecs.enumeratum.laws.discipline.EnumeratedString
import kantan.codecs.enumeratum.laws.discipline.arbitrary.*
import kantan.codecs.laws.discipline.DisciplineSuite
import kantan.codecs.laws.discipline.StringCodecTests
import kantan.codecs.laws.discipline.StringDecoderTests
import kantan.codecs.laws.discipline.StringEncoderTests

class StringEnumCodecTests extends DisciplineSuite {

  checkAll("StringDecoder[EnumeratedString]", StringDecoderTests[EnumeratedString].decoder[Int, Int])
  checkAll("StringEncoder[EnumeratedString]", StringEncoderTests[EnumeratedString].encoder[Int, Int])
  checkAll("StringCodec[EnumeratedString]", StringCodecTests[EnumeratedString].codec[Int, Int])

}
