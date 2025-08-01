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

package kantan.codecs.strings.java8

import java.time.LocalDate
import kantan.codecs.laws.discipline.DisciplineSuite
import kantan.codecs.laws.discipline.SerializableTests
import kantan.codecs.laws.discipline.StringCodecTests
import kantan.codecs.laws.discipline.StringDecoderTests
import kantan.codecs.laws.discipline.StringEncoderTests
import kantan.codecs.strings.StringDecoder
import kantan.codecs.strings.StringEncoder
import kantan.codecs.strings.java8.laws.discipline.arbitrary.*

class LocalDateCodecTests extends DisciplineSuite {

  checkAll("StringDecoder[LocalDate]", StringDecoderTests[LocalDate].decoder[Int, Int])
  checkAll("StringDecoder[LocalDate]", SerializableTests[StringDecoder[LocalDate]].serializable)

  checkAll("StringEncoder[LocalDate]", StringEncoderTests[LocalDate].encoder[Int, Int])
  checkAll("StringEncoder[LocalDate]", SerializableTests[StringEncoder[LocalDate]].serializable)

  checkAll("StringCodec[LocalDate]", StringCodecTests[LocalDate].codec[Int, Int])

}
