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

import java.io.File
import java.io.PrintStream
import scala.collection._
import scala.io.Source
import scala.swing.TextComponent
import scala.util.parsing.combinator.PackratParsers
import scala.util.parsing.input.CharSequenceReader
import scala.xml.XML
import scala.xml.{Elem => XMLElem}
import vll.gui.VllGui
import vll.gui.VllGui._

class VllParsers(val gui: VllGui) extends SimpleLexingRegexParsers with PackratParsers with Aggregates {

  override type Elem = Char
  
  def load(f: File) {load(XML.loadFile(f))}
  private def load(s: String) {load(XML.loadString(s))}
  def load(x: XMLElem) = {
    try {if (gui ne null) gui.setWaitingCursor(true); fileIo.load(x)} 
    finally {if (gui ne null) gui.setWaitingCursor(false)}
  }
  def exportTokens(f: File) = {fileIo.exportTokens(f)}
  def importTokens(f: File) = {fileIo.importTokens(XML.loadFile(f))}
  def importTokens(s: String) = {fileIo.importTokens(XML.loadString(s))}
  def importTokens(x: XMLElem) = {fileIo.importTokens(x)}
  
  def save(f: File) {fileIo.save(f)}
  def save(ps: PrintStream) {fileIo.save(ps)}
  
  override def toString() = fileIo.toString

  def comment = commentRe
  def comment_=(c: String) {
    commentRe = c
    updateSkipExpression()
  }

  def wspace = whiteSpaceRe
  def wspace_=(w: String) {
    whiteSpaceRe = w
    updateSkipExpression()
  }

  var traceAll = false
  var userRequestedStop = false
  
  override def reset() {
    super.reset()
    tokenBank.clear()
    ruleBank.clear()
    commentRe = ""
    whiteSpaceRe = "\\\\s+"
    updateSkipExpression()
  }
  
  def getParserFor(parserName: String): Parser[_] = {
    def tsc(a: String, b: String): Boolean = {
      def prio(i: String) = if (i contains '~') i.reverse.takeWhile(_.isDigit).reverse.toInt else 0
      prio(a) > prio(b)
    }
    super.reset()
    globalTokenParserTime = 0
    val (lits, regs) = tokenBank.toArray.filterNot(_._1.endsWith("_")).partition(_._2.isInstanceOf[Left[_,_]])
    lits.foreach(p => literal(Utils.unEscape(p._2.left.get)))
    regs.sortWith((a, b) => tsc(a._1, b._1)).foreach(p => regex(Utils.unEscape(p._2.right.get).r))
    tokenParserCache.clear()
    var theParser: Parser[_] = null
    try {
      parserCache.clear()
      ok = true
      theParser = getParser(parserName)
    } catch {
      case e => ok = false; e.printStackTrace()
    }
    if (ok) theParser else null
  }

  // The PACKAGE PRIVATE part (internal API) follows ...
  
  override def handleWhiteSpace(source: java.lang.CharSequence, offset: Int): Int =
    offset + whitespaceMatcher(source.subSequence(offset, source.length))

  val ruleBank = new RuleBank(/* this */)
  val tokenBank = new mutable.HashMap[String, Either[String, String]] {
    def getTokenNames = keys.toArray.sorted
  }
  val fileIo = new FileIO(this)

  // The PRIVATE part (implementation) follows ...
  
  private var commentRe = ""
  private var whiteSpaceRe = "\\\\s+"
  private var skipRe = whiteSpaceRe
  private var whitespaceMatcher = Automata.matcher(Utils.unEscape(skipRe))

  private def updateSkipExpression() {
    if (commentRe.isEmpty) {
      if (whiteSpaceRe.isEmpty) {
        skipRe = ""
      } else {
        skipRe = whiteSpaceRe
      }
    } else {
      if (whiteSpaceRe.isEmpty) {
        skipRe = commentRe
      } else {
        skipRe = "(?:(?:" + whiteSpaceRe + ")|(?:" + commentRe + "))+"
      }
    }
    whitespaceMatcher = Automata.matcher(Utils.unEscape(skipRe))
  }
  
  private def withMessage[T](p: Parser[T], node: RuleTreeNode): Parser[T] = {
    Parser(in => p(in) match {
        case Error(_, nxt) => Error(node.errorMessage, nxt)
        case Failure(_, nxt) => Failure(node.errorMessage, nxt)
        case success => success
      }
    )
  }

