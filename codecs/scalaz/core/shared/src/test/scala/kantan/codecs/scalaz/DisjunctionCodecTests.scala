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

package kantan.codecs.scalaz

import kantan.codecs.laws.discipline.DisciplineSuite
import kantan.codecs.laws.discipline.StringCodecTests
import kantan.codecs.laws.discipline.StringDecoderTests
import kantan.codecs.laws.discipline.StringEncoderTests
import kantan.codecs.scalaz.laws.discipline.arbitrary.*
import scalaz.\/

class DisjunctionCodecTests extends DisciplineSuite {

  checkAll("StringDecoder[Int \\/ Boolean]", StringDecoderTests[Int \/ Boolean].decoder[Int, Int])
  checkAll("StringEncoder[Int \\/ Boolean]", StringEncoderTests[Int \/ Boolean].encoder[Int, Int])
  checkAll("StringCodec[Int \\/ Boolean]", StringCodecTests[Int \/ Boolean].codec[Int, Int])

}
