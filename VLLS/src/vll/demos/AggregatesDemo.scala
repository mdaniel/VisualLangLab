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
  
  def which = "hello" | "world" | "\\d+".r
  def which2 = choice("hello", "world", "\\d+".r)

  def all = "hello" ~> "world" ~> "\\d+".r
  def all2 = sequence(Triple("hello", false, false), Triple("world", false, false), Triple("\\d+".r, false, false))
  def all3 = sequence(Triple("hello", true, false), Triple("world", true, false), Triple("\\d+".r, false, false))
  
  def main(args: Array[String]) {
    println("|: " + parseAll(which*, "hello world 2011"))
    println("choice: " + parseAll(which2*, "hello world 2011"))
    println("~: " + parseAll(all, "hello world 2011"))
    parseAll(all2, "hello world 2011").get match {
      case a: Array[_] => println(a.mkString("sequence: ", ", ", ""))
    }
    parseAll(all3, "hello world 2011").get match {
      case a: Array[String] => println(a.mkString("sequence/Drop: ", ", ", ""))
      case x => println("sequence/Drop: " + x)
    }
  }
}
