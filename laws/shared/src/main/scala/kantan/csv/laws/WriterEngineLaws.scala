/*
 * Copyright 2015 Nicolas Rinaudo
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

package kantan.csv.laws

import kantan.csv.engine.WriterEngine
import kantan.csv.ops.*
import kantan.csv.rfc

trait WriterEngineLaws extends RfcWriterLaws {
  def quoteAll(csv: List[List[Int]]): Boolean = {
    val data = csv.filter(_.nonEmpty)

    data.asCsv(rfc.quoteAll).trim == data.map(_.map(i => s""""${i}"""").mkString(",")).mkString("\r\n")
  }

  def columnSeparator(csv: List[List[Cell]], c: Char): Boolean = {
    if(rfc.quote == c) {
      // https://github.com/apache/commons-csv/blob/59164c8b795ebd4cc0362c4c74d7c893c4a50303/src/main/java/org/apache/commons/csv/CSVFormat.java#L2605-L2610
      true
    } else {
      roundTripFor(csv, rfc.withCellSeparator(c))
    }
  }
}

object WriterEngineLaws {
  def apply(e: WriterEngine): WriterEngineLaws =
    new WriterEngineLaws {
      override implicit val engine: WriterEngine = e
    }
}
