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

package net.java.dev.vll

import java.io.File
import java.io.PrintStream
import scala.xml.Elem
import scala.xml.Node
import scala.xml.NodeSeq
import scala.xml.XML

object FileIO {

  def encode(s: String): String = {
    val sb = new StringBuilder()
    s foreach (_ match {
        case '&' => sb.append("&amp;")
        case '<' => sb.append("&lt;")
        case '>' => sb.append("&gt;")
        case '"' => sb.append("&quot;")
        case x => sb.append(x)
      }
    )
    sb.toString
  }

  val decoder = Map("&amp;" -> '&', "&gt;" -> '>', "&lt;" -> '<', "&quot;" -> '"')

  def decode(s: String): String = {
    val sb = new StringBuilder()
    val esc = new StringBuilder()
    var escaping = false
    s foreach (c =>
      if (escaping) {
        esc.append(c)
        if (c == ';') {
          sb.append(decoder(esc.toString))
          esc.clear()
          escaping = false
        }
      } else {
        if (c == '&') {
          escaping = true
          esc.append(c)
        } else
          sb.append(c)
      }
    )
    sb.toString
  }

  def printParserTree(t: ParserTreeNode, level: Int) {
    val margin = "  " * (2 + level)
    t match {
      case t: LiteralNode => out.printf("%s<Token Ref=\"%s\" Mult=\"%s\"/>%n", margin, encode(t.literalName), t.multiplicity)
      case t: RegexNode => out.printf("%s<Token Ref=\"%s\" Mult=\"%s\"/>%n", margin, encode(t.regexName), t.multiplicity)
      case r: ReferenceNode => out.printf("%s<Reference Ref=\"%s\" Mult=\"%s\"/>%n", margin, encode(r.parserName), r.multiplicity)
      case s: RootNode =>
        //out.printf("%s<Root>%n", margin)
        for (i <- 0 until s.getChildCount)
          printParserTree(s.getChildAt(i).asInstanceOf[ParserTreeNode], level + 1)
        //out.printf("%s</Root>%n", margin)
      case s: SequenceNode =>
        out.printf("%s<Sequence Mult=\"%s\">%n", margin, s.multiplicity)
        for (i <- 0 until s.getChildCount)
          printParserTree(s.getChildAt(i).asInstanceOf[ParserTreeNode], level + 1)
        out.printf("%s</Sequence>%n", margin)
      case rs: RepSepNode =>
        out.printf("%s<RepSep Mult=\"%s\">%n", margin, rs.multiplicity)
        for (i <- 0 until rs.getChildCount)
          printParserTree(rs.getChildAt(i).asInstanceOf[ParserTreeNode], level + 1)
        out.printf("%s</RepSep>%n", margin)
      case c: ChoiceNode =>
        out.printf("%s<Choice Mult=\"%s\">%n", margin, c.multiplicity)
        for (i <- 0 until c.getChildCount)
          printParserTree(c.getChildAt(i).asInstanceOf[ParserTreeNode], level + 1)
        out.printf("%s</Choice>%n", margin)
      case x => System.err.printf("ERROR: Unknown type in parser-tree: %s%n", x)
    }
  }

  def save(file: File) {
    out = new PrintStream(file)
    out.println("<VLL-Grammar>")

    out.print("  <Whitespace>")
    out.print(encode(Parsers.customWhitespace.toString))
    out.println("</Whitespace>")

    out.print("  <Comments>")
    out.print(encode(Parsers.comments.toString))
    out.println("</Comments>")

    out.println("  <Tokens>")
    for ((name, either) <- TokenBank.iterator) either match {
      case left: Left[String, String] =>
        out.printf("    <Literal Name=\"%s\" Pattern=\"%s\"/>%n", encode(name), encode(left.a))
      case right: Right[String, String] =>
        out.printf("    <Regex Name=\"%s\" Pattern=\"%s\"/>%n", encode(name), encode(right.b))
    }
    out.println("  </Tokens>")

    out.println("  <Parsers>")
    for ((name, node) <- ParserBank.iterator) {
      out.printf("    <Parser Name=\"%s\">%n", encode(name))
      printParserTree(node, 1)
      out.printf("    </Parser>%n")
    }
    out.println("  </Parsers>")

    out.println("</VLL-Grammar>")
    out.close()
  }

