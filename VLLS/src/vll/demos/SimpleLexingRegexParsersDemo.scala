/*
    Copyright 2011, Sanjay Dasgupta
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
package demos

import scala.util.parsing.combinator.JavaTokenParsers
import scala.util.parsing.combinator.RegexParsers
import vll.core.SimpleLexingRegexParsers

//object Main extends RegexParsers {
object Main extends SimpleLexingRegexParsers {
  def main(args: Array[String]) {
      /**** priming the built-in lexer ... ****/
    literal("begin"); literal("end"); 
    regex("[a-z]+".r); regex("\\d+".r)
      /**** define a parser ... ****/
    lazy val line = "begin" ~ rep("[a-z]+".r | "\\d+".r) ~ "end"
      /**** test the parser ... ****/
    println(parseAll(line, "begin hi 1984 i am 2011 end"))
    println(parseAll(line, "begin the ending of 2010 end"))
    println(parseAll(line, "begin the end of 2010 end"))
  }
}
