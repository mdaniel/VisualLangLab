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

package vll.gui.samples

import scala.swing.Dialog
import scala.swing.Label
import vll.gui.VllGui

class TDARSimpleTreeBasedInterpreter(gui: VllGui) {

  def load() {
    val msg = """<html><body><pre>
This example is based on the parser described at the very end of 
section 3.3, <i>Evaluating Expressions Using an AST Intermediate Form</i>, 
of the book <i>The Definitive ANTLR Reference</i> 
(http://pragprog.com/book/tpantlr/the-definitive-antlr-reference)
Complete details can be found here:
http://www.antlr.org/wiki/display/ANTLR3/Simple+tree-based+interpeter
    
You can find sample test input at the web-page above.

IMPORTANT: The parser needs a NEWLINE token at the end of input.

Read more at http://vll.java.net/examples/a-quick-tour.html
    
The top-level parser-rule has been renamed to "Prog" 
(initial capital) so that it is presented first. 
</pre></body></html>"""

    gui.parsers.reset()
    gui.parsers.load(grammar)
    val firstRule = gui.parsers.ruleBank.ruleNames(0)
    gui.updateRuleChooser(firstRule)
    gui.title = "VisualLangLab/S - TDAR-Simple-Tree-Based-Interpreter"
    gui.ruleTreePanel.setRule(firstRule)
    Dialog.showMessage(gui.contents(0), new Label(msg).peer,
        "VisualLangLab sample - TDAR-Simple-Tree-Based-Interpreter", Dialog.Message.Info, null)
  }

  val grammar = <VLL-Grammar>
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
        <Reference Ref="stat" Mult="+" ActionText="(x:Any) =&gt; {&#xA;  val memory = scala.collection.mutable.Map[String, BigInt]()&#xA;  val functionConstants = scala.collection.mutable.Map[Pair[String, BigInt], BigInt]()&#xA;  val functionFormulae = scala.collection.mutable.Map[String, Pair[String, Any]]()&#xA;&#xA;  def functionCall(id: String, e: Any): BigInt = {&#xA;    val argValue = exprHdlr(e)&#xA;    if (functionConstants.contains(Pair(id, argValue)))&#xA;      functionConstants(Pair(id, argValue))&#xA;    else if (functionFormulae.contains(id)) {&#xA;      val (arg2, expr) = functionFormulae(id)&#xA;      val oldVal: Option[BigInt] = if (memory.contains(arg2)) Some(memory(arg2)) else None&#xA;      memory(arg2) = argValue&#xA;      val retVal = exprHdlr(expr)&#xA;      oldVal match {&#xA;        case Some(v) =&gt; memory(arg2) = v&#xA;        case None =&gt; memory.remove(arg2)&#xA;      }&#xA;      retVal&#xA;    } else {&#xA;      printf(&quot;undefined function: %s%n&quot;, id)&#xA;      0&#xA;    }&#xA;  }&#xA;&#xA;  def functHdlr(f: Any) = f match {&#xA;    case Array(id: String, fp, e) =&gt; fp match {&#xA;      case Pair(0, arg: String) =&gt; functionFormulae(id) = Pair(arg, e)&#xA;      case Pair(1, i: String) =&gt; functionConstants(Pair(id, i.toInt)) = exprHdlr(e)&#xA;    }&#xA;  }&#xA;&#xA;  def atomHdlr(xa: Any): BigInt = xa match {&#xA;    case Pair(0, Array(id: String, expr)) =&gt; functionCall(id, expr)&#xA;    case Pair(1, expr) =&gt; exprHdlr(expr)&#xA;    case Pair(2, s: String) =&gt; s.toInt&#xA;    case Pair(3, id: String) =&gt; &#xA;      if (memory.contains(id)) memory(id)&#xA;      else {println(&quot;undefined: &quot; + id); 0}&#xA;  }&#xA;&#xA;  def multExprHdlr(xme: Any): BigInt = xme match {&#xA;    case Array(mexpr, lst: List[_]) =&gt; &#xA;      var result = atomHdlr(mexpr)&#xA;      lst.foreach(_ match {&#xA;          case Array(Pair(0, _), mexpr2) =&gt; result *= atomHdlr(mexpr2)&#xA;          case Array(Pair(1, _), mexpr2) =&gt; result /= atomHdlr(mexpr2)&#xA;          case Array(Pair(2, _), mexpr2) =&gt; result %= atomHdlr(mexpr2)&#xA;        })&#xA;      result&#xA;  }&#xA;&#xA;  def exprHdlr(xe: Any): BigInt = xe match {&#xA;    case Array(mexpr, lst: List[_]) =&gt; &#xA;      var result = multExprHdlr(mexpr)&#xA;      lst.foreach(_ match {&#xA;          case Array(Pair(0, _), mexpr2) =&gt; result += multExprHdlr(mexpr2)&#xA;          case Array(Pair(1, _), mexpr2) =&gt; result -= multExprHdlr(mexpr2)&#xA;        })&#xA;      result&#xA;  }&#xA;&#xA;  def statHdlr(s: Any) = s match {&#xA;    case Pair(0, Array(id: String, expr)) =&gt; memory(id) = exprHdlr(expr)&#xA;    case Pair(1, func) =&gt; functHdlr(func)&#xA;    case Pair(2, expr) =&gt; println(exprHdlr(expr))&#xA;    case Pair(3, _) =&gt; // do nothing&#xA;  }&#xA;  &#xA;  def progHdlr(p: Any) = p match {&#xA;    case lst: List[_] =&gt; lst.foreach(statHdlr)&#xA;  }&#xA;&#xA;  x match {&#xA;    case null =&gt; // do nothing&#xA;    case ast =&gt; progHdlr(ast)&#xA;  }&#xA;}"/>
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
}

