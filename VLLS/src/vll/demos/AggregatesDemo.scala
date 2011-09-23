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
package vll.demos

import scala.util.parsing.combinator.RegexParsers
import vll.core.Aggregates

object AggregatesDemo extends RegexParsers with Aggregates {
  def main(args: Array[String]) {
    def alt = "hello" | "world" | "\\d+".r
    def alt2 = choice("hello", "world", "\\d+".r)
    println("alt: " + parseAll(alt*, "hello world 2011"))
    println("alt2: " + parseAll(alt2*, "hello world 2011"))
    
    def seq = "hello" ~ "world" ~ "\\d+".r
    def seq2 = sequence(Triple("hello", false, false), Triple("world", false, false), Triple("\\d+".r, false, false))
    println("seq: " + parseAll(seq, "hello world 2011"))
    parseAll(seq2, "hello world 2011").get match {
      case a: Array[_] => println(a.mkString("seq2: Array(", ", ", ")"))
    }
    
    def seq3 = "hello" ~> "world" ~> "\\d+".r
    def seq4 = sequence(Triple("hello", true, false), Triple("world", true, false), Triple("\\d+".r, false, false))
    println("seq3: " + parseAll(seq3, "hello world 2011"))
    parseAll(seq4, "hello world 2011").get match {
      case a: Array[String] => println(a.mkString("seq4: ", ", ", ""))
      case x => println("seq4: " + x)
    }
  }
}
