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

package vll.core

import scala.collection.mutable.HashMap

class RuleBank extends HashMap[String, RootNode] {

  def rename(oldName: String, newName: String) {
    foreach(_._2.allNodes.foreach(_ match {
        case refNode @ ReferenceNode(_, name) => if (name == oldName) refNode.ruleName = newName
        case rootNode @ RootNode(name, _) => if (name == oldName) rootNode.name = newName
        case _ =>
    }))
    put(newName, get(oldName).get)
    remove(oldName)
  }

  def tokenInUse(tokenName: String): List[String] = {
    filter(_._2.allNodes.exists({
        case LiteralNode(_, `tokenName`) | RegexNode(_, `tokenName`) => true
        case _ => false
      })).map(_._1).toList
  }

  def ruleInUse(ruleName: String, excludeSelf: Boolean = false): List[String] = {
    filter(kv => !excludeSelf || ruleName != kv._1).filter(_._2.allNodes.exists({
          case ReferenceNode(_, `ruleName`) => true
          case _ => false
        })).map(_._1).toList
  }

  def ruleNames: Array[String] = keys.toArray.sorted
  override def clear() {
    super.clear()
    put("Main", RootNode("Main"))
  }
  put("Main", RootNode("Main"))
}
