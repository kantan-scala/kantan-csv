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

// Implementation originally from https://github.com/zio/zio/blob/series/2.x/core/shared/src/main/scala/zio/Zippable.scala
trait Zippable[-A, -B] {
  type Out
  def zip(left: A, right: B): Out
}

object Zippable extends ZippableOps1 {
  type Out[-A, -B, C] = Zippable[A, B] { type Out = C }

  implicit def zippableLeftIdentity[A]: Zippable.Out[Unit, A, A] =
    (_: Unit, right: A) => right
}

trait ZippableOps1 extends ZippableOps2 {
  implicit def zippableRightIdentity[A]: Zippable.Out[A, Unit, A] =
    (left: A, _: Unit) => left
}

trait ZippableOps3 {

  implicit def zippable1[A, B]: Zippable.Out[A, B, (A, B)] =
    (left: A, right: B) => (left, right)
}
