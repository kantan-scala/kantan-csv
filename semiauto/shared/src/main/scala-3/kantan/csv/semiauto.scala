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

import scala.compiletime.summonAll
import scala.deriving.Mirror.ProductOf

object semiauto {

  inline def deriveRowEncoder[A <: Product](using mirror: ProductOf[A]): RowEncoder[A] =
    productRowEncoder[A](
      summonAll[Tuple.Map[mirror.MirroredElemTypes, CellEncoder]]
    )

  inline def deriveRowDecoder[A <: Product](using mirror: ProductOf[A]): RowDecoder[A] =
    productRowDecoder[A](
      summonAll[Tuple.Map[mirror.MirroredElemTypes, CellDecoder]]
    )

  inline def deriveRowCodec[A <: Product](using mirror: ProductOf[A]): RowCodec[A] =
    productRowCodec[A](
      summonAll[Tuple.Map[mirror.MirroredElemTypes, CellCodec]]
    )

  def productRowEncoder[A <: Product](using
    mirror: ProductOf[A]
  )(
    typeClasses: Tuple.Map[mirror.MirroredElemTypes, CellEncoder]
  ): RowEncoder[A] = { (value: A) =>
    typeClasses
      .zip(Tuple.fromProductTyped[A](value))
      .toList
      .asInstanceOf[Seq[(CellEncoder[Any], Any)]]
      .map(_ `encode` _)
  }

  def productRowDecoder[A <: Product](using
    mirror: ProductOf[A]
  )(
    typeClasses: Tuple.Map[mirror.MirroredElemTypes, CellDecoder]
  ): RowDecoder[A] =
    RowDecoder.from[A]((values: Seq[String]) =>
      if(typeClasses.toList.lengthCompare(values) <= 0) {
        typeClasses.toList.map(_.asInstanceOf[CellDecoder[Any]]).zip(values).map { case (cellDecoder, cellValue) =>
          cellDecoder.decode(cellValue)
        } match {
          case Nil =>
            Right(mirror.fromProduct(new SeqProduct(Array.empty)))
          case decodedValues =>
            sequenceListEither(decodedValues).map(result => mirror.fromProduct(new SeqProduct(result.toArray)))
        }
      } else {
        DecodeResult.outOfBounds(0)
      }
    )

  def productRowCodec[A <: Product](using
    mirror: ProductOf[A]
  )(
    typeClasses: Tuple.Map[mirror.MirroredElemTypes, CellCodec]
  ): RowCodec[A] =
    RowCodec.from[A](
      productRowDecoder[A](typeClasses),
      productRowEncoder[A](typeClasses)
    )

  private def sequenceListEither[A, B](values: List[Either[A, B]]): Either[A, List[B]] =
    values
      .foldLeft(Right(List.empty[B]).withLeft[A]) {
        case (Right(x), Right(y)) =>
          Right(y :: x)
        case (Right(_), Left(y)) =>
          Left(y)
        case (x @ Left(_), _) =>
          x
      }
      .map(_.reverse)

  private final class SeqProduct(values: Array[Any]) extends Product {
    override def canEqual(that: Any): Boolean = true
    override def productArity: Int = values.length
    @SuppressWarnings(Array("org.wartremover.warts.SeqApply"))
    override def productElement(n: Int): Any = values(n)
  }
}
