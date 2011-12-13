/*
    Copyright 2011, Sanjay Dasgupta
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
import scala.util.parsing.combinator.Parsers
import vll.core.VllParsers

object TDARExprAST {
  
  val memory = scala.collection.mutable.Map[String, BigInt]()

  def atomHdlr(xa: Any): BigInt = xa match {
    case Pair(0, s: String) => s.toInt
    case Pair(1, id: String) => 
      if (memory.contains(id)) memory(id)
      else {println("undefined: " + id); 0}
    case Pair(2, expr) => exprHdlr(expr)
  }

  def multExprHdlr(xme: Any): BigInt = xme match {
    case Array(mexpr, lst: List[_]) => 
      var result = atomHdlr(mexpr)
      lst.foreach(result *= atomHdlr(_))
      result
  }

  def exprHdlr(xe: Any): BigInt = xe match {
    case Array(mexpr, lst: List[_]) => 
      var result = multExprHdlr(mexpr)
      lst.foreach(_ match {
          case Array(Pair(0, _), mexpr2) => result += multExprHdlr(mexpr2)
          case Array(Pair(1, _), mexpr2) => result -= multExprHdlr(mexpr2)
        })
      result
  }

  def statHdlr(s: Any) = s match {
    case Pair(0, Array(id: String, expr)) => memory(id) = exprHdlr(expr)
    case Pair(1, expr) => println(exprHdlr(expr))
    case Pair(2, _) => // do nothing
  }
  
  def progHdlr(p: Any) = p match {
    case lst: List[_] => lst.foreach(statHdlr)
  }

  def main(args: Array[String]) {
    val input1 = "a=3\n b=4\n a+b*5\n (a+b)*5\n"
    val vll = VllParsers.fromFile(new File("TDAR-Expr-AST.vll"))
printf("vll: %s%n", vll.getClass.getName)
//    val vll = VllParsers.fromString(grammar)
    val parser = vll.getParserFor("Prog")
printf("parser: %s%n", parser.getClass.getName)
    val parseResult = vll.parseAll(parser, input1) //phraseParser(new CharSequenceReader(input1))
printf("parseResult: %s%n", parseResult.getClass.getName)
    parseResult match {
      case vll.Success(resultAST, _) => progHdlr(resultAST)
      case vll.Failure(msg, where) => printf("Error: '%s' at line %d col %d%n", msg, where.pos.line, where.pos.column)
    }
  }
  val grammar = """
<VLL-Grammar>
  <Whitespace>[ \\t]+</Whitespace>
  <Comments></Comments>
  <Tokens>
    <Literal Name="RPAREN" Pattern=")"/>
    <Regex Name="INT" Pattern="\\d+"/>
    <Literal Name="MULT" Pattern="*"/>
    <Literal Name="LPAREN" Pattern="("/>
    <Regex Name="NEWLINE" Pattern="\\r?\\n"/>
    <Regex Name="ID" Pattern="[a-zA-Z]+"/>
    <Literal Name="MINUS" Pattern="-"/>
    <Literal Name="EQUALS" Pattern="="/>
    <Literal Name="PLUS" Pattern="+"/>
  </Tokens>
  <Parsers>
    <Parser Name="expr">
        <Sequence >
          <Reference Ref="multExpr" />
          <Sequence Mult="*">
            <Choice >
              <Token Ref="PLUS" />
              <Token Ref="MINUS" />
            </Choice>
            <Reference Ref="multExpr" />
          </Sequence>
        </Sequence>
    </Parser>
    <Parser Name="multExpr">
        <Sequence >
          <Reference Ref="atom" />
          <Sequence Mult="*">
            <Token Ref="MULT" Drop="true"/>
            <Reference Ref="atom" />
          </Sequence>
        </Sequence>
    </Parser>
    <Parser Name="atom">
        <Choice >
          <Token Ref="INT" />
          <Token Ref="ID" />
          <Sequence >
            <Token Ref="LPAREN" Drop="true"/>
            <Reference Ref="expr" />
            <Token Ref="RPAREN" Drop="true"/>
          </Sequence>
        </Choice>
    </Parser>
    <Parser Name="Prog">
        <Reference Ref="stat" Mult="+"/>
    </Parser>
    <Parser Name="stat">
        <Choice >
          <Sequence >
            <Token Ref="ID" />
            <Token Ref="EQUALS" Drop="true"/>
            <Reference Ref="expr" />
            <Token Ref="NEWLINE" Drop="true"/>
          </Sequence>
          <Sequence >
            <Reference Ref="expr" />
            <Token Ref="NEWLINE" Drop="true"/>
          </Sequence>
          <Token Ref="NEWLINE" />
        </Choice>
    </Parser>
  </Parsers>
</VLL-Grammar>
"""
}
