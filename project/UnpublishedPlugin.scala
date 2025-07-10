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

import sbt.AutoPlugin
import sbt.Def
import sbt.Keys.*
import sbt.Resolver
import sbt.file

/** Configures projects whose artifacts are not meant for publication to maven central.
  *
  * This simply disables all publication-related settings.
  */
object UnpublishedPlugin extends AutoPlugin {

  override def projectSettings: Seq[Def.Setting[?]] =
    Seq(
      // List of settings grabbed from https://github.com/scala/scala-parallel-collections/pull/14.
      makePom := file(""),
      deliver := file(""),
      deliverLocal := file(""),
      publish := {},
      publishLocal := {},
      publishM2 := {},
      publishArtifact := false,
      publishTo := Some(Resolver.file("devnull", file("/dev/null")))
    )

  override def requires =
    KantanPlugin

  override def trigger =
    noTrigger
}
