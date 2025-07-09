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
package scalajs

import com.github.tkawachi.doctest.DoctestPlugin.autoImport._
import _root_.kantan.sbt.KantanPlugin.autoImport.checkStyle
import _root_.kantan.sbt.KantanPlugin.setLaws
import sbt.Keys._
import sbt._
import sbtcrossproject.CrossPlugin.autoImport._
import sbtcrossproject.CrossProject
import scalajscrossproject.ScalaJSCrossPlugin.autoImport._
import scoverage.ScoverageKeys.coverageEnabled
import spray.boilerplate.BoilerplatePlugin.autoImport.boilerplateSource

object KantanScalaJsPlugin extends AutoPlugin {

  override def trigger =
    allRequirements

  override def requires =
    KantanPlugin

  object autoImport {
    lazy val testJS: TaskKey[Unit]        = taskKey[Unit]("run tests for JS projects only")
    lazy val testJVM: TaskKey[Unit]       = taskKey[Unit]("run tests for JVM projects only")
    lazy val checkStyleJS: TaskKey[Unit]  = taskKey[Unit]("run style checks for JS projects only")
    lazy val checkStyleJVM: TaskKey[Unit] = taskKey[Unit]("run style checks for JVM projects only")

    // format: off
    def kantanCrossProject(id: String): CrossProject =
      CrossProject(id = id, file(id))(JSPlatform, JVMPlatform)
        .withoutSuffixFor(JVMPlatform)
        .crossType(CrossType.Full)
        // Overrides the default sbt-boilerplate source directory: https://github.com/sbt/sbt-boilerplate/issues/21
        .settings(
          Compile / boilerplateSource := baseDirectory.value.getParentFile / "shared" / "src" / "main" / "boilerplate",
          Test / boilerplateSource    := baseDirectory.value.getParentFile / "shared" / "src" / "test" / "boilerplate"
        )
        .jsSettings(
          name := id + "-js",
          // Disables sbt-doctests in JS mode: https://github.com/tkawachi/sbt-doctest/issues/52
          doctestGenTests := Seq.empty,
          // Disables coverage in JS mode: https://github.com/scoverage/scalac-scoverage-plugin/issues/196
          coverageEnabled := false,
          // Disables parallel execution in JS mode: https://github.com/scala-js/scala-js/issues/1546
          parallelExecution      := false,
          Test / testJS          := (Test / test).value,
          Test / testJVM         := { () },
          Compile / checkStyleJS := (Compile / checkStyle).value,
          Test / checkStyleJS    := (Test / checkStyle).value,
          checkStyleJVM          := { () }
        )
        .jvmSettings(name := id + "-jvm")
        // format: on

    /** Adds a `.laws` method for scala.js projects. */
    implicit class KantanJsOperations(private val proj: CrossProject) extends AnyVal {
      def laws(name: String): CrossProject =
        proj
          .jvmSettings(setLaws(name + ""))
          .jsSettings(setLaws(name + "JS"))

    }

  }

  import autoImport._
  // format: off
  override lazy val projectSettings: Seq[Setting[Task[Unit]]] = Seq(
    Test / testJS           := { () },
    Test / testJVM          := (Test / test).value,
    checkStyleJS            := { () },
    Compile / checkStyleJVM := (Compile / checkStyle).value,
    Test / checkStyleJVM    := (Test / checkStyle).value
  )
  // format: on

  override def globalSettings: Seq[Setting[_]] =
    addCommandAlias(
      "validateJVM",
      "; clean"
        + "; checkStyleJVM"
        + "; Test/checkStyleJVM"
        + "; coverageOn"
        + "; testJVM"
        + "; coverageAggregate"
        + "; coverageOff"
        + "; doc"
    ) ++ addCommandAlias(
      "validateJS",
      "; clean"
        + "; checkStyleJS"
        + "; Test/checkStyleJS"
        + "; testJS"
    )

}
