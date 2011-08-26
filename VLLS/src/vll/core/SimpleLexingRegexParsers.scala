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

import scala.collection._
import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers
import java.util.regex.Pattern

trait SimpleLexingRegexParsers extends RegexParsers {

  private def escapeRegexMetachars(es: String): String = {
    val sb = new StringBuilder()
    for (c <- es) {
      c match {
        case '|' | '*' | '+' | '?' | '-' => sb.append('\\').append(c)
        case '(' | ')' | '[' | ']' | '{' | '}' => sb.append('\\').append(c)
        case ',' | '^' | '.' | '$' | '\"' => sb.append('\\').append(c)
        case _ => sb.append(c)
      }
    }
    sb.toString
  }

  def literal$(s: String) = super.literal(s)
  def regex$(r: Regex) = super.regex(r)

  implicit override def literal(lit: String): Parser[String] = {
    if ((lit eq null) || lit.isEmpty)
      throw new IllegalArgumentException("Null or empty literal string")
    if (tokenParserMap.contains(lit))
      tokenParserMap(lit)
    else {
      setupNeeded = true
      // literals have IDs -1, -2, -3 ...
      // order of following 2 lines must not be changed
      lit +=: theLiterals
      val parser = parserById(-theLiterals.size)
      tokenParserMap(lit) = parser
      parser
    }
  }

  implicit override def regex(reg: Regex): Parser[String] = {
    if (reg eq null)
      throw new IllegalArgumentException("Null regex value")
    val regAsString = reg.toString
    if (regAsString.isEmpty)
      throw new IllegalArgumentException("Empty regex string")
    if (tokenParserMap.contains(regAsString))
      tokenParserMap(regAsString)
    else {
      setupNeeded = true
      // regexs have IDs 0, 1, 2 ...
      // order of following 2 lines must not be changed
      val parser = parserById(theRegexs.size)
      theRegexs += regAsString 
      tokenParserMap(regAsString) = parser
      parser
    }
  }

  private def parserById(id: Int): Parser[String] = {
    def formatToken(tid: Int) = "%s(%s)".format((if (tid < 0) "literal" else "regex"), allTokens(tid + literalBaseIndex))
    Parser(in => {
        if (traceTokens)
          printf("Trying %s @ (%d,%d)%n", formatToken(id), in.pos.line, in.pos.column)
        if (in.atEnd)
          Failure("End-of-input found", in)
        else globalTokenLexer(in.source.toString, in.offset) match {
          case Triple(null, _, charsConsumed) =>
            val next = in.drop(charsConsumed)
            if (next.atEnd) {
              if (traceTokens)
                printf("Failure %s @ (%d,%d)%n", "End-of-input found", in.pos.line, in.pos.column)
              Failure("End-of-input found", in)
            } else {
              if (traceTokens)
                printf("Failure %s @ (%d,%d)%n", formatToken(id), in.pos.line, in.pos.column)
              Failure("Unknown-inputs found", in)
            }
          case Triple(matchString, matchId, charsConsumed) => 
            if (matchId == id) {
              val next = in.drop(charsConsumed)
              if (traceTokens)
                printf("Success %s @ (%d,%d)%n", formatToken(id), next.pos.line, next.pos.column)
              Success(matchString, next)
            } else {
              if (traceTokens)
                printf("Failure %s @ (%d,%d)%n", formatToken(id), in.pos.line, in.pos.column)
              Failure("expected %s (#%d), found %s (#%d) @ (%d,%d)".format(
                  formatToken(id), id, formatToken(matchId), matchId, in.pos.line, in.pos.column), in)
            }
        }
      })
  }
  
  private def globalTokenLexer(inStr: String, offset: Int): LexResults = {
    if (setupNeeded) {
      setupLexer()
      setupNeeded = false
    }
    if (/* offset > maxOffset ||  */!lexResultsCache.contains(offset)) {
      val postSpaces = handleWhiteSpace(inStr, offset)
      val res = lex(inStr, postSpaces)
      val lexResult = (res._1, res._2, if (res._1 eq null) (postSpaces - offset) else (postSpaces - offset + res._1.length))
      if (res._1 ne null)
        lexResultsCache(offset) = lexResult
//      maxOffset = offset
      lexResult
    } else {
      lexResultsCache(offset)
    }
  }
  
  override def phrase[T](p: Parser[T]): Parser[T] = {
    lexResultsCache.clear()
//    maxOffset = -1
    super.phrase(p)
  }

  private def setupLexer() {
    tokenIndices.clear()
    literalBaseIndex = theLiterals.size
    theLiterals.zipWithIndex.foreach(p => tokenIndices(p._1) = p._2 - literalBaseIndex)
    theRegexs.zipWithIndex.foreach(p => tokenIndices(p._1) = p._2)
    val escLits = theLiterals.map(escapeRegexMetachars)
    allTokens = Array(escLits, theRegexs).flatMap(x => x)
    tokenPatterns = allTokens.map(Pattern.compile(_, (Pattern.MULTILINE | Pattern.DOTALL))).
      zipWithIndex.map(p => Pair(p._1, p._2 - literalBaseIndex)).par
    tokenParserMap(escapeRegexMetachars("\\z")) = super.regex("\\z".r)
  }

  private def lex(s: String, offset: Int) = {
    val matchesByLength = tokenPatterns.map(pi => {
        val m = (pi._1).matcher(s.subSequence(offset, s.length))
        if (m.lookingAt) {
          (m.group, pi._2)
        } else {
          (null, 0)
        }
      }).seq.filter(_._1 ne null).sortWith((a, b) => {a._1.length > b._1.length});
    if (matchesByLength.isEmpty) {
      (null, 0)
    } else {
      val maxLength = matchesByLength.head._1.length;
      val byLengthAndPriority = matchesByLength.takeWhile(_._1.length == maxLength).sortWith((a, b) => {a._2 < b._2})
      byLengthAndPriority.head
    }
  }
  
  private var tokenPatterns: parallel.mutable.ParArray[Tuple2[Pattern, Int]] = null
  private val tokenParserMap = mutable.HashMap[String, Parser[String]]()
  var traceTokens = false
//  private var maxOffset = -1
  private type LexResults = Triple[String, Int, Int] // matched-string, token-id, chars-consumed
  private val lexResultsCache = mutable.HashMap[Int, LexResults]()
  private var allTokens: Array[String] = null
  private val tokenIndices = mutable.HashMap[String, Int]()
  private val theLiterals, theRegexs = mutable.Buffer[String]()
  private var setupNeeded = true
  var literalBaseIndex = 0
}

object Main extends SimpleLexingRegexParsers {
  def main(args: Array[String]) {
    literal("end")
    lazy val line = "begin" ~ rep("[a-z]+".r | "\\d+".r) ~ "end"
    println(parseAll(line, "begin 12345 end"))
    println(parseAll(line, "begin 12345 alphabetics %^$#@ end"))
    println(parseAll(line, "begin 12345 begin end"))
    println(parseAll(line, "begin hello 1984 this is 2011 end"))
    println(parseAll(line, "begin 12345 alphas end begin-again!"))
  }
}
