/*
    Copyright 2010, Sanjay Dasgupta
    sanjay.dasgupta@gmail.com

    This file is part of VisualLangLab (http://vll.java.net/).

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

package vll.demos

import java.io.File
import scala.util.parsing.input.CharSequenceReader
import vll.core.VllParsers

object ArithExprWithAPI {
  
  private def handleFactor(t: Any): Float = t match {
    case Pair(0, f: String) => f.toFloat
    case Pair(1, expr) => handleExpr(expr)
  }
  
  private def handleTerm(t: Any): Float = t match {
    case Array(factor, list: List[_]) => 
      var factorValue: Float = handleFactor(factor)
      list.foreach(_ match {
          case Pair(0, multTerm) => factorValue *= handleFactor(multTerm)
          case Pair(1, divTerm) => factorValue /= handleFactor(divTerm)
        })
      factorValue
  }
  
  private def handleExpr(t: Any): Float = t match {
    case Array(term, list: List[_]) => 
      var termValue: Float = handleTerm(term)
      list.foreach(_ match {
          case Pair(0, plusTerm) => termValue += handleTerm(plusTerm)
          case Pair(1, minusTerm) => termValue -= handleTerm(minusTerm)
        })
      termValue
  }

  def main(args: Array[String]) {
    val vll = VllParsers.fromFile(new File("ArithExpr.vll"))
    val parser = vll.phrase(vll.getParserFor("Expr"))
    val ast = parser(new CharSequenceReader("3 * 5 + 16 / 4"))
    val result = handleExpr(ast.get)
    println(result)
  }
}
