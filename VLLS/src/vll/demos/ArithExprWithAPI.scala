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
import vll.core.VllParsers

object ArithExprWithAPI {
  
  private def evalFactorAST(ast: Any): Float = ast match {
    case Pair(0, f: String) => f.toFloat
    case Pair(1, expr) => evalExprAST(expr)
  }
  
  private def evalTermAST(ast: Any): Float = ast match {
    case Array(factor, list: List[_]) => 
      var result: Float = evalFactorAST(factor)
      list.foreach(_ match {
          case Pair(0, multAST) => result *= evalFactorAST(multAST)
          case Pair(1, divAST) => result /= evalFactorAST(divAST)
        })
      result
  }
  
  private def evalExprAST(ast: Any): Float = ast match {
    case Array(term, list: List[_]) => 
      var result: Float = evalTermAST(term)
      list.foreach(_ match {
          case Pair(0, plusAST) => result += evalTermAST(plusAST)
          case Pair(1, minusAST) => result -= evalTermAST(minusAST)
        })
      result
  }

  def main(args: Array[String]) {
    val vll = VllParsers.fromFile(new File("ArithExpr.vll"))
    val parser = vll.getParserFor("Expr")
    val parseResult = vll.parseAll(parser, "(3 + 5) / (8 - 4)")
    parseResult match {
      case vll.Success(resultAST, _) => println(evalExprAST(resultAST))
      case vll.Failure(msg, where) => printf("Error: '%s' at line %d col %d%n", msg, where.pos.line, where.pos.column)
    }
  }
}
