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

final case class SemiautoEncoder[A] private (encode: A => Seq[String]) {
  def toRowEncoder: RowEncoder[A] = (a: A) => encode(a)
}

object SemiautoEncoder {
  implicit val nil: SemiautoEncoder[HNil] =
    SemiautoEncoder(_ => Nil)

  implicit def cons[H: CellEncoder, T <: HList: SemiautoEncoder]: SemiautoEncoder[H :: T] =
    SemiautoEncoder { case h :: t =>
      CellEncoder[H].encode(h) +: implicitly[SemiautoEncoder[T]].encode(t)
    }

  implicit def product[A, L <: HList](implicit
    gen: Generic.Aux[A, L],
    hlist: Lazy[SemiautoEncoder[L]]
  ): SemiautoEncoder[A] = SemiautoEncoder(a => hlist.value.encode(gen.to(a)))
}
