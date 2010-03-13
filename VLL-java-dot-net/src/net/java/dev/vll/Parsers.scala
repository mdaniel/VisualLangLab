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

import scala.collection.mutable.Stack
import scala.util.parsing.combinator.RegexParsers
import scala.collection.mutable.Map
import scala.util.parsing.input.Position

object Parsers extends RegexParsers {
  
  var comments = "".r

  var customWhitespace = """\s+""".r

  override def skipWhitespace = customWhitespace.toString.length > 0

  override def handleWhiteSpace(source: java.lang.CharSequence, offset: Int): Int =
    if (skipWhitespace)
      (customWhitespace findPrefixMatchOf (source.subSequence(offset, source.length))) match {
        case Some(matched) => offset + matched.end
        case None => offset
      }
    else
      offset

  private def setCustomMessage[T](p: Parser[T], newMsg: String): Parser[T] = {
    return Parser(in => p(in) match {
        case Error(_, nxt) => Error(newMsg, nxt)
        case Failure(_, nxt) => Failure(newMsg, nxt)
        case success => success
      }
    )
  }

  private def applyMultiplicity(parser: Parser[_], multiplicity: Multiplicity.Value) = multiplicity match {
    case Multiplicity.One => parser
    case Multiplicity.ZeroOrOne => (parser)?
    case Multiplicity.ZeroOrMore => (parser)*
    case Multiplicity.OneOrMore => (parser)+
    case Multiplicity.Zero => not(parser.asInstanceOf[Parser[Nothing]])
  }

  private def withTrace[T](p: Parser[T]): Parser[T] = {
      Parser(x => {
          traceStack.push(x.pos)
          val res: ParseResult[T] = p(x);
          val startPos = traceStack.pop
          res match {
            case Success(_, next) =>
              printf("TRACE: '%s' matched from (%d,%d) to (%d,%d)%n", p, startPos.line, startPos.column,
                  next.pos.line, next.pos.column - 1)
            case Failure(_, next) =>
              printf("TRACE: '%s' failed at (%d, %d)%n", p, startPos.line, startPos.column)
            case Error(_, next) =>
              printf("TRACE: '%s' error at (%d, %d)%n", p, startPos.line, startPos.column)
          }
          res
        }
      )
  }

  private def getChildArray(node: ParserTreeNode) = {
    val array = new Array[ParserTreeNode](node.getChildCount)
    for (i <- 0 until array.size) array(i) = node.getChild(i)
    array
  }

  private def tilde2array(t: ~[_,_]): Array[Any] = {
    def tilde2list(t: ~[_,_]): List[Any] = {
      val ~(a, b) = t
      a match {
        case at: ~[_,_] => b :: tilde2list(at)
        case _ => List(b, a)
      }
    }
    tilde2list(t).reverse.toArray
  }

  private def node2parser(node: ParserTreeNode): Parser[_] = {
    if (!node.isComplete) {
      printf("Incomplete node: %s%n", node.nodeName)
    }
    var parser: Parser[_] = null
    var multiplicity: Multiplicity.Value = null
    node match {
      case RootNode(mult) =>
        if (node.isComplete) {
          parser = node2parser(node.getChild(0))
          parser.named(node.nodeName)
        } else {
          ok = false;
          parser = err("Incomplete parser")
        }
        multiplicity = mult
      case RegexNode(mult, name) =>
        parser = regex(TokenBank(name).right.get.r)
        parser.named(node.nodeName)
        multiplicity = mult
      case LiteralNode(mult, name) =>
        parser = literal(TokenBank(name).left.get)
        parser.named(node.nodeName)
        multiplicity = mult
      case SequenceNode(mult) =>
        val childParserArray = getChildArray(node) map node2parser
        if (node.isComplete) {
          parser = childParserArray.reduceLeft(_ ~ _)
          parser.named(node.nodeName)
          if (VisualLangLab.flattenMenuItem.selected) {
            parser = (parser.asInstanceOf[Parser[~[_,_]]]) ^^ tilde2array
            parser.named(node.nodeName + ".^^")
          }
        } else {
          ok = false;
          parser = err("Incomplete parser")
        }
        multiplicity = mult
      case ChoiceNode(mult) =>
        val childParserArray = getChildArray(node) map node2parser
        if (node.isComplete) {
          parser = childParserArray.reduceLeft(_ | _)
          parser.named(node.nodeName)
        } else {
          ok = false;
          parser = err("Incomplete parser")
        }
        multiplicity = mult
      case ReferenceNode(mult, name) =>
        parser = Parser(x => getParser(name)(x))
        parser.named(node.nodeName)
        multiplicity = mult
      case RepSepNode(mult) =>
        node.isComplete match {
          case true =>
            val rep = node2parser(node.getChild(0))
            val sep = node2parser(node.getChild(1))
            parser = mult match {
              case Multiplicity.ZeroOrMore =>
                repsep(rep, sep)
              case Multiplicity.OneOrMore =>
                rep1sep(rep, sep)
            }
            parser.named(node.nodeName)
          case false => parser = err("Incomplete parser")
            ok = false
        }
        multiplicity = mult
      case _ => System.err.printf("Unknown node-type: (%s, %s)%n", node.nodeName, node.getClass)
        parser = err("Unknown node type: " + node.toString)
    }
    if (!node.errorMessage.isEmpty)
      parser = setCustomMessage(parser, node.errorMessage)
    if (node.trace) {
      parser = withTrace(parser)
      parser.named(node.nodeName + ".TRACE")
    }
    node match {
      case RepSepNode(_) | RootNode(_) => 
      case _ => 
        if (multiplicity != Multiplicity.One) {
          parser = applyMultiplicity(parser, multiplicity)
          parser.named(node.nodeName + ".Multiplicity-" + multiplicity.toString)
        }
    }
    parser
  }

  private def getParser(parserName: String): Parser[_] = {
    if (parserCache.contains(parserName))
      parserCache(parserName)
    else {
      val p = node2parser(ParserBank(parserName))
      parserCache(parserName) = p
      p
    }
  }

  def createParserFor(parserName: String): Parser[_] = {
    parserCache.clear()
    ok = true
    val theParser = getParser(parserName)
    if (ok) theParser else err("Parser '%s' has errors".format(parserName))
  }
  private val traceStack = new Stack[Position]()
  private var ok = true
  private val parserCache = Map[String, Parser[_]]()
}
