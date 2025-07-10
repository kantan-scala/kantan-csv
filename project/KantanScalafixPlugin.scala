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
import kantan.sbt.Resources
import sbt.*
import sbt.Keys.*
import scalafix.sbt.ScalafixPlugin
import scalafix.sbt.ScalafixPlugin.autoImport.*

/** Provides support for shared scalafix configuration files. */
object KantanScalafixPlugin extends AutoPlugin {
  object autoImport {
    val scalafixResource: SettingKey[Option[String]] = settingKey("resource that holds the scalafix configuration")
    val copyScalafixConfig: TaskKey[Unit] = taskKey("Copies the scalafix resource if necessary")
  }

  import autoImport.*

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

  override lazy val projectSettings: Seq[Setting[?]] =
    rawScalafixSettings(Compile, Test) ++ Seq(
      scalafixResource := None,
      copyScalafixConfig := {
        val path = scalafixConfig.value.getOrElse(file(".scalafix.conf"))

        scalafixResource.value.foreach(Resources.copyIfNeeded(_, path))
      }
    )

  // Makes sure all relevant scalafix tasks depend on copyScalafixConfig
  private def rawScalafixSettings(configs: Configuration*): Seq[Setting[?]] =
    configs.flatMap { config =>
      inConfig(config)(
        Seq(
          scalafix := scalafix.dependsOn(copyScalafixConfig).evaluated
        )
      )
    }
}
