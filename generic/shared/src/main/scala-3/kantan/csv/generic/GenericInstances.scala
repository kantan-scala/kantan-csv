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

package kantan.csv.generic

import kantan.csv.CellDecoder
import kantan.csv.CellEncoder
import kantan.csv.DecodeResult
import kantan.csv.RowDecoder
import kantan.csv.RowEncoder
import kantan.csv.generic.GenericInstances.SeqProduct
import kantan.csv.generic.GenericInstances.sequenceListEither
import kantan.csv.generic.given
import scala.compiletime.summonAll
import scala.deriving.Mirror

@SuppressWarnings(Array("org.wartremover.warts.All"))
trait GenericInstances {
  inline given getRowDecoder[A <: Product](using mirror: Mirror.ProductOf[A]): RowDecoder[A] = {
    val xs = summonAll[Tuple.Map[mirror.MirroredElemTypes, CellDecoder]]
    productRowDecoder[A](mirror)(xs)
  }

  given singleCellDecoder[A <: Product, B: CellDecoder](using
    mirror: Mirror.ProductOf[A] { type MirroredElemTypes = Tuple1[B] }
  ): CellDecoder[A] =
    CellDecoder[B].map(b => mirror.fromProductTyped(Tuple1(b)))

  given singleCellEncoder[A <: Product, B: CellEncoder](using
    mirror: Mirror.ProductOf[A] { type MirroredElemTypes = Tuple1[B] }
  ): CellEncoder[A] =
    CellEncoder[B].contramap((a: A) => Tuple.fromProductTyped(a)._1)

  inline given sumCellDecoder[A](using mirror: Mirror.SumOf[A]): CellDecoder[A] = {
    val xs = summonAll[Tuple.Map[mirror.MirroredElemTypes, CellDecoder]].toList.map(_.asInstanceOf[CellDecoder[A]])
    sumCellDecoderImpl[A](mirror, xs)
  }

  final def sumCellDecoderImpl[A](mirror: Mirror.SumOf[A], typeclasses: List[CellDecoder[A]]): CellDecoder[A] = {
    (value: String) =>
      typeclasses.iterator
        .map(_.decode(value))
        .collectFirst { case x @ Right(_) => x }
        .getOrElse(
          typeclasses.head.decode(value)
        )
  }

  inline given sumCellEncoder[A](using mirror: Mirror.SumOf[A]): CellEncoder[A] = {
    val xs = summonAll[Tuple.Map[mirror.MirroredElemTypes, CellEncoder]].toList.map(_.asInstanceOf[CellEncoder[A]])
    sumCellEncoderImpl[A](mirror, xs)
  }

  final def sumCellEncoderImpl[A](mirror: Mirror.SumOf[A], typeclasses: List[CellEncoder[A]]): CellEncoder[A] = {
    (a: A) =>
      typeclasses(mirror.ordinal(a)).encode(a)
  }

  inline given sumRowDecoder[A](using mirror: Mirror.SumOf[A]): RowDecoder[A] = {
    val xs = summonAll[Tuple.Map[mirror.MirroredElemTypes, CellDecoder]]
    sumRowDecoder[A](mirror)(xs)
  }

  inline given productRowEncoder[A <: Product](using mirror: Mirror.ProductOf[A]): RowEncoder[A] = {
    val xs = summonAll[Tuple.Map[mirror.MirroredElemTypes, CellEncoder]]
    ???
  }

  inline given sumRowEncoder[A](using mirror: Mirror.SumOf[A]): RowEncoder[A] = {
    val xs = summonAll[Tuple.Map[mirror.MirroredElemTypes, CellEncoder]]
    ???
  }

  final def sumRowDecoder[A](
    mirror: Mirror.SumOf[A]
  )(typeClasses: Tuple.Map[mirror.MirroredElemTypes, CellDecoder]): RowDecoder[A] =
    RowDecoder.from[A] { (values: Seq[String]) =>
      ???
    }

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  final def productRowDecoder[A <: Product](
    mirror: Mirror.ProductOf[A]
  )(typeClasses: Tuple.Map[mirror.MirroredElemTypes, CellDecoder]): RowDecoder[A] =
    RowDecoder.from[A]((values: Seq[String]) =>
      if(typeClasses.toList.lengthCompare(values) <= 0) {
        typeClasses.toList.map(_.asInstanceOf[CellDecoder[Any]]).zip(values).map { case (cellDecoder, cellValue) =>
          cellDecoder.decode(cellValue)
        } match {
          case Nil =>
            Right(mirror.fromProduct(new SeqProduct(Nil)))
          case decodedValues =>
            sequenceListEither(decodedValues).map(result => mirror.fromProduct(new SeqProduct(result)))
        }
      } else {
        DecodeResult.outOfBounds(0)
      }
    )
}

object GenericInstances {
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

  private final class SeqProduct(values: Seq[Any]) extends Product {
    override def canEqual(that: Any): Boolean = true
    override def productArity: Int = values.size
    @SuppressWarnings(Array("org.wartremover.warts.SeqApply"))
    override def productElement(n: Int): Any = values(n)
  }
}
