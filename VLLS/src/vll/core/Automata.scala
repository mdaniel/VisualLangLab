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

//import scala.util.parsing.input.CharSequenceReader

import java.util.regex.Pattern

object Automata {
  
  type Input = VllParsers#Input
  
  type MatcherType = Function1[CharSequence,Int]
  type LexerType = Function1[Input,Array[Int]]
  
  def lexer(p: Array[String]): LexerType = {
    val tokenPatterns = p.par.map(Pattern.compile(_, (Pattern.MULTILINE | Pattern.DOTALL))).zipWithIndex
    val lxr = (in: Input) => {
      val matchesByLength = tokenPatterns.map(pi => {
          val s = in.source
          val offset = in.offset
          try {
            val m = (pi._1).matcher(s.subSequence(offset, s.length))
            if (m.lookingAt) {
              Array(m.end, pi._2)
            } else {
              Array(0, 0)
            }
          } catch {
            case x => printf("Automata.lexer regex problem (%s)%n", pi._1.pattern)
            throw x
          }
      }).seq.filter(_(0) != 0).sortWith((a, b) => {a(0) > b(0)});
      if (matchesByLength.isEmpty) {
        Array(0, 0)
      } else {
        val maxLength = matchesByLength.head(0);
        val byLengthAndPriority = matchesByLength.takeWhile(_(0) == maxLength).sortWith((a, b) => {a(1) < b(1)})
        byLengthAndPriority.head
      }
    }
    lxr
  }
  
  def matcher(re: String): MatcherType = {
    val matcher = Pattern.compile(re).matcher("")
    val m = (s: CharSequence) => {
//      val s = in.source.subSequence(in.offset, in.source.length)
      try {
        val mat = matcher.reset(s)
        if (mat.lookingAt) mat.end else 0
      } catch {
        case x => printf("Automata.matcher() regex problem (%s)%n", re)
          throw x
      }
    }
    m
  }
  
  def testRegexp(r: String) = {
    Pattern.compile(r)
  }
  
  def escapeMetaChars(es: String) = {
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
  
  def canMatchEmptyString(rex: String) = "".matches(rex)

//  private val emptyStringPattern = Pattern.compile("")
}
