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

class SimpleJSON(gui: VllGui) {

  def load() {
    val msg = """<html><body><pre>
SimpleJSON is based on the parser described at page 764 of
"Programming in Scala" (http://www.artima.com/shop/programming_in_scala_2ed)
A description of the same parser can also be found here:
http://lamp.epfl.ch/teaching/foundations_of_software/docs/combinator_parsing.pdf

IMPORTANT: Remember to have the top-level parser (Value) selected when running
the parser.

You can find sample input in the book or PDF document referred above.
</pre></body></html>"""

    gui.parsers.load(grammar)
    val firstRule = gui.parsers.ruleBank.ruleNames(0)
    gui.updateRuleChooser(firstRule)
    gui.title = "VisualLangLab/S - SimpleJSON"
    gui.ruleTreePanel.setRule(firstRule)
    gui.logTextPane.clearLogText()
    Dialog.showMessage(gui.contents(0), new Label(msg).peer,
        "VisualLangLab sample - SimpleJSON", Dialog.Message.Info, null)
  }

  val grammar = <VLL-Grammar>
  <Whitespace>[ \t\n\013\f\r]+</Whitespace>
  <Comments></Comments>
  <Tokens>
    <Literal Name="COLON" Pattern=":"/>
    <Literal Name="COMMA" Pattern=","/>
    <Literal Name="FALSE" Pattern="false"/>
    <Literal Name="LBKT" Pattern="["/>
    <Literal Name="LCURLY" Pattern="{"/>
    <Literal Name="NULL" Pattern="null"/>
    <Literal Name="RBKT" Pattern="]"/>
    <Literal Name="RCURLY" Pattern="}"/>
    <Literal Name="TRUE" Pattern="true"/>
    <Regex Name="floatingPointNumber" Pattern="\\-?(\d+(\\.\d*)?|\d*\\.\d+)([eE][+-]?\d+)?[fFdD]?"/>
    <Regex Name="stringLiteral" Pattern="\&quot;[^&quot;]*\&quot;"/>
  </Tokens>
  <Parsers>
    <Parser Name="arr">
        <Sequence Mult="1" ErrMsg="">
          <Token Ref="LBKT" Mult="1" ErrMsg=""/>
          <RepSep Mult="*" ErrMsg="">
            <Reference Ref="Value" Mult="1" ErrMsg=""/>
            <Token Ref="COMMA" Mult="1" ErrMsg=""/>
          </RepSep>
          <Token Ref="RBKT" Mult="1" ErrMsg=""/>
        </Sequence>
    </Parser>
    <Parser Name="member">
        <Sequence Mult="1" ErrMsg="">
          <Token Ref="stringLiteral" Mult="1" ErrMsg=""/>
          <Token Ref="COLON" Mult="1" ErrMsg=""/>
          <Reference Ref="Value" Mult="1" ErrMsg=""/>
        </Sequence>
    </Parser>
    <Parser Name="obj">
        <Sequence Mult="1" ErrMsg="">
          <Token Ref="LCURLY" Mult="1" ErrMsg=""/>
          <RepSep Mult="*" ErrMsg="">
            <Reference Ref="member" Mult="1" ErrMsg=""/>
            <Token Ref="COMMA" Mult="1" ErrMsg=""/>
          </RepSep>
          <Token Ref="RCURLY" Mult="1" ErrMsg=""/>
        </Sequence>
    </Parser>
    <Parser Name="Value">
        <Choice Mult="1" ErrMsg="illegal start of value">
          <Reference Ref="obj" Mult="1" ErrMsg=""/>
          <Reference Ref="arr" Mult="1" ErrMsg=""/>
          <Token Ref="stringLiteral" Mult="1" ErrMsg=""/>
          <Token Ref="floatingPointNumber" Mult="1" ErrMsg=""/>
          <Token Ref="NULL" Mult="1" ErrMsg=""/>
          <Token Ref="TRUE" Mult="1" ErrMsg=""/>
          <Token Ref="FALSE" Mult="1" ErrMsg=""/>
        </Choice>
    </Parser>
  </Parsers>
</VLL-Grammar>
}

