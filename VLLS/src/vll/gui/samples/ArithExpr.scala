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

package vll.gui.samples

import scala.swing.Dialog
import scala.swing.Label
import vll.gui.VllGui

class ArithExpr(val gui: VllGui) {

  def load() {
    val msg = """<html><body><pre>
ArithExpr implements the parser described at page 644 of
"Programming in Scala" (http://www.artima.com/shop/programming_in_scala)
A description of the same parser can also be found here:
http://lamp.epfl.ch/teaching/foundations_of_software/docs/combinator_parsing.pdf

The ArithExpr demo has been loaded. Use the combo-box above the tree pane to
choose different sub-parsers.

IMPORTANT: Remember to have the top-level parser (Expr) selected when running
the parser.

A parse-tree processor that evaluates the arithmetic expressions can be 
installed (optionally) by choosing "Run -> Tree handler -> Custom",
and entering "treehandler.ArithExpr" into the dialog presented.

Code for the expression handler (in the class mentioned above) is here:
https://vll.dev.java.net/source/browse/*checkout*/vll/trunk/VLL-java-dot-net/
src/treehandler/ArithExpr.scala

Sample input (remove quotes): "(2 + 3) * (7 - 3)"
Output (without tree handler): (((((~((2~List())~List((+~(3~List())))))~))
~List((*~(((~((7~List())~List((-~(3~List())))))~)))))~List())

Output (with tree handler): 20.0
</pre></body></html>"""
    gui.parsers.load(grammar)
    val firstParser = gui.parsers.parserBank.parserNames(0)
    gui.updateParserChooser(firstParser)
    gui.title = "VisualLangLab/S - ArithExpr"
    gui.parserTreePanel.setParser(firstParser)
    gui.logTextPane.clearLogText()
    Dialog.showMessage(gui.contents(0), new Label(msg).peer,
        "VisualLangLab sample - ArithExpr", Dialog.Message.Info, null)
  }

  val grammar = <VLL-Grammar>
  <Whitespace>\s+</Whitespace>
  <Comments></Comments>
  <Tokens>
    <Literal Name="RPAREN" Pattern=")"/>
    <Regex Name="floatingPointNumber" Pattern="(\d+(\\.\d*)?|\d*\\.\d+)([eE][+-]?\d+)?[fFdD]?"/>
    <Literal Name="MULT" Pattern="*"/>
    <Literal Name="LPAREN" Pattern="("/>
    <Literal Name="MINUS" Pattern="-"/>
    <Literal Name="DIV" Pattern="/"/>
    <Literal Name="PLUS" Pattern="+"/>
  </Tokens>
  <Parsers>
    <Parser Name="Expr">
        <Sequence ActionText="function (arg) {&#xA;  if (!arg) {&#xA;    return;&#xA;  }&#xA;  var expr = arg[0];&#xA;  var list = arg[1];&#xA;  for (var i = 0; i &lt; list.length; ++i) {&#xA;    var pair = list[i];&#xA;    switch (pair[0]) {&#xA;    case 0:&#xA;      expr += pair[1]; break;&#xA;    case 1:&#xA;      expr -= pair[1]; break;&#xA;    }&#xA;  }&#xA;  return expr;&#xA;}">
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
        <Choice ActionText="function (arg) {&#xA;  if (!arg) {&#xA;    return;&#xA;  }&#xA;  switch (arg[0]) {&#xA;  case 0:&#xA;    return parseFloat(arg[1]); break;&#xA;  case 1: &#xA;    return arg[1]; break;&#xA;  } &#xA;}">
          <Token Ref="floatingPointNumber" />
          <Sequence >
            <Token Ref="LPAREN" Drop="true"/>
            <Reference Ref="Expr" />
            <Token Ref="RPAREN" Drop="true"/>
          </Sequence>
        </Choice>
    </Parser>
    <Parser Name="term">
        <Sequence ActionText="(a: Any) =&gt; a match {&#xA;  case null =&gt; null&#xA;  case Array(f: Double, lst:List[_]) =&gt; &#xA;    var res = f&#xA;    lst.foreach(_ match {&#xA;      case Pair(0, f2:Double) =&gt; res *= f2&#xA;      case Pair(1, f2:Double) =&gt; res /= f2&#xA;    })&#xA;    res&#xA;}">
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

}

