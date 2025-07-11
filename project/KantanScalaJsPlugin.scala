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
import sbt.*
import sbt.Keys.*
import sbt.internal.ProjectMatrix
import sbtprojectmatrix.ProjectMatrixKeys.virtualAxes
import spray.boilerplate.BoilerplatePlugin.autoImport.boilerplateSource

object KantanScalaJsPlugin extends AutoPlugin {

  override def trigger =
    allRequirements

  override def requires =
    KantanPlugin

  object autoImport {
    lazy val testJS: TaskKey[Unit] = taskKey[Unit]("run tests for JS projects only")
    lazy val testJVM: TaskKey[Unit] = taskKey[Unit]("run tests for JVM projects only")

    def kantanCrossProject(id: String, base: String): ProjectMatrix =
      kantanCrossProjectInternal(id = id, base = base, laws = None)

    def kantanCrossProject(id: String, base: String, laws: String): ProjectMatrix =
      kantanCrossProjectInternal(id = id, base = base, laws = Option(laws))

    private def Scala3 =
      "3.3.6"

    private def kantanCrossProjectInternal(id: String, base: String, laws: Option[String]): ProjectMatrix =
      ProjectMatrix(id = id, base = file(base))
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
          scalaVersions = Seq(KantanKantanPlugin.Scala213, Scala3),
          settings = Def.settings(
            laws.map(setLaws).toSeq
          )
        )
        .jsPlatform(
          scalaVersions = Seq(KantanKantanPlugin.Scala213, Scala3),
          settings = Def.settings(
            name := s"$id-js",
            // Disables sbt-doctests in JS mode: https://github.com/tkawachi/sbt-doctest/issues/52
            doctestGenTests := Seq.empty,
            // Disables parallel execution in JS mode: https://github.com/scala-js/scala-js/issues/1546
            parallelExecution := false,
            Test / testJS := (Test / test).value,
            Test / testJVM := (),
            laws.map(x => setLaws(s"${x}JS")).toSeq
          )
        )
  }

  import autoImport.*
  override lazy val projectSettings: Seq[Setting[Task[Unit]]] = Seq(
    Test / testJS := (),
    Test / testJVM := (Test / test).value
  )

  override def globalSettings: Seq[Setting[?]] =
    addCommandAlias(
      "validateJVM",
      "; clean"
        + "; all scalafmtCheckAll scalafmtSbtCheck scalafixCheckAll scalafixConfigRuleNamesSortCheck"
        + "; testJVM"
        + "; doc"
    ) ++ addCommandAlias(
      "validateJS",
      "; clean"
        + "; testJS"
    )

}