  private def withMultiplicity(parser: Parser[_], node: RuleTreeNode): Parser[_] = node.multiplicity match {
    case Multiplicity.One => parser
    case Multiplicity.ZeroOrOne => (parser)? 
    case Multiplicity.ZeroOrMore => (parser)*
    case Multiplicity.OneOrMore => (parser)+
    case Multiplicity.Not => Parser {in => parser(in) match {
          case Success(s, _)  => Failure("%s does NOT expect '%s' at (%s,%s)".format(
                                                node.nodeName, s, in.pos.line, in.pos.column), in)
          case _ => Success((), in)
        }}
    case Multiplicity.Guard => Parser {in => parser(in) match {
          case Success(s, _) => Success(s, in)
          case Failure(_, _) => Failure("%s missing GUARD text at (%s,%s)".format(
                                               node.nodeName, in.pos.line, in.pos.column), in)
          case Error(_, _) => Error("%s missing GUARD text at (%s,%s)".format(
                                           node.nodeName, in.pos.line, in.pos.column), in)
        }}

  }

  private def withTrace[T](p: Parser[T], node: RuleTreeNode): Parser[T] = {
    Parser(in => {
        val margin = augmentString(". ") * traceDepth
        printf("%sENTRY %s (%s,%s)%n", margin, node.nodeName, in.pos.line, in.pos.column)
        traceDepth += 1
        val res: ParseResult[T] = p(in);
        traceDepth -= 1
        printf("%sEXIT %s (%s,%s) %s%n", margin, node.nodeName, res.next.pos.line, res.next.pos.column, res.getClass.getSimpleName)
        res
      }
    )
  }

   private def tokenParser(tokenName: String, rex: String, isRegex: Boolean, isLocal: Boolean, node: RuleTreeNode): Parser[String] = {
    def failureMsg1(nodeName: String, expected: String) = "%s needs %s".format(nodeName, expected)
    def failureMsg2(nodeName: String, expected: String, found: String) = "%s needs %s (got %s)".format(nodeName, expected, found)
    val unescapedString = Utils.unEscape(rex)
    if (isLocal) {
      val p = if (isRegex) regex$(unescapedString.r) else literal$(unescapedString)
      Parser(in => {
          val t = System.currentTimeMillis
          if (userRequestedStop)
            throw new InterruptedException("Interrupted by user")
          val rv = p(in) match {
            case s: Success[String] => s
            case Failure(msg, _) => Failure(msg, in)
          }
          globalTokenParserTime += System.currentTimeMillis - t
          rv
        })
    } else {
      val p = if (isRegex) regex(unescapedString.r) else literal(unescapedString)
      Parser(in => {
          val t = System.currentTimeMillis
          if (userRequestedStop)
            throw new InterruptedException("Interrupted by user")
          val rv = p(in) match {
            case s: Success[String] => s
            case Failure(msg, _) => Failure(msg, in)
          }
          globalTokenParserTime += System.currentTimeMillis - t
          rv
        })
    }
  }

  private def node2parser(node: RuleTreeNode): Parser[_] = {
    val parser1: Parser[_] =
    if (!node.isValid) {
      System.err.printf("Invalid node: %s: %s%n", node.nodeName, node.isValidMessage)
      ok = false
      err(node.isValidMessage)
    } else {
      node match {
        case RootNode(_, packrat) =>
          val theParser = node2parser(node.head)
          if (packrat) parser2packrat(theParser) else theParser
        case WildCardNode(_) => wildCardParser
        case RegexNode(_, tokenName) =>
          if (tokenParserCache.contains(tokenName))
            tokenParserCache(tokenName)
          else {
            val p = tokenParser(tokenName, tokenBank(tokenName).right.get, true, tokenName.endsWith("_"), node)
            tokenParserCache(tokenName) = p
            p
          }
        case LiteralNode(_, tokenName) =>
          if (tokenParserCache.contains(tokenName))
            tokenParserCache(tokenName)
          else {
            val p = tokenParser(tokenName, tokenBank(tokenName).left.get, false, tokenName.endsWith("_"), node)
            tokenParserCache(tokenName) = p
            p
          }
        case sn: SequenceNode => sequence(sn.zipWithIndex.map(
                  n => {Triple(node2parser(n._1), n._1.drop || (n._1.multiplicity == Multiplicity.Not) ||
                  (n._1.multiplicity == Multiplicity.Guard), (sn.commitPoint != -1 && n._2 >= sn.commitPoint))}
              ):_*)
        case cn: ChoiceNode => choice(cn.map(node2parser):_*)
        case ReferenceNode(_, tokenName) => Parser(x => getParser(tokenName)(x))
        case RepSepNode(_) =>
          val rep = node2parser(node(0))
          val sep = node2parser(node(1))
          node.multiplicity match {
            case Multiplicity.ZeroOrMore => repsep(rep, sep)
            case Multiplicity.OneOrMore => rep1sep(rep, sep)
          }
        case pn: PredicateNode => if (node.actionText.isEmpty) 
            failure("Predicate has no code")
          else {
            Parser(in => {node.actionFunction(in.source, gui.logTextPane.inputArea, 
                       gui.logTextPane.logAreaTextComponent, 
                       in.pos.line, in.pos.column, in.offset, null) match {
                  case b: java.lang.Boolean if b.booleanValue => Success("", in)
                  case errMsg: String => Failure(errMsg, in)
                  case _ => Failure("Predicate error", in)
                }
            })
          }
      }
    }
    val parser2 = if (node.errorMessage.isEmpty) parser1 else withMessage(parser1, node)
    val parser3 = node match {
      case RepSepNode(_) | RootNode(/* _,  */_, _) =>
        parser2
      case _ =>
        if (node.multiplicity != Multiplicity.One) {
          withMultiplicity(parser2, node)
        } else
          parser2
    }
    val parser4: Parser[_] = if (node.isInstanceOf[PredicateNode] || node.actionText.isEmpty) parser3 else {
      if (node.actionFunction eq null)
        failure("Action code has syntax error")
      else
        Parser(in => {
          node.actionFunction(null, gui.logTextPane.inputArea, 
                              gui.logTextPane.logAreaTextComponent,  
                              in.pos.line, in.pos.column, in.offset, null)
          parser3(in) match {
            case Success(tree, next) =>
              val source = in.source.subSequence(in.offset, next.offset)
              var actionResult = node.actionFunction(source, gui.logTextPane.inputArea, 
                                                     gui.logTextPane.logAreaTextComponent, 
                                                     next.pos.line, next.pos.column, next.offset, tree)
              Success(if (actionResult == null) tree else actionResult, next)
            case other => other
          }
        })
    }
    val parser5 = if (node.trace || (traceAll && node.parent == null)) withTrace(parser4, node) else parser4
    parser5.named(node.nodeName)
  }

