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

import de.heikoseeberger.sbtheader.FileType
import de.heikoseeberger.sbtheader.HeaderPlugin
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport._
import sbt.Keys._
import sbt._
import spray.boilerplate.BoilerplatePlugin

/** Add support for headers in sbt-boilerplate.
  *
  * See the [Boilerplate](https://github.com/sbt/sbt-header#sbt-boilerplate) documentation.
  */
object KantanBoilerplatePlugin extends AutoPlugin {

  override def trigger =
    allRequirements

  override def requires: Plugins =
    HeaderPlugin && BoilerplatePlugin

  override lazy val projectSettings: Seq[Setting[_]] = addBoilerplate(Compile, Test)

  private def addBoilerplate(confs: Configuration*): List[Setting[_]] =
    confs.foldLeft(List.empty[Setting[_]]) { (acc, conf) =>
      acc ++ Seq(
        conf / headerSources ++= (((conf / sourceDirectory).value / "boilerplate") ** "*.template").get,
        headerMappings        += (FileType("template") -> HeaderCommentStyle.cStyleBlockComment)
      )
    }
}
