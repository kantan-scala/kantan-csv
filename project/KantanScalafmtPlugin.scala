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

package kantan.sbt.scalafmt

import kantan.sbt.KantanPlugin
import kantan.sbt.KantanPlugin.autoImport._
import kantan.sbt.Resources
import org.scalafmt.sbt.ScalafmtPlugin
import org.scalafmt.sbt.ScalafmtPlugin.autoImport._
import sbt._

/** Provides support for shared scalafmt configuration files. */
object KantanScalafmtPlugin extends AutoPlugin {
  object autoImport {
    val scalafmtResource: SettingKey[Option[String]] = settingKey("resource that holds the scalafmt configuration")
    val copyScalafmtConfig: TaskKey[Unit]            = taskKey("Copies the scalafmt resource if necessary")
  }

  import autoImport._

  override def trigger =
    allRequirements

  override def requires: Plugins =
    KantanPlugin && ScalafmtPlugin

  override lazy val projectSettings: Seq[Setting[_]] =
    rawScalafmtSettings(Compile, Test) ++ checkStyleSettings ++ Seq(
      scalafmtResource := None,
      scalafmtAll      := scalafmtAll.dependsOn(Compile / scalafmtSbt).value,
      copyScalafmtConfig := {
        val path = scalafmtConfig.value

        scalafmtResource.value.foreach(Resources.copyIfNeeded(_, path))
      }
    )

  // Makes sure checkStyle depends on the right scalafmt commands depending on the context.
  private def checkStyleSettings: Seq[Setting[_]] =
    Seq(
      (Compile / checkStyle) := (Compile / checkStyle)
        .dependsOn(Compile / scalafmtCheck, Compile / scalafmtSbtCheck)
        .value,
      (Test / checkStyle) := (Test / checkStyle).dependsOn(Test / scalafmtCheck).value
    )

  // Makes sure all relevant scalafmt tasks depend on copyScalafmtConfig
  private def rawScalafmtSettings(configs: Configuration*): Seq[Setting[_]] =
    configs.flatMap { config =>
      inConfig(config)(
        Seq(
          scalafmt         := scalafmt.dependsOn(copyScalafmtConfig).value,
          scalafmtCheck    := scalafmtCheck.dependsOn(copyScalafmtConfig).value,
          scalafmtSbt      := scalafmtSbt.dependsOn(copyScalafmtConfig).value,
          scalafmtSbtCheck := scalafmtSbtCheck.dependsOn(copyScalafmtConfig).value
        )
      )
    }
}
