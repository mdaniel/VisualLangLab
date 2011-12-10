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

class TDARExpr(gui: VllGui) {

  def load() {
    val msg = """<html><body><pre>
This example is based on the parser described in 
section 3.1 "<i>Recognizing Language Syntax</i>" 
of the book "<i>The Definitive ANTLR Reference</i>" 
(http://pragprog.com/book/tpantlr/the-definitive-antlr-reference)
    
You can find sample test input in the book.

IMPORTANT: All test-input must end with an ENTER or RETURN 
as the parser expects a NEWLINE token at the end.

Read more at http://vll.java.net/Examples.html
    
The top-level parser-rule has been renamed to "Prog" 
(initial capital) so that it is presented first. 
</pre></body></html>"""

    gui.parsers.reset()
    gui.parsers.load(grammar)
    val firstRule = gui.parsers.ruleBank.ruleNames(0)
    gui.updateRuleChooser(firstRule)
    gui.title = "VisualLangLab/S - TDAR-Expr"
    gui.ruleTreePanel.setRule(firstRule)
    Dialog.showMessage(gui.contents(0), new Label(msg).peer,
        "VisualLangLab sample - TDAR-Expr", Dialog.Message.Info, null)
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
            <Token Ref="MULT" />
            <Reference Ref="atom" />
          </Sequence>
        </Sequence>
    </Parser>
    <Parser Name="atom">
        <Choice >
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
        <Reference Ref="stat" Mult="+"/>
    </Parser>
    <Parser Name="stat">
        <Choice >
          <Sequence >
            <Token Ref="ID" />
            <Token Ref="EQUALS" />
            <Reference Ref="expr" />
            <Token Ref="NEWLINE" />
          </Sequence>
          <Sequence >
            <Reference Ref="expr" />
            <Token Ref="NEWLINE" />
          </Sequence>
          <Token Ref="NEWLINE" />
        </Choice>
    </Parser>
  </Parsers>
</VLL-Grammar>
}

