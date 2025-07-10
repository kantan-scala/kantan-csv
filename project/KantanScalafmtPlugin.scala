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
import kantan.sbt.KantanPlugin.autoImport.*
import org.scalafmt.sbt.ScalafmtPlugin
import org.scalafmt.sbt.ScalafmtPlugin.autoImport.*
import sbt.*

object KantanScalafmtPlugin extends AutoPlugin {
  override def trigger =
    allRequirements

  override def requires: Plugins =
    KantanPlugin && ScalafmtPlugin

  override lazy val projectSettings: Seq[Setting[?]] =
    checkStyleSettings ++ Seq(
      scalafmtAll := scalafmtAll.dependsOn(Compile / scalafmtSbt).value
    )

  // Makes sure checkStyle depends on the right scalafmt commands depending on the context.
  private def checkStyleSettings: Seq[Setting[?]] =
    Seq(
      (Compile / checkStyle) := (Compile / checkStyle)
        .dependsOn(Compile / scalafmtCheck, Compile / scalafmtSbtCheck)
        .value,
      (Test / checkStyle) := (Test / checkStyle).dependsOn(Test / scalafmtCheck).value
    )
}
