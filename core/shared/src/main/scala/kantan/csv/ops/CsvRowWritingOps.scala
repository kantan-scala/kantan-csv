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

package kantan.csv.ops

import kantan.csv.CsvConfiguration
import kantan.csv.RowEncoder
import kantan.csv.engine.WriterEngine
import kantan.csv.rfc

/** Provides syntax for encoding single CSV rows as a string.
  *
  * Writing a single row as a `String` is a surprisingly recurrent feature request. This is how to do it:
  *
  * {{{
  * scala> import kantan.csv.rfc
  *
  * scala> (1, 2, 3).writeCsvRow(rfc)
  * res0: String = 1,2,3
  * }}}
  */
final class CsvRowWritingOps[A](private val a: A) extends AnyVal {
  @deprecated("use writeCsvRow(CsvConfiguration) instead", "0.1.18")
  def writeCsvRow(sep: Char)(implicit e: WriterEngine, encoder: RowEncoder[A]): String =
    writeCsvRow(rfc.withCellSeparator(sep))

  def writeCsvRow(conf: CsvConfiguration)(implicit e: WriterEngine, encoder: RowEncoder[A]): String =
    Seq(a).asCsv(conf).trim
}

trait ToCsvRowWritingOps {
  implicit def toCsvRowWritingOps[A](a: A): CsvRowWritingOps[A] =
    new CsvRowWritingOps(a)
}
