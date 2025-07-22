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

package kantan.csv

import shapeless.::
import shapeless.Generic
import shapeless.HList
import shapeless.HNil
import shapeless.Lazy

final case class SemiautoDecoder[A] private (decode: Seq[String] => Either[DecodeError, A]) {
  def toRowDecoder: RowDecoder[A] = (a: Seq[String]) => decode(a)
}

object SemiautoDecoder {
  private val rightHNil: Either[DecodeError, HNil] = Right(HNil)

  implicit val nil: SemiautoDecoder[HNil] =
    SemiautoDecoder(_ => rightHNil)

  implicit def cons[H: CellDecoder, T <: HList: SemiautoDecoder]: SemiautoDecoder[H :: T] =
    SemiautoDecoder { case h +: t =>
      for {
        head <- CellDecoder[H].decode(h)
        tail <- implicitly[SemiautoDecoder[T]].decode(t)
      } yield shapeless.::(head, tail)
    }

  implicit def product[A, L <: HList](implicit
    gen: Generic.Aux[A, L],
    hlist: Lazy[SemiautoDecoder[L]]
  ): SemiautoDecoder[A] =
    SemiautoDecoder(values => hlist.value.decode(values).map(gen.from))
}
