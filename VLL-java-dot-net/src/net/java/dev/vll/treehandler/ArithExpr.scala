/*
 Copyright 2010, Sanjay Dasgupta
 sanjay.dasgupta@gmail.com

 This file is part of VisualLangLab (https://vll.dev.java.net/).

 VisualLangLab is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 VisualLangLab is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with VisualLangLab.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.java.dev.vll.treehandler

import net.java.dev.vll.ParseTreeHandler
import net.java.dev.vll.VllParsers._

class ArithExpr extends ParseTreeHandler {
  private def eval(t: Any): Float = {
    t match {
      case n: String => n.toFloat
      case ~(~("(", x), ")") => eval(x)
      case ~(a, b: List[_]) => b.foldLeft (eval(a)) (
          (f: Float, x: Any) => x match {
            case ~("+", x) => f + eval(x)
            case ~("-", x) => f - eval(x)
            case ~("*", x) => f * eval(x)
            case ~("/", x) => f / eval(x)
          }
        )
    }
  }

  def onParse(tree: Any) {
    printf("%f%n", eval(tree))
  }
}
