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

package kantan.csv.benchmark

import java.io.StringWriter
import java.util.concurrent.TimeUnit
import kantan.csv.engine.WriterEngine
import kantan.csv.engine.jackson.defaultSequenceWriterBuilder
import kantan.csv.ops.*
import kantan.csv.rfc
import org.apache.commons.csv.CSVFormat
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MICROSECONDS)
class Encoding {
  @Benchmark
  def kantanInternal: String =
    Encoding.kantan(rawData)

  @Benchmark
  def kantanJackson: String =
    Encoding.kantan(rawData)(kantan.csv.engine.jackson.jacksonCsvWriterEngine)

  @Benchmark
  def kantanCommons: String =
    Encoding.kantan(rawData)(kantan.csv.engine.commons.commonsCsvWriterEngine)

  @Benchmark
  def opencsv: String =
    Encoding.opencsv(rawData)

  @Benchmark
  def commons: String =
    Encoding.commons(rawData)

  @Benchmark
  def jackson: String =
    Encoding.jackson(rawData)

  @Benchmark
  def univocity: String =
    Encoding.univocity(rawData)

  @Benchmark
  def scalaCsv: String =
    Encoding.scalaCsv(rawData)
}

object Encoding {
  def write(data: List[CsvEntry])(f: Array[String] => Unit): Unit =
    data.foreach(entry => f(Array(entry._1.toString, entry._2, entry._3.toString, entry._4.toString)))

  def kantan(data: List[CsvEntry])(implicit engine: WriterEngine): String =
    data.asCsv(rfc)

  def opencsv(data: List[CsvEntry]): String = {
    import com.opencsv.{CSVWriter as OCSVWriter, ICSVWriter}
    val out = new StringWriter()
    val writer = new OCSVWriter(
      out,
      ',',
      ICSVWriter.DEFAULT_QUOTE_CHARACTER,
      ICSVWriter.DEFAULT_ESCAPE_CHARACTER,
      ICSVWriter.DEFAULT_LINE_END
    )
    write(data)(a => writer.writeNext(a))
    writer.close()
    out.close()
    out.toString
  }

  def commons(data: List[CsvEntry]): String = {
    val out = new StringWriter()
    val writer = new org.apache.commons.csv.CSVPrinter(out, CSVFormat.RFC4180)
    write(data)(a => writer.printRecords(a))
    writer.close()
    out.close()
    out.toString
  }

  def jackson(data: List[CsvEntry]): String = {

    val out = new StringWriter()
    val writer = defaultSequenceWriterBuilder(out, rfc)
    write(data) { a =>
      writer.write(a)
      ()
    }
    writer.close()
    out.close()
    out.toString
  }

  def univocity(data: List[CsvEntry]): String = {
    import com.univocity.parsers.csv.{CsvWriter, CsvWriterSettings}

    val out = new StringWriter()
    val writer = new CsvWriter(out, new CsvWriterSettings())
    write(data)(a => writer.writeRow(a*))
    writer.close()
    out.close()
    out.toString
  }

  def scalaCsv(data: List[CsvEntry]): String = {
    import com.github.tototoshi.csv.CSVWriter

    val out = new StringWriter()
    val writer = CSVWriter.open(out)
    data.foreach { row =>
      writer.writeRow(List(row._1.toString, row._2, row._3.toString, row._4.toString))
    }
    writer.close()
    out.close()
    out.toString
  }
}
