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

import com.github.tkawachi.doctest.DoctestPlugin.autoImport.*
import de.heikoseeberger.sbtheader.HeaderPlugin
import sbt.*
import sbt.Keys.*
import sbt.plugins.JvmPlugin

/** Settings common to all projects.
  *
  * Among other things, this plugin will set sane scalac options, import whatever is required for the current version of
  * scala to support macros, ...
  *
  * It's usually necessary to enable one of the following plugins:
  *   - [[PublishedPlugin]] : configures projects whose artifacts are intended for publication on maven central.
  *   - [[UnpublishedPlugin]] : configures projects whose artifacts are not meant to be published, such as tests and
  *     documentation.
  *   - [[DocumentationPlugin]]: configures projects whose output is a documentation website.
  */
object KantanPlugin extends AutoPlugin {

  /** Hack to let you work around the fact that SBT refuses scenarios like:
    *
    *   - project `a`
    *   - project `tests` depends on `a` and provides useful tools, such as scalacheck Arbitrary instances
    *   - project `a` depends on `tests` in its `Test` configuration
    *
    * This is perfectly legal, but not supported. You can, however, use `.laws("tests")` in project `a` to enable it.
    */
  def setLaws(name: String): Setting[Task[Classpath]] =
    Test / unmanagedClasspath ++= (LocalProject(name) / Compile / fullClasspath).value

  object autoImport {

    implicit class KantanOperations(private val proj: Project) extends AnyVal {

      def laws(name: String): Project =
        proj.settings(setLaws(name))
    }

  }

  override def trigger =
    allRequirements

  override def requires: Plugins =
    JvmPlugin && HeaderPlugin

  override lazy val projectSettings: Seq[Setting[?]] =
    generalSettings ++ scalacSettings ++ javacSettings

  /** General settings. */
  lazy val generalSettings: Seq[Setting[?]] =
    Seq(
      autoAPIMappings := true,
      doctestMarkdownEnabled := true,
      doctestTestFramework := DoctestTestFramework.ScalaTest,
      doctestScalaTestVersion := Some("3.2.2")
    )

  private val jvmOptionVersion = "11"

  def javacSettings: Seq[Setting[?]] =
    Seq(
      javacOptions ++= Seq("-source", jvmOptionVersion, "-target", jvmOptionVersion)
    )

  /** Sane, version dependent scalac settings. */
  def scalacSettings: Seq[Setting[?]] = {
    def base(version: String) =
      CrossVersion.partialVersion(version) match {
        case Some((2, 13)) =>
          Seq(
            "-deprecation", // Emit warning and location for usages of deprecated APIs.
            "-encoding",
            "utf-8", // Specify character encoding used by source files.
            "-explaintypes", // Explain type errors in more detail.
            "-feature", // Emit warning and location for usages of features that should be imported explicitly.
            "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
            "-language:implicitConversions", // Allow definition of implicit functions called views
            "-unchecked", // Enable additional warnings where generated code depends on assumptions.
            "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
            "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
            "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
            "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
            "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
            "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
            "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
            "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
            "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
            "-Xlint:option-implicit", // Option.apply used implicit view.
            "-Xlint:package-object-classes", // Class or object defined in package object.
            "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
            "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
            "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
            "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
            "-Yrangepos", // Report Range Position of Errors to Language Server
            "-Ywarn-dead-code", // Warn when dead code is identified.
            "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
            "-Ywarn-numeric-widen", // Warn when numerics are widened.
            "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
            "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
            "-Ywarn-unused:locals", // Warn if a local definition is unused.
            "-Ywarn-unused:params", // Warn if a value parameter is unused.
            "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
            "-Ywarn-unused:privates", // Warn if a private member is unused.
            "-Ywarn-value-discard", // Warn when non-Unit expression results are unused.
            "-Xsource:3-cross",
            "-Xlint:inaccessible"
          )
        case _ =>
          Nil
      }

    // Sane defaults for warnings / errors:
    // - disable unused imports from the REPL, since it basically make sit unusable
    // - nothing is fatal (use StrictKantanPlugin for that)
    val unusedImports = Seq("-Ywarn-unused:imports", "-Ywarn-unused-import")
    Seq(
      scalacOptions := base(scalaVersion.value),
      Compile / console / scalacOptions --= unusedImports
    )
  }
}