  private def getParser(parserName: String): Parser[_] = {
    if (parserCache.contains(parserName))
      parserCache(parserName)
    else {
      val p = node2parser(ruleBank(parserName))
      parserCache(parserName) = p
      p
    }
  }

//  var fileToParse: File = null;
  private val tokenParserCache = mutable.HashMap[String,Parser[String]]()
  var globalTokenParserTime: Long = 0
  private var traceDepth = 0
  private var ok = true
  private val parserCache = mutable.Map[String, Parser[_]]()
}

object VllParsers {
  type Parser[T] = VllParsers#Parser[T]
  type ActionType = Function7[CharSequence,TextComponent,TextComponent,Int,Int,Int,Any,Any]
  def fromFile(f: File, gui: VllGui): VllParsers = {
    val rv = new VllParsers(gui)
    rv.load(f)
    rv
  }
  def fromFile(f: File): VllParsers = {
    val rv = new VllParsers(null)
    rv.load(f)
    rv
  }
  def fromString(s: String, gui: VllGui): VllParsers = {
    val rv = new VllParsers(gui)
    rv.load(s)
    rv
  }
  def fromString(s: String): VllParsers = {
    val rv = new VllParsers(null)
    rv.load(s)
    rv
  }
  def fromXml(xml: XMLElem, gui: VllGui = null): VllParsers = {
    val rv = new VllParsers(gui)
    rv.load(xml)
    rv
  }
  def main(args: Array[String]) {
    val err = System.err
    if (args.length < 2 || args.length > 3) {
      err.println("usage: VllParsers <vll-file> <data-file> [parser-name]")
      sys.exit(1)
    }
    val vllFile = new File(args(0))
    if (!vllFile.exists) {
      err.printf("unable to find vll-file (%s)%n", vllFile.getAbsolutePath)
      sys.exit(1)
    }
    val dataFile = new File(args(1))
    if (!dataFile.exists) {
      err.printf("unable to find data-file (%s)%n", dataFile.getAbsolutePath)
      sys.exit(1)
    }
    val vllParsers = fromFile(vllFile)
    var parser: vllParsers.Parser[_] = null;
    if (args.length == 3) {
      if (vllParsers.ruleBank.contains(args(2))) {
          parser = vllParsers.getParserFor(args(2))
      } else {
        err.printf("unable to find parser '%s'%n", args(2))
        sys.exit(1)
      }
    } else if (vllParsers.ruleBank.contains("Main")) {
        parser = vllParsers.getParserFor("Main")
    } else {
      val pName = args(0).substring(0, args(0).length - 4)
      if (vllParsers.ruleBank.contains(pName)) {
        parser = vllParsers.getParserFor(pName)
      } else {
        err.printf("unable to find top-level parser ('Main' or '%s')%n", pName)
        sys.exit(1)
      }
    }
    val src = Source.fromFile(dataFile)
    val csr = new CharSequenceReader(src.getLines.mkString("\n"))
    val res = vllParsers.phrase(parser)(csr)
    if (!res.successful) {
      println(res)
    }
  }
}
