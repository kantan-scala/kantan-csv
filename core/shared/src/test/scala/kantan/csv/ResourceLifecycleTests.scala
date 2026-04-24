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

package kantan.csv

import java.io.Reader
import java.io.StringReader
import java.io.Writer
import java.util.concurrent.atomic.AtomicBoolean
import kantan.csv.ops.*
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ResourceLifecycleTests extends AnyFunSuite with Matchers {

  private final class TrackingReader(content: String) extends Reader {
    private val underlying = new StringReader(content)
    val closed = new AtomicBoolean(false)

    override def read(cbuf: Array[Char], off: Int, len: Int): Int =
      underlying.read(cbuf, off, len)

    override def close(): Unit = {
      closed.set(true)
      underlying.close()
    }
  }

  private final class TrackingWriter extends Writer {
    val closed = new AtomicBoolean(false)
    override def write(cbuf: Array[Char], off: Int, len: Int): Unit = ()
    override def flush(): Unit = ()
    override def close(): Unit = {
      closed.set(true)
      ()
    }
  }

  test("readCsvRow closes the underlying Reader on success") {
    val r = new TrackingReader("1,2,3")
    val _ = r.readCsvRow[(Int, Int, Int)](rfc)
    r.closed.get should be(true)
  }

  test("readCsvRow closes the underlying Reader when there are extra rows") {
    val r = new TrackingReader("1,2,3\n4,5,6")
    val _ = r.readCsvRow[(Int, Int, Int)](rfc)
    r.closed.get should be(true)
  }

  test("readCsvRow closes the underlying Reader when decoding fails") {
    val r = new TrackingReader("not-an-int,2,3")
    val _ = r.readCsvRow[(Int, Int, Int)](rfc)
    r.closed.get should be(true)
  }

  test("CsvSink.write closes the writer when a row encoder throws") {
    final case class Explode(x: Int)
    implicit val explode: RowEncoder[Explode] =
      RowEncoder.from(_ => throw new RuntimeException("boom"))

    implicit val sink: CsvSink[TrackingWriter] =
      CsvSink.from(identity)

    val w = new TrackingWriter

    assertThrows[RuntimeException] {
      CsvSink[TrackingWriter].write(w, List(Explode(1)), rfc)
    }
    w.closed.get should be(true)
  }

  test("CsvReader.apply closes the Reader when header decoding fails") {
    final case class Row(a: Int, b: Int)
    // The decoder expects headers "a" and "b"; the CSV provides "x" and "y".
    implicit val hd: HeaderDecoder[Row] = HeaderDecoder.decoder("a", "b")(Row.apply)

    val r = new TrackingReader("x,y\n1,2\n")
    // `CsvReader.apply` must release the Reader as soon as header decoding fails, since the returned iterator no
    // longer references it.
    val _ = CsvReader[Row](r, rfc.withHeader)
    r.closed.get should be(true)
  }
}
