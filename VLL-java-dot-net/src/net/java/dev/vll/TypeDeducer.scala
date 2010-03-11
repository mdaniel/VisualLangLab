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

import scala.collection.mutable.Stack

object TypeDeducer {

  var stack: Stack[ParserTreeNode] = null

  def applyMultiplicity(t: String, node: ParserTreeNode): String = node.multiplicity match {
    case Multiplicity.One => t
    case Multiplicity.OneOrMore | Multiplicity.ZeroOrMore => "List[%s]".format(t)
    case Multiplicity.ZeroOrOne => "Option[%s]".format(t)
  }

  def getType(node: ParserTreeNode, depthLimit: Int) = {
    stack = new Stack()
    returnType(node, 1, depthLimit)
  }

  private def returnType(node: ParserTreeNode, depth: Int, limit: Int): String = {
    if (depth > limit)
      "_"
    else {
      stack.push(node)
      val basicType = node match {
        case LiteralNode(_, _) => "String"
        case RegexNode(_, _) => "String"
        case root: RootNode => if (root.isComplete) returnType(root.getChild(0), depth, limit) else "?"
        case seq: SequenceNode => seq.getChildCount match {
            case 0 => "~[?,?]"
            case 1 => "~[%s,?]".format(returnType(seq.getChild(0), depth + 1, limit))
            case 2 => "~[%s,%s]".format(returnType(seq.getChild(0), depth + 1, limit), returnType(seq.getChild(1), depth + 1, limit))
            case _ =>
              val ch = new Array[ParserTreeNode](seq.getChildCount)
              for (i <- 0 until seq.getChildCount) {ch(i) = seq.getChild(i)}
              val seed = returnType(ch(0), depth + 1, limit)
              ch.tail.foldLeft (seed) ((a: String, b: ParserTreeNode) => "~[%s,%s]".format(a, returnType(b, depth + 1, limit)))
          }
        case choice: ChoiceNode =>
          val childCount = choice.getChildCount
          childCount match {
            case 0 => "?"
            case _ =>
              val sb = new StringBuilder; sb.append("A[")
              for (i <- 0 until choice.getChildCount) {
                if (i != 0) sb.append(",")
                sb.append(returnType(choice.getChild(i), depth + 1, limit))
              }
              sb.append(if (childCount > 1) "]" else ",?]")
              sb.toString
          }
        case rs: RepSepNode => if (rs.getChildCount > 0) returnType(rs.getChild(0), depth + 1, limit) else "?"
        case ReferenceNode(_, parserName) =>
          val that = ParserBank(parserName)
          if (stack.contains(that)) "_" else returnType(that, depth, limit)
        case _ => "*"
      }
      stack.pop()
      applyMultiplicity(basicType, node)
    }
  }
}
