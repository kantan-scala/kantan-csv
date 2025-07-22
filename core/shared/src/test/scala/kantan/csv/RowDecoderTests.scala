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

import org.scalatest.funsuite.AnyFunSuite

@SuppressWarnings(Array("scalafix:DisableSyntax.var"))
class RowDecoderTests extends AnyFunSuite {
  test("recoverWith") {
    var sideEffect = 0
    val decoder: RowDecoder[(Int, Int)] = RowDecoder[(Int, Int)].recoverWith {
      case _ if { sideEffect += 1; true } =>
        Right((2, 3))
    }
    assert(decoder.decode(Nil) == Right((2, 3)))
    assert(sideEffect == 1)
  }

  test("fromPartial") {
    var sideEffect = 0
    val decoder: RowDecoder[(Int, Int)] = RowDecoder.fromPartial {
      case _ if { sideEffect += 1; true } =>
        Right((2, 3))
    }
    assert(decoder.decode(Nil) == Right((2, 3)))
    assert(sideEffect == 1)
  }
}
