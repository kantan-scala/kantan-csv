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

import KantanPlugin.setLaws
import com.github.tkawachi.doctest.DoctestPlugin.autoImport.*
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.fastLinkJS
import sbt.*
import sbt.Keys.*
import sbt.internal.ProjectMatrix
import sbtprojectmatrix.ProjectMatrixKeys.virtualAxes
import scalafix_check.ScalafixCheck.autoImport.scalafixCheckAll
import spray.boilerplate.BoilerplatePlugin.autoImport.boilerplateSource

object KantanCrossBuildPlugin extends AutoPlugin {

  override def trigger =
    allRequirements

  override def requires =
    KantanPlugin

  object autoImport {
    def Scala3 = "3.3.6"

    def Scala213 = "2.13.16"

    def kantanCrossProject(id: String, base: String, enableScala3: Boolean = true): ProjectMatrix =
      kantanCrossProjectInternal(id = id, base = base, laws = None, enableScala3 = enableScala3)

    def kantanCrossProject(id: String, base: String, laws: String, enableScala3: Boolean): ProjectMatrix =
      kantanCrossProjectInternal(id = id, base = base, laws = Option(laws), enableScala3 = enableScala3)

    private def kantanCrossProjectInternal(
      id: String,
      base: String,
      laws: Option[String],
      enableScala3: Boolean
    ): ProjectMatrix = {
      val scalaVersions =
        if(enableScala3) {
          Seq(Scala213, Scala3)
        } else {
          Seq(Scala213)
        }

      ProjectMatrix(id = id, base = file(base))
        .defaultAxes()
        .settings(
          Seq(Compile, Test).flatMap { x =>
            Seq(
              x / boilerplateSource := file(base).getAbsoluteFile / "shared" / "src" / Defaults.nameForSrc(
                x.name
              ) / "boilerplate",
              (x / unmanagedResourceDirectories) ++= {
                if(virtualAxes.value.toSet.contains(VirtualAxis.jvm)) {
                  Seq(
                    file(base).getAbsoluteFile / "jvm" / "src" / Defaults.nameForSrc(x.name) / "resources"
                  )
                } else {
                  Nil
                }
              },
              (x / unmanagedSourceDirectories) ++= {
                val sharedBase = file(base).getAbsoluteFile / "shared" / "src" / Defaults.nameForSrc(x.name)

                Seq(
                  sharedBase / "scala",
                  scalaBinaryVersion.value match {
                    case "2.13" =>
                      sharedBase / "scala-2"
                    case "3" =>
                      sharedBase / "scala-3"
                  }
                )
              },
              (x / unmanagedSourceDirectories) ++= {
                val xs = virtualAxes.value.toSet
                val dirOpt =
                  if(xs(VirtualAxis.jvm)) {
                    Some("jvm")
                  } else if(xs(VirtualAxis.js)) {
                    Some("js")
                  } else if(xs(VirtualAxis.native)) {
                    Some("native")
                  } else {
                    None
                  }

                dirOpt.toSeq.flatMap { dir =>
                  val platformBase = file(base).getAbsoluteFile / dir / "src" / Defaults.nameForSrc(x.name)

                  Seq(
                    platformBase / "scala",
                    scalaBinaryVersion.value match {
                      case "2.13" =>
                        platformBase / "scala-2"
                      case "3" =>
                        platformBase / "scala-3"
                    }
                  )
                }
              }
            )
          }
        )
        .jvmPlatform(
          scalaVersions = scalaVersions,
          settings = Def.settings(
            laws.map(x => setLaws(s"${x}JVM")).toSeq,
            doctestTestFramework := DoctestTestFramework.ScalaTest,
            doctestScalaTestVersion := Some("3.2.19"),
            doctestGenTests := {
              scalaBinaryVersion.value match {
                case "3" =>
                  Seq.empty
                case _ =>
                  // TODO enable with Scala 3 and disable Scala 2
                  doctestGenTests.value
              }
            }
          )
        )
        .jsPlatform(
          scalaVersions = scalaVersions,
          settings = Def.settings(
            scalacOptions += {
              val a = (LocalRootProject / baseDirectory).value.toURI.toString
              val hash: String = sys.process.Process("git rev-parse HEAD").lineStream_!.head
              val g = s"https://raw.githubusercontent.com/kantan-scala/kantan-csv/${hash}"
              val key = scalaBinaryVersion.value match {
                case "3" =>
                  "-scalajs-mapSourceURI"
                case _ =>
                  "-P:scalajs:mapSourceURI"
              }
              s"${key}:$a->$g/"
            },
            name := s"$id-js",
            // Disables sbt-doctests in JS mode: https://github.com/sbt-doctest/sbt-doctest/issues/52
            doctestGenTests := Seq.empty,
            // Disables parallel execution in JS mode: https://github.com/scala-js/scala-js/issues/1546
            parallelExecution := false,
            laws.map(x => setLaws(s"${x}JS")).toSeq
          )
        )
    }

  }

  import autoImport.*

  override def globalSettings: Seq[Setting[?]] = Def.settings(
    Seq("2.13", "3").flatMap { scalaV =>
      val suffix = scalaV.replace('.', '_')
      val testCompile = TaskKey[Unit](s"testCompileScala$suffix")
      Seq(
        TaskKey[Unit](s"validate$suffix") := {
          testCompile.value
          taskAll(
            Compile / doc,
            scalaV
          ).value
          taskAll(
            scalafixCheckAll,
            scalaV
          ).value
          taskAll(
            Test / fastLinkJS,
            scalaV,
            _.autoPlugins.toSet.contains(ScalaJSPlugin)
          ).value
        },
        testCompile := {
          taskAll(
            Test / compile,
            scalaV
          ).value
        },
        TaskKey[Unit](s"testScala$suffix") := taskAll(
          Test / test,
          scalaV
        ).value
      )
    }
  )

  private def taskAll[A](
    key: TaskKey[A],
    scalaV: String,
    projectFilter: ResolvedProject => Boolean = Function.const(true)
  ): Def.Initialize[Task[Seq[A]]] = Def.taskDyn {
    val extracted = Project.extract(state.value)
    val projects = getProjects(state.value)
      .withFilter(projectFilter)
      .withFilter(p => extracted.get(LocalProject(p.id) / scalaBinaryVersion) == scalaV)
      .map(_.id)
      .sorted
    streams.value.log.info(projects.mkString(s"${key.key.label} [", " ", "]"))
    projects.map(p => LocalProject(p) / key).join
  }

  private def getProjects(s: State): Seq[ResolvedProject] = {
    val extracted = Project.extract(s)
    val currentBuildUri = extracted.currentRef.build
    val buildStructure = extracted.structure
    val buildUnitsMap = buildStructure.units
    val currentBuildUnit = buildUnitsMap(currentBuildUri)
    val projectsMap = currentBuildUnit.defined
    projectsMap.values.toVector
  }

}
