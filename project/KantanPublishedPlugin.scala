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

import sbt.*
import sbt.Keys.*

/** Configures publication for kantan projects. */
object KantanPublishedPlugin extends AutoPlugin {
  override def trigger =
    allRequirements

  override def requires: Plugins =
    KantanKantanPlugin && PublishedPlugin

  override lazy val projectSettings: Seq[Setting[?]] = Seq(
    publishTo := (
      if(isSnapshot.value) None else localStaging.value
    ),
    versionScheme := Some("early-semver")
  )
}
