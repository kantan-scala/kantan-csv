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

import com.github.sbt.sbtghpages.GhpagesPlugin.autoImport.ghpagesPushSite
import kantan.sbt.KantanPlugin.autoImport.*
import sbt.*
import sbt.Keys.*
import sbtrelease.ReleasePlugin.autoImport.*

object KantanRelease {

  /** Disables code instrumentation, to avoid publishing instrumented bytecode. */
  lazy val runCoverageOff: ReleaseStep = releaseStepCommand("coverageOff")

  /** Runs checkStyle. */
  lazy val runCheckStyle: ReleaseStep =
    ReleaseStep(
      action = { (st: State) =>
        val extracted = Project.extract(st)
        val ref = extracted.get(thisProjectRef)

        val stCompile = extracted.runAggregated(ref / Compile / checkStyle, st)
        extracted.runAggregated(ref / Test / checkStyle, stCompile)
      }
    )

  /** Runs `pushSite`. */
  lazy val runPushSite: ReleaseStep = { (st: State) =>
    val extracted = Project.extract(st)
    val ref = extracted.get(thisProjectRef)

    extracted.runAggregated(ref / ghpagesPushSite, st)
  }
}
