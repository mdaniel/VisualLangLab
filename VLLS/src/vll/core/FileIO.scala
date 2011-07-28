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

package vll.core

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.PrintStream
import scala.xml.Elem
import scala.xml.Node
import scala.xml.NodeSeq
import scala.collection.mutable.Set
import scala.xml.Text

class FileIO(hub: VllParsers) {

  private def encode(s: String): String = {
    val sb = new StringBuilder()
    s foreach (_ match {
        case '\n' => sb.append("&#xA;")
        case '\t' => sb.append("&#x9;")
        case '\r' => sb.append("&#xD;")
        case '&' => sb.append("&amp;")
        case '<' => sb.append("&lt;")
        case '>' => sb.append("&gt;")
        case '\"' => sb.append("&quot;")
        case '\'' => sb.append("&apos;")
        case x => sb.append(x)
      }
    )
    sb.toString
  }

  private def decode(s: String): String = {
    val decoder = Map("&amp;" -> '&', "&gt;" -> '>', "&lt;" -> '<', "&quot;" -> '\"', "&apos;" -> '\'',
    "&#xA;" -> '\n', "&#x9;" -> '\t', "&#xD;" -> '\r')
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

  private def printRuleTree(out: PrintStream, t: RuleTreeNode, level: Int) {
    def details = {
      val sb = new StringBuilder
      if (t.multiplicity != Multiplicity.One)
        sb.append("Mult=\"%s\" ".format(t.multiplicity))
      if (!t.errorMessage.isEmpty)
        sb.append("ErrMsg=\"%s\" ".format(encode(t.errorMessage)))
      if (t.drop)
        sb.append("Drop=\"true\" ")
      if (t.isInstanceOf[SequenceNode] && t.asInstanceOf[SequenceNode].commitPoint != -1)
        sb.append("Commit=\"%d\" ".format(t.asInstanceOf[SequenceNode].commitPoint))
      if (!t.actionText.isEmpty)
        sb.append("ActionText=\"%s\" ".format(encode(t.actionText)))
      sb.toString.trim
    }
    val margin = "  " * (2 + level)
    t match {
      case lit: LiteralNode => out.printf("%s<Token Ref=\"%s\" %s/>%n", margin, encode(lit.literalName), details)
      case reg: RegexNode => out.printf("%s<Token Ref=\"%s\" %s/>%n", margin, encode(reg.regexName), details)
      case ref: ReferenceNode => out.printf("%s<Reference Ref=\"%s\" %s/>%n", margin, encode(ref.ruleName), details)
      case rn: RootNode/*(/* _,  */name, isPackrat)*/ =>
        out.printf("    <Parser Name=\"%s\"%s>%n", encode(rn.name), if (rn.isPackrat) " Packrat=\"true\"" else "")
        //out.printf("%s<Root>%n", margin)
        for (i <- 0 until t.size)
          printRuleTree(out, t(i).asInstanceOf[RuleTreeNode], level + 1)
        out.printf("    </Parser>%n")
        //out.printf("%s</Root>%n", margin)
      case seq: SequenceNode =>
        out.printf("%s<Sequence %s>%n", margin, details)
        for (i <- 0 until seq.size)
          printRuleTree(out, seq(i).asInstanceOf[RuleTreeNode], level + 1)
        out.printf("%s</Sequence>%n", margin)
      case rsn: RepSepNode =>
        out.printf("%s<RepSep %s>%n", margin, details)
        for (i <- 0 until rsn.size)
          printRuleTree(out, rsn(i).asInstanceOf[RuleTreeNode], level + 1)
        out.printf("%s</RepSep>%n", margin)
      case ch: ChoiceNode =>
        out.printf("%s<Choice %s>%n", margin, details)
        for (i <- 0 until ch.size)
          printRuleTree(out, ch(i).asInstanceOf[RuleTreeNode], level + 1)
        out.printf("%s</Choice>%n", margin)
      case pn: PredicateNode => out.printf("%s<Predicate %s/>%n", margin, details)
      case _ => System.err.printf("ERROR: Unknown type in parser-tree: %s%n", t)
    }
  }
  
  def exportTokens(out: PrintStream) {
    out.println("<VLL-Grammar>")

/*     out.print("  <Whitespace>")
    out.print(encode(Utils.reEscape(hub.wspace)))
    out.println("</Whitespace>")

    out.print("  <Comments>")
    out.print(encode(hub.comment))
    out.println("</Comments>")
    
 *///    if (hub.flattenNestedTilde)
//      out.println("  <FlattenNestedTildes>true</FlattenNestedTildes>")

    out.println("  <Tokens>")
    for ((name, either) <- hub.tokenBank) either match {
      case left: Left[String, String] =>
        out.printf("    <Literal Name=\"%s\" Pattern=\"%s\"/>%n", encode(name), encode(left.a))
      case right: Right[String, String] =>
        out.printf("    <Regex Name=\"%s\" Pattern=\"%s\"/>%n", encode(name), encode(right.b))
    }
    out.println("  </Tokens>")

    out.println("</VLL-Grammar>")
  }

