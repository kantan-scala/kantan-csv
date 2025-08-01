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

package kantan.codecs.resource.bom

import java.io.ByteArrayInputStream
import java.io.Reader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import kantan.codecs.resource.bom.BomReaderTests.TestString
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import scala.io.Codec

/** Makes sure `BomReader` reads BOMs as expected. */
class BomReaderTests extends AnyFunSuite with ScalaCheckPropertyChecks with Matchers {

  override implicit val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 1000)

  def read(str: String, codec: Codec): String = {
    def go(reader: Reader, acc: StringBuilder): String =
      reader.read() match {
        case -1 => acc.toString()
        case b => go(reader, acc.append(b.toChar))
      }

    go(BomReader(new ByteArrayInputStream(InMemoryBomWriter.write(str, codec)), Codec.ISO8859), new StringBuilder)
  }

  test("UTF-8 BOMs should be read properly") {
    forAll { (s: TestString) =>
      val str = s.value
      read(str, Codec.UTF8) should be(str)
    }
  }

  test("UTF-16LE BOMs should be read properly") {
    forAll { (s: TestString) =>
      val str = s.value
      read(str, Codec(Charset.forName("UTF-16LE"))) should be(str)
    }
  }

  test("UTF-16BE BOMs should be read properly") {
    forAll { (s: TestString) =>
      val str = s.value
      read(str, Codec(Charset.forName("UTF-16BE"))) should be(str)
    }
  }

  test("UTF-32LE BOMs should be read properly") {
    forAll { (s: TestString) =>
      val str = s.value
      read(str, Codec(Charset.forName("UTF-32LE"))) should be(str)
    }
  }

  test("UTF-32BE BOMs should be read properly") {
    forAll { (s: TestString) =>
      val str = s.value
      read(str, Codec(Charset.forName("UTF-32BE"))) should be(str)
    }
  }

  // Uses Gen.identifier to make sure we only get strings that are actually encodable in ISO-8859-1.
  test("Non-BOM encodings should be read properly") {
    forAll(Gen.identifier) { (str: String) =>
      read(str, Codec.ISO8859) should be(str)
    }
  }
}

object BomReaderTests {
  private final case class TestString(value: String)

  private object TestString {
    implicit val arbitrary: Arbitrary[TestString] =
      Arbitrary(
        implicitly[Arbitrary[String]].arbitrary.map {
          case x if x.getBytes(StandardCharsets.UTF_8).headOption.contains[Byte](0) =>
            TestString("")
          case x if x.getBytes(StandardCharsets.UTF_8).take(3).toList == List[Byte](-17, -69, -65) =>
            TestString("")
          case x =>
            TestString(x)
        }
      )
  }
}
