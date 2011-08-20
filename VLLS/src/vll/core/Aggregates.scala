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
import scala.util.parsing.combinator.Parsers

trait Aggregates {
  self: Parsers =>

   protected def choice(errMsg: Option[String], ps: Parser[_]*): Parser[_] = {
    ps.zipWithIndex.map(tup => (tup._1) ^^ (res => Pair(tup._2, res))).reduceLeft(_ | _)
  }

  protected def choice(ps: Parser[_]*): Parser[_] = {
    ps.zipWithIndex.map(tup => (tup._1) ^^ (res => Pair(tup._2, res))).reduceLeft(_ | _)
  }

  protected class SequenceMbr(protected val parser: Parser[Any]) {
    def commit = false
    def toDrop = false
    def p = parser
  }
  protected case class Drop(pp: Parser[_]) extends SequenceMbr(pp) {
    override def toDrop = true
  }
   protected case class Not(pp: Parser[_]) extends SequenceMbr(pp) {
    override def toDrop = true
  }
  protected case class Guard(pp: Parser[_]) extends SequenceMbr(pp) {
    override def toDrop = true
  }
  def Commit(pp: Parser[_]) = new SequenceMbr(pp) {override def commit = true}
  def Commit(sm: SequenceMbr) = sm match {
    case Drop(p) =>  new Drop(p) {override def commit = true}
    case Not(p) =>  new Not(p) {override def commit = true}
    case Guard(p) =>  new Guard(p) {override def commit = true}
    case sm: SequenceMbr =>  new SequenceMbr(sm.p) {override def commit = true}
  }
  implicit def parser2other(pr: Parser[_]) = new SequenceMbr(pr)
  implicit def any2other2[T](t: T)(implicit vu: T=>Parser[String]) = new SequenceMbr(vu(t))
  
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

  protected def sequence(ps: SequenceMbr*): Parser[_] = {
    val t3 = ps.map(nz => Tuple3(nz.p, nz.toDrop, nz.commit))
    t3.reduceLeft((p1, p2) => (p1._2, p2._2, p1._3) match {
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
  
}
