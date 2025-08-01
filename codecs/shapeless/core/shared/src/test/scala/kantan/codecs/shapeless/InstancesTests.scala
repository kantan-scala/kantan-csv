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

package kantan.codecs.shapeless

import kantan.codecs.Decoder
import kantan.codecs.shapeless.Instances.*
import kantan.codecs.shapeless.laws.Or
import kantan.codecs.shapeless.laws.discipline.DisciplineSuite
import kantan.codecs.shapeless.laws.discipline.StringCodecTests
import kantan.codecs.shapeless.laws.discipline.StringDecoderTests
import kantan.codecs.shapeless.laws.discipline.StringEncoderTests
import kantan.codecs.shapeless.laws.discipline.arbitrary.*
import kantan.codecs.strings.DecodeError
import kantan.codecs.strings.StringEncoder
import kantan.codecs.strings.codecs
import shapeless.CNil

object InstancesTests {
  private val decoder: Decoder[String, Or[Int, Boolean], DecodeError, codecs.type] =
    StringDecoderTests[Int Or Boolean].laws.decoder
}

@SuppressWarnings(Array("scalafix:DisableSyntax.null"))
class InstancesTests extends DisciplineSuite {

  private implicit val decoder: Decoder[String, Or[Int, Boolean], DecodeError, codecs.type] =
    InstancesTests.decoder

  // - Tests -----------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  checkAll("StringDecoder[Int Or Boolean]", StringDecoderTests[Int Or Boolean].decoder[Int, Int])
  checkAll("StringEncoder[Int Or Boolean]", StringEncoderTests[Int Or Boolean].encoder[Int, Int])
  checkAll("StringCodec[Int Or Boolean]", StringCodecTests[Int Or Boolean].codec[Int, Int])

  test("Encoder[?, CNil, ?] should fail") {
    intercept[IllegalStateException](StringEncoder[CNil].encode(null))
    ()
  }

}
