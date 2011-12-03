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
import java.util.regex.Matcher
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
      theLiterals += lit
      val theLiteralsSize = theLiterals.size
      val parser = Parser(in => {
        val t0 = if (profileCode) System.currentTimeMillis else 0
        val rv = parserById(-theLiteralsSize, "literal(%s)".format(lit))(in)
        if (profileCode) literalTime += System.currentTimeMillis - t0
        rv
      })
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
      val theRegexsSize = theRegexs.size
      val parser = Parser(in => {
        val t0 = if (profileCode) System.currentTimeMillis else 0
        val rv = parserById(theRegexsSize, "regex(%s)".format(regAsString))(in)
        if (profileCode) regexTime += System.currentTimeMillis - t0
        rv
      })
      theRegexs += regAsString 
      tokenParserMap(regAsString) = parser
      parser
    }
  }
  
  val wildCardParser = parserById(Int.MaxValue, "*")
  
  private def parserById(id: Int, expect: String): Parser[String] = {
    val failMsg = "Expected " + expect
    Parser(in => {
        val t0 = if (profileCode) System.currentTimeMillis else 0
//        if (traceTokens)
//          printf("Trying %s @ (%d,%d)%n", expect, in.pos.line, in.pos.column)
        val rv = if (in.atEnd)
          Failure("End-of-input found", in)
        else globalTokenLexer(in.source.toString, in.offset) match {
          case Triple(matchString, matchId, charsConsumed) => 
            if (matchString eq null) {
              val next = in.drop(charsConsumed)
              if (next.atEnd) {
//                if (traceTokens)
//                  printf("Failure %s @ (%d,%d)%n", "End-of-input found", in.pos.line, in.pos.column)
                Failure("Unexpected end-of-input", in)
              } else {
//                if (traceTokens)
//                  printf("Failure %s @ (%d,%d)%n", expect, in.pos.line, in.pos.column)
                Failure(failMsg, in)
              }
            } else {
              if ((matchId == id) || (id == Int.MaxValue)) {
                val next = in.drop(charsConsumed)
//                if (traceTokens)
//                  printf("Success %s @ (%d,%d)%n", expect, next.pos.line, next.pos.column)
                Success(matchString, next)
              } else {
//                if (traceTokens)
//                  printf("Failure %s @ (%d,%d)%n", expect, in.pos.line, in.pos.column)
                Failure(failMsg, in)
              }
            }
        }
        if (profileCode) parserByIdTime += System.currentTimeMillis - t0
        rv
      })
  }
  
  private def globalTokenLexer(inStr: String, offset: Int): LexResults = {
    val t0 = if (profileCode) System.currentTimeMillis else 0
    if (setupNeeded) {
      setupLexer()
      setupNeeded = false
    }
    val rv = if (offset > maxOffset || !lexResultsCache.contains(offset)) {
      val t00 = if (profileCode) System.currentTimeMillis else 0
      val postSpaces = handleWhiteSpace(inStr, offset)
      if (profileCode) handleWhitespaceTime += System.currentTimeMillis - t00
      val res = lex(inStr.substring(postSpaces))
      val lexResult = (res._1, res._2, if (res._1 eq null) (postSpaces - offset) else (postSpaces - offset + res._1.length))
      if (res._1 ne null) {
        lexResultsCache(offset) = lexResult
        maxOffset = offset
      }
      lexResult
    } else {
      lexResultsCache(offset)
    }
    if (profileCode) globalTokenLexerTime += System.currentTimeMillis - t0
    rv
  }
  
  override def phrase[T](p: Parser[T]): Parser[T] = {
    lexResultsCache.clear()
    maxOffset = -1
    Parser(in => {val rv = super.phrase(p)(in)
        if (profileCode) {
          printf("literalTime: %d, regexTime: %d%n", literalTime, regexTime)
          printf("parserByIdTime: %d, globalTokenLexerTime: %d%n", parserByIdTime, globalTokenLexerTime)
          printf("handleWhitespaceTime: %d, lexTime: %d%n", handleWhitespaceTime, lexTime)
          printf("literalsMatcherTime: %d, regexMatcherTime: %d%n", literalsMatcherTime, regexMatcherTime)
        }   
        rv 
    })
  }

  private def setupLexer() {
    if (!theLiterals.isEmpty) {
      val orderedLits = theLiterals.toArray.map(escapeRegexMetachars).zipWithIndex.map(p => Pair(p._1, -(p._2 + 1))).
        sortWith((l, r) => l._1.length >= r._1.length)
      literalsMatcher = Pattern.compile(orderedLits.map(_._1).mkString("(", ")|(", ")")).matcher("")
      literalIds = orderedLits.map(_._2).toArray
    }
    regexMatchers = theRegexs.toArray.map(Pattern.compile(_, (Pattern.MULTILINE | Pattern.DOTALL)).matcher("")).
      zipWithIndex.par
    tokenParserMap(escapeRegexMetachars("\\z")) = super.regex("\\z".r)
  }

  private def lex(s: String) = {
    val t0 = if (profileCode) System.currentTimeMillis else 0
    val matchingLiteral = {
      val t01 = if (profileCode) System.currentTimeMillis else 0
      val rv = if ((literalsMatcher ne null) && {literalsMatcher.reset(s); literalsMatcher.lookingAt}) {
        var k = -1
        for (i <- 1 to literalsMatcher.groupCount) 
          if (literalsMatcher.end(i) != -1)
            k = i
        (literalsMatcher.group, literalIds(k - 1))
      } else
        (null, 0)
      if (profileCode) literalsMatcherTime += System.currentTimeMillis - t01
      rv
    }
    val matchingRegex = {
      val t02 = if (profileCode) System.currentTimeMillis else 0
      val rv = regexMatchers.map(pi => {
        val m = (pi._1).reset(s)
        if (m.lookingAt) {
          (m.group, pi._2)
        } else {
          (null, 0)
        }
      }).seq.filter(_._1 ne null).sortWith((a, b) => {(a._1.length > b._1.length) || 
             ((a._1.length == b._1.length) && (a._2 < b._2))});
      if (profileCode) regexMatcherTime += System.currentTimeMillis - t02
      rv
    }
    val rv = if (matchingRegex.isEmpty) {
      matchingLiteral
    } else {
      if ((matchingLiteral._1 eq null) || (matchingRegex.head._1.length > matchingLiteral._1.length))
        matchingRegex.head else matchingLiteral
    }
    if (profileCode) lexTime += System.currentTimeMillis - t0
    rv
  }
  
  def reset() {
    literalsMatcher = null
    literalIds = null
    regexMatchers = null
    theLiterals.clear()
    theRegexs.clear()
    tokenParserMap.clear()
    lexResultsCache.clear()
    setupNeeded = true
    maxOffset = -1
    
    literalsMatcherTime = 0
    regexMatcherTime = 0
    lexTime = 0
    globalTokenLexerTime = 0
    handleWhitespaceTime = 0
    regexTime = 0
    literalTime = 0
    parserByIdTime = 0
  }

  private var profileCode = false
  private var literalsMatcher: Matcher = null
  private var literalIds: Array[Int] = null
  private var regexMatchers: parallel.mutable.ParArray[Tuple2[Matcher, Int]] = null
  private val tokenParserMap = mutable.HashMap[String, Parser[String]]()
//  var traceTokens = false
  private type LexResults = Triple[String, Int, Int] // matched-string, token-id, chars-consumed
  private val lexResultsCache = mutable.HashMap[Int, LexResults]()
  private val theLiterals, theRegexs = mutable.ArrayBuffer[String]()
  private var setupNeeded = true
  private var maxOffset = -1
  private var literalsMatcherTime, regexMatcherTime, lexTime, globalTokenLexerTime, 
      handleWhitespaceTime, regexTime, literalTime, parserByIdTime = 0L
}

object Main2 extends SimpleLexingRegexParsers {
  def main(args: Array[String]) {
      // priming the lexer ...
    literal("begin"); literal("end"); 
    regex("[a-z]+".r); regex("\\d+".r)
      // define a parser ...
    lazy val line = "begin" ~ rep("[a-z]+".r | "\\d+".r) ~ "end"
      // test the parser ...
    println(parseAll(line, "begin hi 1984 i am 2011 end"))
    println(parseAll(line, "begin the ending of 2010 end"))
    println(parseAll(line, "begin the end of 2010 end"))
  }
}
