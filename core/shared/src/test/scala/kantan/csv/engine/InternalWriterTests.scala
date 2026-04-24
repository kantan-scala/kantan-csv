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

package kantan.csv.engine

import kantan.csv.laws.discipline.DisciplineSuite
import kantan.csv.laws.discipline.WriterEngineTests
import kantan.csv.ops.*
import kantan.csv.rfc

class InternalWriterTests extends DisciplineSuite {
  checkAll("InternalWriter", WriterEngineTests(WriterEngine.internalCsvWriterEngine).writerEngine)

  test("cells containing the quote char must be escaped under QuotePolicy.Always") {
    List(List("a\"b")).asCsv(rfc.quoteAll) should be("\"a\"\"b\"\r\n")
  }

  test("cells containing multiple quote chars must be escaped under QuotePolicy.Always") {
    List(List("\"hello\"")).asCsv(rfc.quoteAll) should be("\"\"\"hello\"\"\"\r\n")
  }

  test("cells containing the quote char round-trip under QuotePolicy.Always") {
    val rows = List(List("a\"b", "plain", "c\"\"d"))
    rows.asCsv(rfc.quoteAll).unsafeReadCsv[List, List[String]](rfc) should be(rows)
  }
}
