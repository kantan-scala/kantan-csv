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

package kantan.codecs

package object `export` {
  @deprecated("use kantan.codecs.DerivedDecoder")
  type DerivedDecoder[E, D, F, T] = kantan.codecs.DerivedDecoder[E, D, F, T]
  @deprecated("use kantan.codecs.DerivedDecoder")
  val DerivedDecoder = kantan.codecs.DerivedDecoder

  @deprecated("use kantan.codecs.DerivedEncoder")
  type DerivedEncoder[E, D, T] = kantan.codecs.DerivedEncoder[E, D, T]
  @deprecated("use kantan.codecs.DerivedEncoder")
  val DerivedEncoder = kantan.codecs.DerivedEncoder

  @deprecated("use kantan.codecs.Exported")
  type Exported[A] = kantan.codecs.Exported[A]
}
