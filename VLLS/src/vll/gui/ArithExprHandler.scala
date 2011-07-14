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

package vll.gui

//import vll.core.VllParsers.~

import vll.core.VllParsers

class ArithExprHandler extends ParseTreeHandler {

  private def factor(fctr: Any): Float = fctr match {
    case Pair(0, fpn: String) => java.lang.Float.parseFloat(fpn)
    case Pair(1, Array(_, xprn, _)) => Expr(xprn)
  }

/*   private def factor(p: VllParsers, fctr: Any): Float = fctr match {
    case Pair(0, fpn: String) => java.lang.Float.parseFloat(fpn)
    case Pair(1, Array(_, xprn, _)) => Expr(p, xprn)
  }

 */  private def term(expr: Any): Float = expr match {
    case Array(t: Any, y: List[Any]) => y.foldLeft (factor(t)) (
        (f: Float, x: Any) => x match {
          case Pair(0, Array(_, t)) => f * factor(t)
          case Pair(1, Array(_, t)) => f / factor(t)
        }
      )
  }

/*   private def term(p: VllParsers, expr: Any): Float = expr match {
    case Array(t: Any, y: List[Any]) => y.foldLeft (factor(p, t)) (
        (f: Float, x: Any) => x match {
          case Pair(0, Array(_, t)) => f * factor(p, t)
          case Pair(1, Array(_, t)) => f / factor(p, t)
        }
      )
  }

 */  private def Expr(expr: Any): Float = expr match {
    case Array(t: Any, y: List[Any]) => y.foldLeft (term(t)) (
        (f: Float, x: Any) => x match {
          case Pair(0, Array(_, t)) => f + term(t)
          case Pair(1, Array(_, t)) => f - term(t)
        }
      )
  }

/*   private def Expr(p: VllParsers, expr: Any): Float = expr match {
    case Array(t: Any, y: List[Any]) => y.foldLeft (term(p, t)) (
        (f: Float, x: Any) => x match {
          case Pair(0, Array(_, t)) => f + term(p, t)
          case Pair(1, Array(_, t)) => f - term(p, t)
        }
      )
  }

 */  def onParse(parsers: VllParsers, tree: Any) {
    //printf("%f%n", Expr(parsers, tree))
    printf("%f%n", Expr(tree))
  }
}
