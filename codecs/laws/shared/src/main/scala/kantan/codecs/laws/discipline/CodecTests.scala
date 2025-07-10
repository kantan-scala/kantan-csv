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

import kantan.codecs.laws.CodecLaws
import kantan.codecs.laws.CodecValue
import kantan.codecs.laws.CodecValue.IllegalValue
import kantan.codecs.laws.CodecValue.LegalValue
import kantan.codecs.laws.discipline.arbitrary.*
import org.scalacheck.Arbitrary
import org.scalacheck.Cogen
import org.scalacheck.Prop.forAll

trait CodecTests[E, D, F, T] extends DecoderTests[E, D, F, T] with EncoderTests[E, D, T] {
  def laws: CodecLaws[E, D, F, T]

  private def coreRules[A: Arbitrary: Cogen, B: Arbitrary: Cogen](implicit
    av: Arbitrary[CodecValue[E, D, T]]
  ): RuleSet =
    new DefaultRuleSet(
      "round trip",
      Some(encoder[A, B]),
      "round trip (encoding)" -> forAll(laws.roundTripEncoding),
      "round trip (decoding)" -> forAll(laws.roundTripDecoding),
      "leftMap identity(encoding)" -> forAll(laws.leftMapIdentityEncoding),
      "leftMap composition(decoding)" -> forAll(laws.leftMapCompositionEncoding[A, B]),
      "imap identity (encoding)" -> forAll(laws.imapIdentityEncoding),
      "imap identity (decoding)" -> forAll(laws.imapIdentityDecoding),
      "imap composition (encoding)" -> forAll(laws.imapCompositionEncoding[A, B]),
      "imap composition (decoding)" -> forAll(laws.imapCompositionDecoding[A, B]),
      "imapEncoded identity (encoding)" -> forAll(laws.imapEncodedIdentityEncoding),
      "imapEncoded identity (decoding)" -> forAll(laws.imapEncodedIdentityDecoding),
      "imapEncoded composition (encoding)" -> forAll(laws.imapEncodedCompositionEncoding[A, B]),
      "imapEncoded composition(decoding)" -> forAll(laws.imapEncodedCompositionDecoding[A, B])
    )

  def bijectiveCodec[A: Arbitrary: Cogen, B: Arbitrary: Cogen]: RuleSet =
    new RuleSet {
      implicit val arbValues: Arbitrary[CodecValue[E, D, T]] = Arbitrary(arbLegal.arbitrary)

      val name = "bijective codec"
      val bases = Nil
      val parents = Seq(coreRules[A, B], bijectiveDecoder[A, B])
      val props = Seq.empty
    }

  def codec[A: Arbitrary: Cogen, B: Arbitrary: Cogen](implicit ai: Arbitrary[IllegalValue[E, D, T]]): RuleSet =
    new RuleSet {
      val name = "codec"
      val bases = Nil
      val parents = Seq(coreRules[A, B], decoder[A, B])
      val props = Seq.empty
    }
}

object CodecTests {
  def apply[E: Arbitrary: Cogen, D: Arbitrary: Cogen, F: Cogen: Arbitrary, T](implicit
    l: CodecLaws[E, D, F, T],
    al: Arbitrary[LegalValue[E, D, T]]
  ): CodecTests[E, D, F, T] =
    new CodecTests[E, D, F, T] {
      override val laws = l
      override val arbLegal = al
      override val arbF = implicitly[Arbitrary[F]]
      override val cogenF = Cogen[F]
      override val cogenD = Cogen[D]
      override val cogenE = Cogen[E]
      override val arbD = implicitly[Arbitrary[D]]
      override val arbE = implicitly[Arbitrary[E]]
    }
}
