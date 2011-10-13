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

package vll.core

import scala.collection._
import JavaConversions._
import java.util.{Collection => JavaCollection, Iterator => JavaIterator}
import scala.util.parsing.combinator.Parsers

trait Aggregates {
  self: Parsers =>

  protected def choice(errMsg: Option[String], ps: Parser[_]*): Parser[_] = {
    ps.zipWithIndex.map(tup => (tup._1) ^^ (res => Pair(tup._2, res))).reduceLeft(_ | _)
  }

  protected def choice(ps: Parser[_]*): Parser[_] = {
    ps.zipWithIndex.map(tup => (tup._1) ^^ (res => Pair(tup._2, res))).reduceLeft(_ | _)
  }

  private def tilde2array(t: Any): Any = {
    def makeArray(size: Int, obj: Any): Array[Any] = obj match {
      case ~(a, b) => val arr = makeArray(size + 1, a); arr(arr.length - size) = b; arr
      case _ => val arr = new Array[Any](size); arr(0) = obj; arr
    }
    t match {
      case ~(_, _) => makeArray(1, t)
      case _ => t
    }
  }

  protected def sequence(ps: Triple[Parser[Any], Boolean, Boolean]*): Parser[_] = {
      // (Parser, to-drop, to-commit)
    ps.reduceLeft((p1, p2) => (p1._2, p2._2, p1._3) match {
        case (false, false, false) => Tuple3(p1._1 ~ p2._1, (p1._2 && p2._2), (p1._3 || p2._3))
        case (false, false, true) => Tuple3(p1._1 ~ commit(p2._1), (p1._2 && p2._2), (p1._3 || p2._3))
        case (false, true, false) => Tuple3((p1._1 <~ p2._1), (p1._2 && p2._2), (p1._3 || p2._3))
        case (false, true, true) => Tuple3(p1._1 <~ commit(p2._1), (p1._2 && p2._2), (p1._3 || p2._3))
        case (true, false, false) => Tuple3((p1._1 ~> p2._1), (p1._2 && p2._2), (p1._3 || p2._3))
        case (true, false, true) => Tuple3(p1._1 ~> commit(p2._1), (p1._2 && p2._2), (p1._3 || p2._3))
        case (true, true, false) => Tuple3((p1._1 ~> p2._1), (p1._2 && p2._2), (p1._3 || p2._3))
        case (true, true, true) => Tuple3(p1._1 ~> commit(p2._1), (p1._2 && p2._2), (p1._3 || p2._3))
      })._1 ^^ tilde2array
  }
  
  def ast4jvm(tree: Any): Object = {
    val rv = tree match {
      case a: Array[_] => a.map(ast4jvm).toArray // Sequence
      case p: Pair[Int, _] => Array[Any](p._1, ast4jvm(p._2)) // Choice
      case None => Array[Any]() // mult = ?
      case Some(s) => Array[Any](ast4jvm(s)) // mult = ?
      case lst: List[_] => val iterator: JavaIterator[_] = lst.map(ast4jvm).iterator // repsep, rep1sep, and mult = *, +
        iterator
      case s: String => s // Literal and Regex
      case null => null
      case r: AnyRef => r // something produced by action-code
    }
    rv
  }

}
