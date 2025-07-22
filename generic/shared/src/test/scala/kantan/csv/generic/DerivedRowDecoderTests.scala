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

import kantan.csv.CellDecoder
import kantan.csv.DecodeError
import kantan.csv.RowDecoder
import org.scalatest.funsuite.AnyFunSuite

@SuppressWarnings(Array("scalafix:DisableSyntax.var"))
class DerivedRowDecoderTests extends AnyFunSuite {
  test("does not call all CellDecoder") {
    var a1Call, a2Call, a3Call = 0
    case class A1(x: Int)
    object A1 {
      implicit val decoder: CellDecoder[A1] = (s: String) => {
        a1Call += 1
        s.toIntOption.map(apply).toRight(DecodeError.TypeError("invalid A1"))
      }
    }
    case class A2(x: Int)
    object A2 {
      implicit val decoder: CellDecoder[A2] = (s: String) => {
        a2Call += 1
        s.toIntOption.map(apply).toRight(DecodeError.TypeError("invalid A2"))
      }
    }
    case class A3(x: Int)
    object A3 {
      implicit val decoder: CellDecoder[A3] = (s: String) => {
        a3Call += 1
        s.toIntOption.map(apply).toRight(DecodeError.TypeError("invalid A3"))
      }
    }
    case class B(a1: A1, a2: A2, a3: A3)

    assert((a1Call, a2Call, a3Call) == (0, 0, 0))
    assert(RowDecoder[B].decode(Seq("10", "invalid", "30")).isLeft)
    assert((a1Call, a2Call, a3Call) == (1, 1, 0))
  }
}
