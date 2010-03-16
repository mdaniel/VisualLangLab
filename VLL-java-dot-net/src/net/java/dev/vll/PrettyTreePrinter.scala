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

import net.java.dev.vll.VllParsers._
import scala.collection.immutable.WrappedString

object PrettyTreePrinter extends ParseTreeHandler {

  private def printTree(t: Any, level: Int) {
    val margin: String = new WrappedString(":   ") * level
    t match {
      case None => printf("%sNone()%n", margin)
      case Some(s) =>
        printf("%sSome(%n", margin)
        printTree(s, level + 1)
        printf("%s)%n", margin)
      case lst: List[_] =>
        printf("%sList(%n", margin)
        for (i <- 0 until lst.size) {
          printTree(lst(i), level + 1)
        }
        printf("%s)%n", margin)
      case a: Array[_] =>
        printf("%sArray(%n", margin)
        for (i <- 0 until a.size) {
          printTree(a(i), level + 1)
        }
        printf("%s)%n", margin)
      case ~(a, b) =>
        printf("%s~(%n", margin)
        printTree(a, level + 1)
        printTree(b, level + 1)
        printf("%s)%n", margin)
      case _ => printf("%s%s%n", margin, t)
    }
  }

  def onParse(tree: Any) {
    printTree(tree, 0)
  }
}
