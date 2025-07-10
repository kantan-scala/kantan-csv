/*
 * Copyright 2016 Nicolas Rinaudo
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

package kantan.sbt

import sbt.*
import sbt.Keys.*
import wartremover.Wart
import wartremover.WartRemover
import wartremover.Warts

/** Makes compilation much more strict.
  *
  * This will make warnings fatal, as well as turn on various Scala linters (such as WartRemover).
  */
object StrictPlugin extends AutoPlugin {

  override def trigger =
    noTrigger

  override def requires: Plugins =
    KantanPlugin && WartRemover

  override lazy val projectSettings: Seq[Setting[?]] = wartRemoverSettings ++ scalacSettings

  /** All warnings are fatal in `Compile`.
    *
    * I'd love to make warnings fatal in `Test` as well, but the problem is that some tests actually need to do some
    * dodgy things to see what happens.
    */
  def scalacSettings: Seq[Setting[?]] =
    Seq(Compile / compile / scalacOptions += "-Xfatal-warnings")

  def wartRemoverSettings: Seq[Setting[?]] =
    List(Compile, Test).flatMap { c =>
      inConfig(c)(
        Compile / compile / WartRemover.autoImport.wartremoverErrors ++=
          Warts.allBut(
            Wart.MutableDataStructures,
            Wart.NonUnitStatements,
            Wart.Equals,
            Wart.Overloading,
            Wart.ImplicitParameter,
            Wart.Nothing,
            Wart.ImplicitConversion,
            Wart.Any,
            Wart.PublicInference, // Disabled because https://github.com/wartremover/wartremover/issues/337
            Wart.Recursion,
            Wart.ScalaApp
          )
      )
    }
}