  def getTokens(tokens: NodeSeq) {
    TokenBank.clear
    val literals = tokens \ "Literal"
    //printf("literals-size: %d%n", literals.size)
    for (parser <- literals) {
      val name = decode((parser \ "@Name").toString)
      val pattern = decode((parser \ "@Pattern").toString)
      //printf("Name=%s Pattern=%s%n", name, pattern)
      TokenBank(name) = Left(pattern)
    }
    val regex = tokens \ "Regex"
    //printf("regexs-size: %d%n", regex.size)
    for (parser <- regex) {
      val name = decode((parser \ "@Name").toString)
      val pattern = decode((parser \ "@Pattern").toString)
      //printf("Name=%s Pattern=%s%n", name, pattern)
      TokenBank(name) = Right(pattern)
    }
  }

  def traverse(node: Node, parent: ParserTreeNode): ParserTreeNode = {
    var aNode: ParserTreeNode = null
    node match {
      case <Reference></Reference> =>
        val ref = decode((node \ "@Ref").toString)
        val mult = (node \ "@Mult").toString
        //printf("  Reference: %s, %s%n", ref, mult)
        aNode = new ReferenceNode(ref, strMap(mult))
        parent.add(aNode)
      case <Token></Token> => 
        val ref = decode((node \ "@Ref").toString)
        val mult = (node \ "@Mult").toString
        //printf("  Token: %s, %s%n", ref, mult)
        TokenBank.get(ref) match {
          case Some(Left(_)) => aNode = new LiteralNode(ref, strMap(mult))
          case Some(Right(_)) => aNode = new RegexNode(ref, strMap(mult))
          case None => println("Token not found")
        }
        //aNode = new TokenTreeNode(ref, strMap(mult))
        parent.add(aNode)
      case <Sequence>{contents @ _*}</Sequence> => 
        val mult = (node \ "@Mult").toString
        //printf("  Sequence: %s%n", mult)
        aNode = new SequenceNode(strMap(mult))
        if (parent != null)
          parent.add(aNode)
        for (c <- node.child) traverse(c, aNode)
      case <RepSep>{contents @ _*}</RepSep> =>
        val mult = (node \ "@Mult").toString
        //printf("  Sequence: %s%n", mult)
        aNode = new RepSepNode(strMap(mult))
        if (parent != null)
          parent.add(aNode)
        for (c <- node.child) traverse(c, aNode)
      case <Choice>{contents @ _*}</Choice> => 
        val mult = (node \ "@Mult").toString
        //printf("  Choice: %s%n", mult)
        aNode = new ChoiceNode(strMap(mult))
        if (parent != null)
          parent.add(aNode)
        for (c <- node.child) traverse(c, aNode)
      case _ =>
    }
    aNode
  }

  def getParsers(parserRoot: NodeSeq) {
    ParserBank.clear()
    val parsers = parserRoot \ "Parser"
    var lastParser: String = null
    for (parser <- parsers) {
      val name = (parser \ "@Name").toString
      lastParser = name
      //printf("Parser: %s%n", name)
      val root = new RootNode(name)
      ParserBank(name) = root
      for (h <- parser.child) {
        traverse(h, root)
      }
    }
    VisualLangLab.updateParserChooser(lastParser)
    ParserTreePanel.setParser(ParserBank(lastParser))
  }

  def load(s: String) {
    val grammar = XML.loadString(s)
    load(grammar)
/*    Parsers.customWhitespace = (grammar \ "Whitespace").text.r
    Parsers.comments = (grammar \ "Comments").text.r
    getTokens(grammar \ "Tokens")
    getParsers(grammar \ "Parsers")*/
}

  def load(inFile: File) {
    val grammar = XML.loadFile(inFile)
    load(grammar)
/*    Parsers.customWhitespace = (grammar \ "Whitespace").text.r
    Parsers.comments = (grammar \ "Comments").text.r
    getTokens(grammar \ "Tokens")
    getParsers(grammar \ "Parsers")*/
  }

  def load(grammar: Elem) {
    Parsers.customWhitespace = (grammar \ "Whitespace").text.r
    Parsers.comments = (grammar \ "Comments").text.r
    getTokens(grammar \ "Tokens")
    getParsers(grammar \ "Parsers")
  }

  val strMap = Map("0" -> Multiplicity.Zero, "1" -> Multiplicity.One,
      "?" -> Multiplicity.ZeroOrOne, "*" -> Multiplicity.ZeroOrMore,
      "+" -> Multiplicity.OneOrMore)
  var out: PrintStream = _
}