  def exportTokens(file: File) {
    val out = new PrintStream(file)
    exportTokens(out)
    out.close()
  }
  
  def save(out: PrintStream) {
    out.println("<VLL-Grammar>")

    out.print("  <Whitespace>")
    out.print(encode(Utils.reEscape(hub.wspace)))
    out.println("</Whitespace>")

    out.print("  <Comments>")
    out.print(encode(hub.comment))
    out.println("</Comments>")
    
//    if (hub.flattenNestedTilde)
//      out.println("  <FlattenNestedTildes>true</FlattenNestedTildes>")

    out.println("  <Tokens>")
    for ((name, either) <- hub.tokenBank) either match {
      case left: Left[String, String] =>
        out.printf("    <Literal Name=\"%s\" Pattern=\"%s\"/>%n", encode(name), encode(left.a))
      case right: Right[String, String] =>
        out.printf("    <Regex Name=\"%s\" Pattern=\"%s\"/>%n", encode(name), encode(right.b))
    }
    out.println("  </Tokens>")

    out.println("  <Parsers>")
    for ((name, node) <- hub.ruleBank) {
      //out.printf("    <Parser Name=\"%s\">%n", encode(name))
      printRuleTree(out, node, 1)
      //out.printf("    </Parser>%n")
    }
    out.println("  </Parsers>")

    out.println("</VLL-Grammar>")
  }

  def save(file: File) {
    val out = new PrintStream(file)
    save(out)
    out.close()
  }
  
  override def toString() = {
    val baos = new ByteArrayOutputStream()
    val ps = new PrintStream(baos)
    save(ps)
    ps.close()
    val str = baos.toString
    baos.close()
    str
  }

  private def getTokens(tokens: NodeSeq) {
    val literals = tokens \ "Literal"
    //printf("literals-size: %d%n", literals.size)
    for (parser <- literals) {
      val name = decode((parser \ "@Name").toString)
      val pattern = decode((parser \ "@Pattern").toString)
      //printf("Name=%s Pattern=%s%n", name, pattern)
      hub.tokenBank(name) = Left(pattern)
    }
    val regex = tokens \ "Regex"
    //printf("regexs-size: %d%n", regex.size)
    for (parser <- regex) {
      val name = decode((parser \ "@Name").toString)
      val pattern = decode((parser \ "@Pattern").toString)
      //printf("Name=%s Pattern=%s%n", name, pattern)
      hub.tokenBank(name) = Right(pattern)
    }
  }

