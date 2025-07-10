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

import java.io.*
import java.net.URL
import java.security.MessageDigest
import sbt.*
import sbt.io.Using

object Resources {

  /** Computes the digest of the specified stream. */
  private def digest(in: => InputStream): Array[Byte] = {
    val digest = MessageDigest.getInstance("SHA-1")
    val stream = in
    val buffer = new Array[Byte](2048)

    def loop(): Array[Byte] = {
      val count = stream.read(buffer)
      if(count >= 0) {
        digest.update(buffer, 0, count)
        loop()
      } else digest.digest()
    }

    try loop()
    finally stream.close()
  }

  /** Copies the content of `from` to `to`, if `to` does not exist or is different from `from`. */
  def copyIfNeeded(from: URL, to: File): Unit =
    if(!(to.exists && digest(from.openStream).sameElements(digest(new FileInputStream(to))))) {
      Using.urlInputStream(from) { inputStream =>
        IO.transfer(inputStream, to)
      }
    }

  def copyIfNeeded(res: String, to: File): Unit =
    copyIfNeeded(getClass.getResource(res), to)
}
