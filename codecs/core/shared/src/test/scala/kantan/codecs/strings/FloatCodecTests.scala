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

package kantan.codecs.strings

import kantan.codecs.laws.discipline.DecoderTests
import kantan.codecs.laws.discipline.DisciplineSuite
import kantan.codecs.laws.discipline.EncoderTests
import kantan.codecs.laws.discipline.StringCodecTests
import kantan.codecs.laws.discipline.StringDecoderTests
import kantan.codecs.laws.discipline.StringEncoderTests
import kantan.codecs.laws.discipline.arbitrary.*

class FloatCodecTests extends DisciplineSuite {

  checkAll("StringDecoder[Float]", StringDecoderTests[Float].decoder[Int, Int])
  checkAll("StringEncoder[Float]", StringEncoderTests[Float].encoder[Int, Int])
  checkAll("StringCodec[Float]", StringCodecTests[Float].codec[Int, Int])

  checkAll("TaggedDecoder[Float]", DecoderTests[String, Float, DecodeError, tagged.type].decoder[Int, Int])
  checkAll("TaggedEncoder[Float]", EncoderTests[String, Float, tagged.type].encoder[Int, Int])

}
