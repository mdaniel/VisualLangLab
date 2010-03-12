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

import net.java.dev.vll.Parsers._
import net.java.dev.vll.FileIO
import net.java.dev.vll.VisualLangLab
import scala.swing.Dialog

object Arith {

  def load() {
    val msg = """
Arith is the arithmetic expression parser described at page 644 of
"Programming in Scala" (http://www.artima.com/shop/programming_in_scala)

A description of the parser can also be found at the following link:
http://lamp.epfl.ch/teaching/foundations_of_software/docs/combinator_parsing.pdf

The Arith parsers have been loaded into the IDE, and you can review them
by using the tree pane (on the left), and the drop-down combo-box above it.

A parse-tree processor that evaluates the arithmetic expressions can be 
installed (optionally) by choosing "Run -> Tree handler -> Custom",
and entering "net.java.dev.vll.treehandler.Arith" into the dialog presented.

Code for the expression handler (in the class mentioned above) can be found here:
https://vll.dev.java.net/source/browse/*checkout*/vll/trunk/VLL-java-dot-net/src/net/java/dev/vll/samples/Arith.scala
"""
    FileIO.load(grammar)
    println(msg)
    Dialog.showMessage(VisualLangLab.splitPane, 
        "Arith - an arithmetic expression parser\nPlease read notes in log window",
        "VisualLangLab sample - Arith", Dialog.Message.Info, null)
  }

  val grammar = <VLL-Grammar>
    <Whitespace>\s+</Whitespace>
    <Comments></Comments>
    <Tokens>
      <Literal Name="DIV" Pattern="/"/>
      <Literal Name="LPAREN" Pattern="("/>
      <Literal Name="MINUS" Pattern="-"/>
      <Literal Name="MULT" Pattern="*"/>
      <Literal Name="PLUS" Pattern="+"/>
      <Literal Name="RPAREN" Pattern=")"/>
      <Regex Name="floatingPointNumber" Pattern="-?(\d+(\.\d*)?|\d*\.\d+)([eE][+-]?\d+)?[fFdD]?"/>
    </Tokens>
    <Parsers>
      <Parser Name="expr">
        <Sequence Mult="1">
          <Reference Ref="term" Mult="1"/>
          <Choice Mult="*">
            <Sequence Mult="1">
              <Token Ref="PLUS" Mult="1"/>
              <Reference Ref="term" Mult="1"/>
            </Sequence>
            <Sequence Mult="1">
              <Token Ref="MINUS" Mult="1"/>
              <Reference Ref="term" Mult="1"/>
            </Sequence>
          </Choice>
        </Sequence>
      </Parser>
      <Parser Name="factor">
        <Choice Mult="1">
          <Token Ref="floatingPointNumber" Mult="1"/>
          <Sequence Mult="1">
            <Token Ref="LPAREN" Mult="1"/>
            <Reference Ref="expr" Mult="1"/>
            <Token Ref="RPAREN" Mult="1"/>
          </Sequence>
        </Choice>
      </Parser>
      <Parser Name="term">
        <Sequence Mult="1">
          <Reference Ref="factor" Mult="1"/>
          <Choice Mult="*">
            <Sequence Mult="1">
              <Token Ref="MULT" Mult="1"/>
              <Reference Ref="factor" Mult="1"/>
            </Sequence>
            <Sequence Mult="1">
              <Token Ref="DIV" Mult="1"/>
              <Reference Ref="factor" Mult="1"/>
            </Sequence>
          </Choice>
        </Sequence>
      </Parser>
    </Parsers>
  </VLL-Grammar>
}

