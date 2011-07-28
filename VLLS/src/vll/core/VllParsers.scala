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
import scala.util.parsing.combinator.PackratParsers
import scala.util.parsing.combinator.Parsers
import scala.util.parsing.input.CharSequenceReader
import scala.xml.XML
import scala.xml.{Elem => XMLElem}
import sun.org.mozilla.javascript.internal.NativeArray

class VllParsers extends Parsers with PackratParsers {

  override type Elem = Char
  
  def load(f: File) {fileIo.load(XML.loadFile(f))}
  def load(s: String) {fileIo.load(XML.loadString(s))}
  def load(x: XMLElem) = {fileIo.load(x)}
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
  
  def reset() {
    tokenBank.clear()
    ruleBank.clear()
    commentRe = ""
    whiteSpaceRe = "\\s+"
    updateSkipExpression()
  }
  
  def getParserFor(parserName: String): Parser[_] = {
    globalTokenParserTime = 0
    globalTokenParserTime2 = 0
    val (lits, regs) = tokenBank.toArray.filterNot(_._1.endsWith("_")).partition(_._2.isInstanceOf[Left[_,_]])
    val sortedLits = lits.sortWith((a, b) => a._1 < b._1)
    val sortedRegs = regs.sortWith((a, b) => a._1 < b._1)
    val sortedTokens = Array(sortedLits, sortedRegs).flatMap(x => x)
    globalTokenIds.clear()
    tokenParserCache.clear()
    for (i <- 0 until sortedTokens.length) {
//printf("%s:%s=%d%n", sortedTokens(i)._1, sortedTokens(i)._2, i)
      globalTokenIds(sortedTokens(i)._1) = i
    }
    globalTokens = sortedTokens.map(kv => Pair(kv._1, kv._2 match {case Left(s) => Automata.escapeMetaChars(Utils.unEscape(s)); case Right(s) => Utils.unEscape(s)}))
//    globalTokenLexer = Automata.lexer(globalTokens.map(_._2))
    globalTokenLexer = Automata.lexer(globalTokens.map(_._2))
    var theParser: Parser[_] = null
    try {
      parserCache.clear()
      ok = true
      theParser = getParser(parserName)
    } catch {
      case e => ok = false; e.printStackTrace()
    }
/*     if (ok) {
      Parser(in => {stringToParse = in.source.toString; theParser(in)})
    } else
      null
 */
    if (ok) theParser else null
  }

  override def phrase[T](p: Parser[T]) = super.phrase[T](p <~ eofParser)
  
  // The PACKAGE PRIVATE part (internal API) follows ...
  
  private def eofParser = {
    val parser = Parser(in => {
        if (in.atEnd)
          Success("", in)
//         val spaces = math.max(whitespaceMatcher(stringToParse, in.offset), 0)
        val spaces = math.max(whitespaceMatcher(in), 0)
        if (in.drop(spaces).atEnd)
          Success("", in.drop(spaces))
        else
          Failure("Expected EOF", in)
      })
    parser
  }

