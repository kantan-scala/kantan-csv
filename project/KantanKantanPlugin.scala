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

import com.github.sbt.git.SbtGit.git
import com.jsuereth.sbtpgp.PgpKeys.publishSigned
import sbt.*
import sbt.Keys.*
import sbtrelease.ReleasePlugin.autoImport.*
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations.*

/** Plugin that sets kantan-specific values.
  *
  * This is really only meant for kantan projects. Don't use this unless you're me.
  *
  * In order for kantan builds to behave properly, the following two lines *must* be present in the `build.sbt` files:
  * {{{
  * ThisBuild / startYear     := Some(1978)
  * }}}
  */
object KantanKantanPlugin extends AutoPlugin {
  val kantanProject = "kantan-scala/kantan-csv"

  override def trigger =
    allRequirements

  override def requires: Plugins =
    KantanScalafixPlugin

  override lazy val projectSettings: Seq[Setting[?]] = generalSettings ++ remoteSettings

  lazy val generalSettings: Seq[Setting[?]] =
    Seq(
      organization := "io.github.kantan-scala",
      organizationName := "Nicolas Rinaudo",
      licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html")),
      releasePublishArtifactsAction := publishSigned.value,
      releaseProcess := Seq[ReleaseStep](
        checkSnapshotDependencies,
        inquireVersions,
        runClean,
        runTest,
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,
        publishArtifacts,
        releaseStepCommand("sonaRelease"),
        setNextVersion,
        commitNextVersion,
        pushChanges
      ),
      pomExtra := (
        <developers>
          <developer>
            <id>xuwei-k</id>
            <name>Kenji Yoshida</name>
            <url>https://github.com/xuwei-k</url>
          </developer>
        </developers>
      )
    )

  lazy val remoteSettings: Seq[Setting[?]] =
    Seq(
      homepage := Some(url("https://github.com/kantan-scala")),
      scmInfo := Some(
        ScmInfo(
          url(s"https://github.com/${kantanProject}"),
          s"scm:git:git@github.com:${kantanProject}.git"
        )
      )
    )
}
