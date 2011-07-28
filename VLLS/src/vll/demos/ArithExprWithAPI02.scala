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

object ArithExprWithAPI02 {
  
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
    val vll = VllParsers.fromString(grammar)
    val phraseParser = vll.phrase(vll.getParserFor("Expr"))
    val parseResult = phraseParser(new CharSequenceReader("(3 + 5) / (8 - 4)"))
    parseResult match {
      case vll.Success(resultAST, _) => println(evalExprAST(resultAST))
      case vll.Failure(msg, where) => printf("Error: '%s' at line %d col %d%n", msg, where.pos.line, where.pos.column)
    }
  }
  
  val grammar = """
<VLL-Grammar>
  <Whitespace>\\s+</Whitespace>
  <Comments></Comments>
  <Tokens>
    <Literal Name="RPAREN" Pattern=")"/>
    <Regex Name="floatingPointNumber" Pattern="(\\d+(\\.\\d*)?|\\d*\\.\\d+)([eE][+-]?\\d+)?[fFdD]?"/>
    <Literal Name="MULT" Pattern="*"/>
    <Literal Name="LPAREN" Pattern="("/>
    <Literal Name="MINUS" Pattern="-"/>
    <Literal Name="DIV" Pattern="/"/>
    <Literal Name="PLUS" Pattern="+"/>
  </Tokens>
  <Parsers>
    <Parser Name="Expr">
        <Sequence >
          <Reference Ref="term" />
          <Choice Mult="*">
            <Sequence >
              <Token Ref="PLUS" Drop="true"/>
              <Reference Ref="term" />
            </Sequence>
            <Sequence >
              <Token Ref="MINUS" Drop="true"/>
              <Reference Ref="term" />
            </Sequence>
          </Choice>
        </Sequence>
    </Parser>
    <Parser Name="factor">
        <Choice >
          <Token Ref="floatingPointNumber" />
          <Sequence >
            <Token Ref="LPAREN" Drop="true"/>
            <Reference Ref="Expr" />
            <Token Ref="RPAREN" Drop="true"/>
          </Sequence>
        </Choice>
    </Parser>
    <Parser Name="term">
        <Sequence >
          <Reference Ref="factor" />
          <Choice Mult="*">
            <Sequence >
              <Token Ref="MULT" Drop="true"/>
              <Reference Ref="factor" />
            </Sequence>
            <Sequence >
              <Token Ref="DIV" Drop="true"/>
              <Reference Ref="factor" />
            </Sequence>
          </Choice>
        </Sequence>
    </Parser>
  </Parsers>
</VLL-Grammar>
"""
}