  private def traverse(node: Node, parent: RuleTreeNode): RuleTreeNode = {
    var aNode: RuleTreeNode = null
        val multNode = (node \ "@Mult")
        val mult = if (multNode.isEmpty) "1" else (node \ "@Mult").toString
        val errMsgNode = (node \ "@ErrMsg")
        val errMsg = if (errMsgNode.isEmpty) "" else decode(errMsgNode.toString)
        val dropNode = (node \ "@Drop")
        val drop = !dropNode.isEmpty
        val actionTextNode = (node \ "@ActionText")
        val actionText = if (actionTextNode.isEmpty) "" else decode(actionTextNode.toString)
    node match {
      case <Reference></Reference> =>
        val ref = decode((node \ "@Ref").toString)
        //val errMsg = (node \ "@ErrMsg").toString
        //printf("  Reference: %s, %s%n", ref, mult)
        aNode = /* new  */ReferenceNode(strMap(mult), ref)
        aNode.errorMessage = errMsg
        aNode.actionText = actionText
        aNode.drop = drop
        aNode.parent = parent
        parent.append(aNode)
        refdParsers + ref
      case <Token></Token> => 
        val ref = decode((node \ "@Ref").toString)
        //val mult = (node \ "@Mult").toString
        //val errMsg = (node \ "@ErrMsg").toString
        //printf("  Token: %s, %s%n", ref, mult)
        hub.tokenBank.get(ref) match {
          case Some(Left(_)) => aNode = /* new  */LiteralNode(strMap(mult), ref)
          case Some(Right(_)) => aNode = /* new  */RegexNode(strMap(mult), ref)
          case None => printf("FATAL: Token '%s' not found. VLL file is corrupted%n", ref)
            sys.exit()
        }
        aNode.errorMessage = errMsg
        aNode.actionText = actionText
        aNode.drop = drop
        //aNode = new TokenTreeNode(ref, strMap(mult))
        aNode.parent = parent
        parent.append(aNode)
        refdTokens + ref
      case <Sequence>{contents @ _*}</Sequence> => 
        //val errMsg = (node \ "@ErrMsg").toString
        //printf("  Sequence: %s%n", mult)
        aNode = /* new  */SequenceNode(strMap(mult))
        aNode.errorMessage = errMsg
        aNode.actionText = actionText
        aNode.drop = drop
        aNode.parent = parent
        val commit = (node \ "@Commit").toString
        if (!commit.isEmpty)
          aNode.asInstanceOf[SequenceNode].commitPoint = commit.toInt
        if (parent != null)
          parent.append(aNode)
        for (c <- node.child) traverse(c, aNode)
      case <RepSep>{contents @ _*}</RepSep> =>
        //val mult = (node \ "@Mult").toString
        //val errMsg = (node \ "@ErrMsg").toString
        //printf("  Sequence: %s%n", mult)
        aNode = /* new  */RepSepNode(strMap(mult))
        aNode.errorMessage = errMsg
        aNode.actionText = actionText
        aNode.drop = drop
        aNode.parent = parent
        if (parent != null)
          parent.append(aNode)
        for (c <- node.child) traverse(c, aNode)
      case <Choice>{contents @ _*}</Choice> => 
        //val mult = (node \ "@Mult").toString
        //val errMsg = (node \ "@ErrMsg").toString
        //printf("  Choice: %s%n", mult)
        //val distinguishNode = (node \ "@Distinguish")
        //val distinguish = !distinguishNode.isEmpty
        aNode = /* new  */ChoiceNode(strMap(mult))
        aNode.errorMessage = errMsg
        aNode.actionText = actionText
        aNode.drop = drop
//        aNode.asInstanceOf[ChoiceNode].distinguish = distinguish
        aNode.parent = parent
        if (parent != null)
          parent.append(aNode)
        for (c <- node.child) traverse(c, aNode)
      case <Predicate>{contents @ _*}</Predicate> => 
        //val mult = (node \ "@Mult").toString
        //val errMsg = (node \ "@ErrMsg").toString
        //printf("  Choice: %s%n", mult)
        //val distinguishNode = (node \ "@Distinguish")
        //val distinguish = !distinguishNode.isEmpty
        aNode = /* new  */PredicateNode()
        aNode.errorMessage = errMsg
        aNode.actionText = actionText
        aNode.drop = drop
//        aNode.asInstanceOf[ChoiceNode].distinguish = distinguish
        aNode.parent = parent
        if (parent != null)
          parent.append(aNode)
        for (c <- node.child) traverse(c, aNode)
      case Text(s) => 
    }
    aNode
  }

  private def getParsers(parserRoot: NodeSeq) {
    hub.ruleBank.clear()
    val parsers = parserRoot \ "Parser"
    var lastParser: String = null
    for (parser <- parsers) {
      val name = (parser \ "@Name").toString
      val packratNode = (parser \ "@Packrat")
      val isPackrat = !packratNode.isEmpty
      lastParser = name
      //printf("Parser: %s%n", name)
      val root = /* new  */RootNode(name, isPackrat)
      hub.ruleBank(name) = root
      for (h <- parser.child) {
        traverse(h, root)
      }
    }
  }

  def importTokens(grammar: Elem) {
    getTokens(grammar \ "Tokens")
  }

  def load(grammar: Elem) {
    hub.wspace = (grammar \ "Whitespace").text
    hub.comment = (grammar \ "Comments").text
    hub.tokenBank.clear
    getTokens(grammar \ "Tokens")
    getParsers(grammar \ "Parsers")
    if (hub.ruleBank.size > 1 && hub.ruleBank("Main").isEmpty)
      hub.ruleBank.remove("Main")
    missingTokens.clear()
    missingParsers.clear()
    refdTokens.filter(!hub.tokenBank.contains(_)).foreach(missingTokens + _)
    refdParsers.filter(!hub.ruleBank.contains(_)).foreach(missingParsers + _)
    refdTokens.clear()
    refdParsers.clear()
    if (!missingTokens.isEmpty || !missingParsers.isEmpty)
      throw new IOException("Reference integrity errors exist")
  }

  private val strMap = Map("0" -> Multiplicity.Not, "1" -> Multiplicity.One,
      "?" -> Multiplicity.ZeroOrOne, "*" -> Multiplicity.ZeroOrMore,
      "+" -> Multiplicity.OneOrMore, "=" -> Multiplicity.Guard)

  private val refdTokens = Set[String]()
  private val refdParsers = Set[String]()
  var missingTokens = Set[String]()
  var missingParsers = Set[String]()
}
