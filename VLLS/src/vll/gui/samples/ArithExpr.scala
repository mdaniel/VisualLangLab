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

class ArithExpr(val gui: VllGui) {

  def load() {
    val msg = """<html><body><pre>
ArithExpr implements the parser described at page 760 of "Programming in Scala, 
Second Edition" (http://www.artima.com/shop/programming_in_scala_2ed)
A description of the same parser can also be found here:
http://lamp.epfl.ch/teaching/foundations_of_software/docs/combinator_parsing.pdf

Another version of this parser with action-code that evaluages the 
expression to a number is also packaged with VisualLangLab.

IMPORTANT: Remember to have the top-level parser (Expr) selected when running
the parser.

Sample input (remove quotes): "(2 + 3) * (7 - 3)"
Output: Array(Array(Pair(1, Array(Array(Pair(0, 2), List()), 
    List(Pair(0, Array(Pair(0, 3), List()))))), List(Pair(0, 
    Pair(1, Array(Array(Pair(0, 7), List()), List(Pair(1, 
    Array(Pair(0, 3), List())))))))), List())

</pre></body></html>"""
    gui.parsers.load(grammar)
    val firstRule = gui.parsers.ruleBank.ruleNames(0)
    gui.updateRuleChooser(firstRule)
    gui.title = "VisualLangLab/S - ArithExpr"
    gui.ruleTreePanel.setRule(firstRule)
    gui.logTextPane.clearLogText()
    Dialog.showMessage(gui.contents(0), new Label(msg).peer,
        "VisualLangLab sample - ArithExpr", Dialog.Message.Info, null)
  }

  val grammar = <VLL-Grammar>
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
        <Sequence Description="The &apos;expr&apos; parser-rule&apos;s structure">
          <Reference Ref="term" />
          <Choice Mult="*" Description="PLUS or MINUS">
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
        <Choice Description="The &apos;factor&apos; parser-rule&apos;s structure">
          <Token Ref="floatingPointNumber" />
          <Sequence >
            <Token Ref="LPAREN" Drop="true"/>
            <Reference Ref="Expr" />
            <Token Ref="RPAREN" Drop="true"/>
          </Sequence>
        </Choice>
    </Parser>
    <Parser Name="term">
        <Sequence Description="The &apos;term&apos; parser-rule&apos;s structure">
          <Reference Ref="factor" />
          <Choice Mult="*" Description="MULT or DIV">
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