  private lazy val globalTokenParser: PackratParser[Pair[String, Int]] = Parser(in => {
        val spaces = whitespaceMatcher(in)
        val in2 = if (spaces > 0) in.drop(spaces) else in
        val n = globalTokenLexer(in2)
        if (n(0) > 0) {
          Success(Pair(in2.source.subSequence(in2.offset, in2.offset + n(0)).asInstanceOf[String], n(1)), in2.drop(n(0)))
        } else {
          Failure(if (in2.atEnd) "End-of-input" else "No-known-token", in2)
        }
      }
    )
    
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
        case Error(_, nxt) => Error("%s %s at (%d,%d)".format(
                                           node.nodeName, node.errorMessage, in.pos.line, in.pos.column), nxt)
        case Failure(_, nxt) => Failure("%s %s at (%d,%d)".format(
                                               node.nodeName, node.errorMessage, in.pos.line, in.pos.column), nxt)
        case success => success
      }
    )
  }

  private def withMultiplicity(parser: Parser[_], node: RuleTreeNode): Parser[_] = node.multiplicity match {
    case Multiplicity.One => parser
//    case Multiplicity.ZeroOrOne => ((parser)?) ^^ {r => if (r.isEmpty) Array() else Array(r.get)}
    case Multiplicity.ZeroOrOne => (parser)? 
//    case Multiplicity.ZeroOrMore => ((parser)*) ^^ {_.toArray}
    case Multiplicity.ZeroOrMore => (parser)*
//    case Multiplicity.OneOrMore => ((parser)+) ^^ {_.toArray}
    case Multiplicity.OneOrMore => (parser)+
    case Multiplicity.Not => Parser {in => parser(in) match {
          case Success(s, _)  => Failure("%s does NOT expect '%s' at (%s,%s)".format(
                                                node.nodeName, s, in.pos.line, in.pos.column), in)
          case _ => Success((), in)
        }}
    case Multiplicity.Guard => Parser {in => parser(in) match {
          case Success(s, _) => Success(s, in)
          case Failure(_, _) => Failure("%s missing expected text at (%s,%s)".format(
                                               node.nodeName, in.pos.line, in.pos.column), in)
          case Error(_, _) => Error("%s missing expected text at (%s,%s)".format(
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

  private def tilde2array(t: Any): Any = {
    def add(t: ~[_, _], buf: mutable.ArrayBuffer[Any]) {
      val a ~ b = t
      a match {
        case tt: ~[_, _] => add(tt, buf)
        case _ => buf.append(a)
      }
      buf.append(b)
    }
    t match {
      case x: ~[_, _] => val aBuf = new mutable.ArrayBuffer[Any]()
        add(x, aBuf)
        aBuf.toArray
      case _ => t
    }
  }

  private def tokenParser(tokenName: String, rex: String, isRegex: Boolean, isLocal: Boolean, node: RuleTreeNode): Parser[String] = {
    def failureMsg1(nodeName: String, expected: String) = "%s needs %s".format(nodeName, expected)
    def failureMsg2(nodeName: String, expected: String, found: String) = "%s needs %s (got %s)".format(nodeName, expected, found)
    if (isLocal) {
        val unescapedString = Utils.unEscape(rex)
        val regExp = if (isRegex) unescapedString else Automata.escapeMetaChars(unescapedString)
        val localTokenMatcher = if (localTokenMatcherCache.contains(regExp)) 
          localTokenMatcherCache(regExp) 
        else {
          val newMatcher = Automata.matcher(regExp)
          localTokenMatcherCache(regExp) = newMatcher
          newMatcher
        }
      Parser(in => {
        if (userRequestedStop)
          throw new InterruptedException("Interrupted by user")
        val spaces = whitespaceMatcher(in)
        val in2 = if (spaces > 0) in.drop(spaces) else in
        val n = localTokenMatcher(in2)
        if (n > 0)
          Success(in2.source.subSequence(0, n).asInstanceOf[String], in2.drop(n))
        else
          Failure(failureMsg1(node.nodeName, tokenName), in)
      })
    } else {
        val tokenId = globalTokenIds(tokenName)
      Parser(in => {
        if (userRequestedStop)
          throw new InterruptedException("Interrupted by user")
        val startTime = System.currentTimeMillis
        val gtp = globalTokenParser(in)
        val startTime2 = System.currentTimeMillis
        globalTokenParserTime += startTime2 - startTime
        val result: ParseResult[String] = gtp match {
          case Success(stringAndId, next) =>
            if (stringAndId._2 == tokenId) Success(stringAndId._1, next) else 
              Failure(failureMsg2(node.nodeName, tokenName, globalTokens(stringAndId._2)._1), in)
          case Failure(msg, _) => Failure(msg, in)
          case Error(msg, _) => Error(msg, in)
        }
        globalTokenParserTime2 += System.currentTimeMillis - startTime2
        result
      })
    }
  }

  private def combineChoice(node: ChoiceNode): Parser[_] = {
//    node.zipWithIndex.map(tup => node2parser(tup._1) ^^ (res => Array(tup._2, res))).reduceLeft(_ | _)
    node.zipWithIndex.map(tup => node2parser(tup._1) ^^ (res => Pair(tup._2, res))).reduceLeft(_ | _)
  }

  private def combineSequence(node: SequenceNode): Parser[_] = {
    def toDrop(n: RuleTreeNode) = n.drop || n.multiplicity == Multiplicity.Not || n.multiplicity == Multiplicity.Guard || n.isInstanceOf[PredicateNode]
    val t3 = node.zipWithIndex.map(nz => Tuple3(node2parser(nz._1), toDrop(nz._1), (node.commitPoint != -1 && nz._2 > node.commitPoint)))
    t3.reduceLeft((p1, p2) => (p1._2, p2._2, p2._3) match {
        case (false, false, false) => Tuple3(p1._1 ~ p2._1, (p1._2 && p2._2), false)
        case (false, false, true) => Tuple3(p1._1 ~ commit(p2._1), (p1._2 && p2._2), false)
        case (false, true, false) => Tuple3((p1._1 <~ p2._1), (p1._2 && p2._2), false)
        case (false, true, true) => Tuple3(p1._1 <~ commit(p2._1), (p1._2 && p2._2), false)
        case (true, false, false) => Tuple3((p1._1 ~> p2._1), (p1._2 && p2._2), false)
        case (true, false, true) => Tuple3(p1._1 ~> commit(p2._1), (p1._2 && p2._2), false)
        case (true, true, false) => Tuple3((p1._1 ~> p2._1), (p1._2 && p2._2), false)
        case (true, true, true) => Tuple3(p1._1 ~> commit(p2._1), (p1._2 && p2._2), false)
      })._1
  }

  private def objToJsArray(tree: Any): Object = {
    def nativeArray(na: Array[Object]) = new NativeArray(na) {
      override def toString = getDefaultValue(null)
      override def getDefaultValue(claz: Class[_]) = {
        val b = mutable.Buffer[Object]()
        for (i <- 0 until getLength.asInstanceOf[Int]) {
          b.append(get(i, this) match {
              case narr: NativeArray => "[" + narr.getDefaultValue(null) + "]"
              case x => x
            })
        }
        b.mkString(",")
      }
    }
    val rv = tree match {
      case null => null
      case a: Array[_] => nativeArray(a.map(objToJsArray).toArray[Object])
      case p: Pair[_, _] => objToJsArray(Array[Any](p._1, p._2))
      case opt: Option[_] => objToJsArray(if (opt.isEmpty) Array[Any]() else Array[Any](opt.get))
      case lst: List[_] => objToJsArray(lst.map(objToJsArray).toArray)
      case s: String => s
      case i: Int => java.lang.Double.valueOf(i) // produced only by "combine-choice" parser 
      case r: AnyRef => r
    }
    rv
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
        case sn: SequenceNode => combineSequence(sn) ^^ tilde2array
        case cn: ChoiceNode => combineChoice(cn)
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
            val actionText: String = node.actionText.trim
            val isJS = Utils.isJavascriptCode(actionText)
            var compiledScript = if (isJS)
              JsEngine.compile(actionText)
            else
              ScalaEngine.compile(actionText)
            Parser(in => {
                compiledScript(in.pos.line, in.pos.column, null) match {
                  case b: java.lang.Boolean if b.booleanValue => Success("", in)
                  case errMsg: String => Failure(errMsg, in)
                  case _ => Failure("Predicate error", in)
                }
            })
          }
      }
    }
    val parser2 = if (node.errorMessage.isEmpty) parser1 else withMessage(parser1, node)
    val parser3 = if (node.trace || (traceAll && node.parent == null)) withTrace(parser2, node) else parser2
    val parser4 = node match {
      case RepSepNode(_) | RootNode(/* _,  */_, _) =>
        parser3
      case _ =>
        if (node.multiplicity != Multiplicity.One) {
          withMultiplicity(parser3, node)
        } else
          parser3
    }
    val parser5: Parser[_] = if (node.isInstanceOf[PredicateNode] || node.actionText.isEmpty) parser4 else {
      val actionText: String = node.actionText.trim
      val isJS = Utils.isJavascriptCode(actionText)
      val compiledScript = if (isJS)
        JsEngine.compile(actionText)
      else
        ScalaEngine.compile(actionText)
      Parser(in => {
          compiledScript(in.pos.line, in.pos.column, null)
          parser4(in) match {
            case Success(tree, next) =>
              var actionResult = compiledScript(next.pos.line, next.pos.column, if (isJS)objToJsArray(tree) else tree)
              Success(if (actionResult == null) tree else actionResult, next)
            case other => other
          }
      })
    }
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

  var fileToParse: File = null;
  private var currentLine, currentColumn = 0
  private val tokenParserCache = mutable.HashMap[String,Parser[String]]()
  private var globalTokens: Array[Pair[String,String]] = null
//  private var globalTokenLexer: Automata.LexerType = null
  private var globalTokenLexer: Automata.LexerType = null
//  private var stringToParse = ""
  var globalTokenParserTime, globalTokenParserTime2: Long = 0
  val globalTokenIds = mutable.HashMap[String, Int]()
//  private var allLiterals = mutable.HashSet[String]()
  private var traceDepth = 0
  private var ok = true
  private val parserCache = mutable.Map[String, Parser[_]]()
//  private val localTokenMatcherCache = mutable.Map[String, Automata.MatcherType]()
  private val localTokenMatcherCache = mutable.Map[String, Automata.MatcherType]()
}

object VllParsers {
  type Parser[T] = VllParsers#Parser[T]
  def fromFile(f: File): VllParsers = {
    val rv = new VllParsers
    rv.load(f)
    rv
  }
  def fromString(s: String): VllParsers = {
    val rv = new VllParsers
    rv.load(s)
    rv
  }
  def fromXml(xml: XMLElem): VllParsers = {
    val rv = new VllParsers
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
