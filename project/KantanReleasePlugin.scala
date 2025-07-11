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

package kantan.sbt.release

import kantan.sbt.KantanPlugin
import sbt.*
import sbtrelease.ReleasePlugin
import sbtrelease.ReleasePlugin.autoImport.*
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations.*

/** Slightly improves the default release process.
  */
object KantanReleasePlugin extends AutoPlugin {
  override def trigger =
    allRequirements

  override def requires: Plugins =
    KantanPlugin && ReleasePlugin

  override lazy val projectSettings: Seq[Setting[?]] =
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion,
      pushChanges
    )

}
