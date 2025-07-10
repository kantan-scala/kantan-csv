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

package kantan.sbt.scalafix

import kantan.sbt.KantanPlugin
import sbt.*
import sbt.Keys.*
import scalafix.sbt.ScalafixPlugin
import scalafix.sbt.ScalafixPlugin.autoImport.*

object KantanScalafixPlugin extends AutoPlugin {
  override def trigger =
    allRequirements

  override def requires: Plugins =
    KantanPlugin && ScalafixPlugin

  override def buildSettings: Seq[Setting[?]] =
    Seq(
      semanticdbEnabled := true,
      semanticdbIncludeInJar := false,
      semanticdbVersion := scalafixSemanticdb.revision
    )
}
