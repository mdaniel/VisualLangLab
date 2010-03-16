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

package net.java.dev.vll.samples

import net.java.dev.vll.VllParsers._
import net.java.dev.vll.FileIO
import net.java.dev.vll.TextPane
import net.java.dev.vll.VisualLangLab
import scala.swing.Dialog

object SimpleJSON {

  def load() {
    val msg = """SimpleJSON implements the parser described at page 648 of
"Programming in Scala" (http://www.artima.com/shop/programming_in_scala)
A description of the same parser can also be found here:
http://lamp.epfl.ch/teaching/foundations_of_software/docs/combinator_parsing.pdf

The SimpleJSON parsers have been loaded -- into the tree pane on the left.
use the combo-box above the tree pane to choose different sub-parsers.

IMPORTANT: Remember to have the top-level parser (value) selected when running
the parser.

This parser also uses a custom error message. To see an example
select the parser called "value", right-click the choice element
at the top of the parser, and choose "Error message" from the context menu.
Custom error messages are discussed at page 666 of "Programming in Scala",
but VisualLangLab uses a completely different (and more flexible) technique.
VisualLangLab's custom error messages can be applied to any grammar tree element.

You can find sample input in the book or PDF document referred above."""

    FileIO.load(grammar)
    TextPane.clearLogText()
    println(msg)
    Dialog.showMessage(VisualLangLab.splitPane, 
        "SimpleJSON - a simple JSON parser\nPlease read notes in log window",
        "VisualLangLab sample - SimpleJSON", Dialog.Message.Info, null)
  }

  val grammar = <VLL-Grammar>
  <Whitespace>\s+</Whitespace>
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
    <Regex Name="floatingPointNumber" Pattern="-?(\d+(\.\d*)?|\d*\.\d+)([eE][+-]?\d+)?[fFdD]?"/>
    <Regex Name="stringLiteral" Pattern="&quot;([^&quot;\p{Cntrl}\\]|\\[\\/bfnrt]|\\u[a-fA-F0-9]{4})*&quot;"/>
  </Tokens>
  <Parsers>
    <Parser Name="arr">
        <Sequence Mult="1" ErrMsg="">
          <Token Ref="LBKT" Mult="1" ErrMsg=""/>
          <RepSep Mult="*" ErrMsg="">
            <Reference Ref="value" Mult="1" ErrMsg=""/>
            <Token Ref="COMMA" Mult="1" ErrMsg=""/>
          </RepSep>
          <Token Ref="RBKT" Mult="1" ErrMsg=""/>
        </Sequence>
    </Parser>
    <Parser Name="member">
        <Sequence Mult="1" ErrMsg="">
          <Token Ref="stringLiteral" Mult="1" ErrMsg=""/>
          <Token Ref="COLON" Mult="1" ErrMsg=""/>
          <Reference Ref="value" Mult="1" ErrMsg=""/>
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
    <Parser Name="value">
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

