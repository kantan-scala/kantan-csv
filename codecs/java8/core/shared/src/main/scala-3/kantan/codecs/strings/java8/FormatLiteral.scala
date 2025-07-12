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

package kantan.codecs.strings.java8

import scala.quoted.Expr
import scala.quoted.Quotes

trait ToFormatLiteral {
  extension(inline sc: StringContext) {
    inline def fmt(args: Any*): Format =
      ${ FormatLiteral.fmtImpl('sc) }
  }
}

object FormatLiteral {
  def fmtImpl(sc: Expr[StringContext])(using q: Quotes): Expr[Format] = {
    import q.reflect.*
    val parts = sc match {
      case '{ StringContext($xs*) } => xs.value
    }
    parts match {
      case Some(Seq(str)) =>
        Format.from(str) match {
          case Left(_) =>
            report.errorAndAbort(s"Illegal format: '$str'")
          case Right(_) =>
            val spliced = Expr(str)

            '{
              Format
                .from(${ spliced })
                .getOrElse(sys.error(s"Illegal format: '${$spliced}'"))
            }
        }
      case _ =>
        report.errorAndAbort("fmt can only be used on string literals")
    }
  }
}
