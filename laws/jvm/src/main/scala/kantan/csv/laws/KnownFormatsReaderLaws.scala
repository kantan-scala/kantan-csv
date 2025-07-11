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

import kantan.csv.RowCodec
import kantan.csv.engine.ReaderEngine
import kantan.csv.laws.KnownFormatsReaderLaws.Car
import kantan.csv.ops.*
import kantan.csv.rfc
import scala.io.Codec

trait KnownFormatsReaderLaws {
  implicit def engine: ReaderEngine

  implicit val carFormat: RowCodec[Car] =
    RowCodec.caseCodec(1, 2, 3, 4, 0)(Car.apply)(x => Option((x.make, x.model, x.description, x.price, x.year)))

  def read(res: String): List[Car] = {
    implicit val codec: Codec = Codec.UTF8

    getClass.getResource(s"/known_formats/$res.csv").unsafeReadCsv[List, Car](rfc.withHeader(true))
  }

  lazy val reference: List[Car] = read("raw")

  def excelMac120: Boolean =
    read("excel_mac_12_0") == reference

  def numbers103: Boolean =
    read("numbers_1_0_3") == reference

  def googleDocs: Boolean =
    read("google_docs") == reference
}

object KnownFormatsReaderLaws {
  final case class Car(make: String, model: String, description: Option[String], price: Int, year: Int)
}
