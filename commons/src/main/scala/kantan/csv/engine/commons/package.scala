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

import kantan.codecs.resource.ResourceIterator
import kantan.csv.CsvConfiguration
import kantan.csv.CsvWriter
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.QuoteMode
import scala.jdk.CollectionConverters.*

/** Provides CSV reader and writer engines using [[https://commons.apache.org/proper/commons-csv/ commons-csv]].
  *
  * Importing `kantan.csv.engine.commons._` will replace default engines by the commons-backed ones. If you need to
  * tweak how commons-csv behaves, however, you can handcraft engines though [[readerEngineFrom]] and
  * [[writerEngineFrom]] - all you need is a function that knows how to turn a column separator character in an instance
  * of `CSVFormat`.
  */
package object commons {
  // - Formats ---------------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  type CSVFormat = org.apache.commons.csv.CSVFormat
  type QuoteMode = org.apache.commons.csv.QuoteMode

  /** Type of functions that create a `CSVFormat` instance from a given column separator. */
  type CSVFormatBuilder = CsvConfiguration => CSVFormat

  /** Creates a default `CSVFormat` instance using the specified column separator. */
  def defaultFormat(conf: CsvConfiguration): CSVFormat =
    CSVFormat.Builder
      .create(CSVFormat.RFC4180)
      .setDelimiter(conf.cellSeparator)
      .setQuote(conf.quote)
      .setQuoteMode(conf.quotePolicy match {
        case CsvConfiguration.QuotePolicy.Always     => QuoteMode.ALL
        case CsvConfiguration.QuotePolicy.WhenNeeded => QuoteMode.MINIMAL
      })
      .get()

  // - Reader engines --------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Creates a new `ReaderEngine` from the specified [[kantan.csv.engine.commons.CSVFormatBuilder]].
    *
    * The purpose of this is to let developers use some of the commons-csv features that kantan.csv does not expose
    * through its public API.
    */
  def readerEngineFrom(f: CSVFormatBuilder): ReaderEngine =
    ReaderEngine.from { (r, s) =>
      ResourceIterator.fromIterator(f(s).parse(r).iterator.asScala.map(CsvSeq.apply))
    }

  /** Default commons-csv `ReaderEngine`.
    *
    * It's possible to tweak the behaviour of the underlying writer through [[readerEngineFrom]].
    */
  implicit val commonsCsvReaderEngine: ReaderEngine = readerEngineFrom(defaultFormat)

  // - Writer engines --------------------------------------------------------------------------------------------------
  // -------------------------------------------------------------------------------------------------------------------
  /** Creates a new `WriterEngine` from the specified [[CSVFormatBuilder]].
    *
    * The purpose of this is to let developers use some of the commons-csv features that kantan.csv does not expose
    * through its public API.
    */
  def writerEngineFrom(f: CSVFormatBuilder): WriterEngine =
    WriterEngine.from { (w, s) =>
      CsvWriter(new CSVPrinter(w, f(s))) { (csv, ss) =>
        csv.printRecord(ss.asJava)
      }(_.close())
    }

  /** Default commons-csv `WriterEngine`.
    *
    * It's possible to tweak the behaviour of the underlying writer through [[writerEngineFrom]].
    */
  implicit val commonsCsvWriterEngine: WriterEngine = writerEngineFrom(defaultFormat)
}
