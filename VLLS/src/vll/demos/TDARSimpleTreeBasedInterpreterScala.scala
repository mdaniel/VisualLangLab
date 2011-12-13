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

import vll.core.VllParsers

object TDARSimpleTreeBasedInterpreterScala {
  
  val memory = scala.collection.mutable.Map[String, BigInt]()
  val functionConstants = scala.collection.mutable.Map[Pair[String, BigInt], BigInt]()
  val functionFormulae = scala.collection.mutable.Map[String, Pair[String, Any]]()

  def functionCall(id: String, e: Any): BigInt = {
    val argValue = exprHdlr(e)
    if (functionConstants.contains(Pair(id, argValue)))
      functionConstants(Pair(id, argValue))
    else if (functionFormulae.contains(id)) {
      val (arg2, expr) = functionFormulae(id)
      val oldVal: Option[BigInt] = if (memory.contains(arg2)) Some(memory(arg2)) else None
      memory(arg2) = argValue
      val retVal = exprHdlr(expr)
      oldVal match {
        case Some(v) => memory(arg2) = v
        case None => memory.remove(arg2)
      }
      retVal
    } else {
      printf("undefined function: %s%n", id)
      0
    }
  }

  def funcHdlr(f: Any) = f match {
    case Array(id: String, fp, e) => fp match {
      case Pair(0, arg: String) => functionFormulae(id) = Pair(arg, e)
      case Pair(1, i: String) => functionConstants(Pair(id, i.toInt)) = exprHdlr(e)
    }
  }

  def atomHdlr(xa: Any): BigInt = xa match {
    case Pair(0, Array(id: String, expr)) => functionCall(id, expr)
    case Pair(1, expr) => exprHdlr(expr)
    case Pair(2, s: String) => s.toInt
    case Pair(3, id: String) => 
      if (memory.contains(id)) memory(id)
      else {println("undefined: " + id); 0}
  }

  def multExprHdlr(xme: Any): BigInt = xme match {
    case Array(mexpr, lst: List[_]) => 
      var result = atomHdlr(mexpr)
      lst.foreach(_ match {
          case Array(Pair(0, _), mexpr2) => result *= atomHdlr(mexpr2)
          case Array(Pair(1, _), mexpr2) => result /= atomHdlr(mexpr2)
          case Array(Pair(2, _), mexpr2) => result %= atomHdlr(mexpr2)
        })
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
    case Pair(1, func) => funcHdlr(func)
    case Pair(2, expr) => println(exprHdlr(expr))
    case Pair(3, _) => // do nothing
  }
  
  def progHdlr(p: Any) = p match {
    case lst: List[_] => lst.foreach(statHdlr)
  }

  def main(args: Array[String]) {
//    val input1 = "f(0)=1\n f(x)=f(x-1)*x\n a=3\n b=4\n a+b*5\n f(5)\n"
    val input1 = "f(0)=1\n f(x)=f(x-1)*x\n a=3\n b=4\n (a+b)*5\n f(a+b)*5\n"
    val vll = VllParsers.fromString(grammar)
    val parser = vll.getParserFor("Prog")
    val parseResult = vll.parseAll(parser, input1) //phraseParser(new CharSequenceReader(input1))
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
    <Literal Name="MOD" Pattern="%"/>
    <Literal Name="LPAREN" Pattern="("/>
    <Regex Name="NEWLINE" Pattern="\\r?\\n"/>
    <Regex Name="ID" Pattern="[a-zA-Z]+"/>
    <Literal Name="MINUS" Pattern="-"/>
    <Literal Name="EQUALS" Pattern="="/>
    <Literal Name="DIV" Pattern="/"/>
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
            <Choice >
              <Token Ref="MULT" />
              <Token Ref="DIV" />
              <Token Ref="MOD" />
            </Choice>
            <Reference Ref="atom" />
          </Sequence>
        </Sequence>
    </Parser>
    <Parser Name="func">
        <Sequence >
          <Token Ref="ID" />
          <Token Ref="LPAREN" Drop="true"/>
          <Reference Ref="formalPar" />
          <Token Ref="RPAREN" Drop="true"/>
          <Token Ref="EQUALS" Drop="true"/>
          <Reference Ref="expr" />
        </Sequence>
    </Parser>
    <Parser Name="atom">
        <Choice >
          <Sequence >
            <Token Ref="ID" />
            <Token Ref="LPAREN" Drop="true"/>
            <Reference Ref="expr" />
            <Token Ref="RPAREN" Drop="true"/>
          </Sequence>
          <Sequence >
            <Token Ref="LPAREN" Drop="true"/>
            <Reference Ref="expr" />
            <Token Ref="RPAREN" Drop="true"/>
          </Sequence>
          <Token Ref="INT" />
          <Token Ref="ID" />
        </Choice>
    </Parser>
    <Parser Name="Prog">
        <Reference Ref="stat" Mult="+" />
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
            <Reference Ref="func" />
            <Token Ref="NEWLINE" Drop="true"/>
          </Sequence>
          <Sequence >
            <Reference Ref="expr" />
            <Token Ref="NEWLINE" Drop="true"/>
          </Sequence>
          <Token Ref="NEWLINE" />
        </Choice>
    </Parser>
    <Parser Name="formalPar">
        <Choice >
          <Token Ref="ID" />
          <Token Ref="INT" />
        </Choice>
    </Parser>
  </Parsers>
</VLL-Grammar>
"""
}
