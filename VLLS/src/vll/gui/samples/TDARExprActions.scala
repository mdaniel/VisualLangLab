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

class TDARExprActions(gui: VllGui) {

  def load() {
    val msg = """<html><body><pre>
This example is based on the parser described in 
section 3.2 "<i>Using Syntax to Drive Action Execution</i>" 
of the book "<i>The Definitive ANTLR Reference</i>" 
(http://pragprog.com/book/tpantlr/the-definitive-antlr-reference)

Actions (written as JavaScript and Scala functions) are associated  
with various parse-tree nodes (which can be identified by an "action" 
attribute near the icon).
    
You can find sample test input in the book.

Read more at http://vll.java.net/Examples.html
    
IMPORTANT: All test-input must end with an ENTER or RETURN 
as the parser expects a NEWLINE token at the end.
    
The top-level parser-rule has been renamed to "Prog" 
(initial capital) so that it is presented first. 
</pre></body></html>"""

    gui.parsers.reset()
    gui.parsers.load(grammar)
    val firstRule = gui.parsers.ruleBank.ruleNames(0)
    gui.updateRuleChooser(firstRule)
    gui.title = "VisualLangLab/S - TDAR-Expr-Actions"
    gui.ruleTreePanel.setRule(firstRule)
    Dialog.showMessage(gui.contents(0), new Label(msg).peer,
        "VisualLangLab sample - TDAR-Expr-Actions", Dialog.Message.Info, null)
  }

  val grammar = <VLL-Grammar>
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
        <Sequence ActionText="(arg: Any) =&gt; arg match {&#xA;  case Array(multExpr: Double, lst: List[_]) =&gt;&#xA;    var result = multExpr&#xA;    lst foreach (_ match {&#xA;        case Array(Pair(d: Int, _), mExpr: Double) =&gt;&#xA;          d match {&#xA;            case 0 =&gt; result += mExpr&#xA;            case 1 =&gt; result -= mExpr&#xA;          }&#xA;      }&#xA;    )&#xA;  result&#xA;  case null =&gt; arg  // Do nothing&#xA;}">
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
        <Sequence ActionText="(arg: Any) =&gt; arg match {&#xA;  case Array(atom: Double, lst: List[_]) =&gt;&#xA;    var result = atom&#xA;    lst foreach (_ match {&#xA;        case Array(&quot;*&quot;, a: Double) =&gt;&#xA;          result *= a&#xA;      }&#xA;    )&#xA;  result&#xA;  case null =&gt; arg  // Do nothing&#xA;}">
          <Reference Ref="atom" />
          <Sequence Mult="*">
            <Token Ref="MULT" />
            <Reference Ref="atom" />
          </Sequence>
        </Sequence>
    </Parser>
    <Parser Name="atom">
        <Choice ActionText="function (arg) {&#xA;  if (arg) {&#xA;    switch (arg[0]) {&#xA;      case 0: &#xA;        return parseInt(arg[1]); &#xA;      case 1:&#xA;        if (VLL.memory.hasOwnProperty(arg[1])) {&#xA;          return VLL.memory[arg[1]]; &#xA;        } else {&#xA;          println(&quot;undefined variable &quot; +&#xA;              arg[1]);&#xA;          return 0;&#xA;        }&#xA;      case 2: &#xA;        return arg[1][1]; &#xA;    }&#xA;  }&#xA;}">
          <Token Ref="INT" />
          <Token Ref="ID" />
          <Sequence >
            <Token Ref="LPAREN" />
            <Reference Ref="expr" />
            <Token Ref="RPAREN" />
          </Sequence>
        </Choice>
    </Parser>
    <Parser Name="Prog">
        <Reference Ref="stat" Mult="+" ActionText="function (arg) {&#xA;  if (!arg) {&#xA;    VLL.memory = {};&#xA;  } else {&#xA;    return &quot;Ok&quot;;&#xA;  }&#xA;}"/>
    </Parser>
    <Parser Name="stat">
        <Choice ActionText="function (arg) {&#xA;  if (arg) {&#xA;    switch (arg[0]) {&#xA;      case 0: &#xA;        println(arg[1][0]); break;&#xA;      case 1:&#xA;        VLL.memory[arg[1][0]] = arg[1][2]; &#xA;        break;&#xA;      case 2: &#xA;        return arg; &#xA;    }&#xA;  }&#xA;}">
          <Sequence >
            <Reference Ref="expr" />
            <Token Ref="NEWLINE" />
          </Sequence>
          <Sequence >
            <Token Ref="ID" />
            <Token Ref="EQUALS" />
            <Reference Ref="expr" />
            <Token Ref="NEWLINE" />
          </Sequence>
          <Token Ref="NEWLINE" />
        </Choice>
    </Parser>
  </Parsers>
</VLL-Grammar>
}

