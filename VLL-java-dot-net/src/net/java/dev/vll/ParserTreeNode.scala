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

import java.awt.Image
import javax.swing.tree.{DefaultMutableTreeNode, DefaultTreeModel}

object Multiplicity extends Enumeration {
  val One = Value("1")
  val ZeroOrOne = Value("?")
  val ZeroOrMore = Value("*")
  val OneOrMore = Value("+")
  val Zero = Value("0")
}

abstract sealed class ParserTreeNode (
  var multiplicity: Multiplicity.Value,
  val imageName: String,
  var trace: Boolean = false
) extends DefaultMutableTreeNode {
  lazy val image: Image = swing.Swing.Icon(getClass.getResource(imageName)).getImage
  var errorMessage = ""
  def cloneTree: ParserTreeNode
  def isComplete: Boolean
  def nodeName: String = getParent.asInstanceOf[ParserTreeNode].nodeName + "." + getParent.getIndex(this)
  def getChild(i: Int) = getChildAt(i).asInstanceOf[ParserTreeNode]
  def annotation = (trace, errorMessage) match {
    case (false, "") => "   "
    case (false, _) => "[M]"
    case (true, "") => "[T]"
    case (true, _) => "[#]"
  }
}

class LiteralNode(val literalName: String, multi: Multiplicity.Value = Multiplicity.One) extends
ParserTreeNode(multi, "Literal.gif") {
  override def toString = "%s %s %s  ".format(multiplicity, literalName, annotation)
  def cloneTree() = {
    val clone = new LiteralNode(literalName, multiplicity)
    clone.errorMessage = errorMessage
    clone
  }
  def isComplete = true
}

object LiteralNode {
  def unapply(n: LiteralNode) = Some(Pair(n.multiplicity, n.literalName))
}

class RegexNode(val regexName: String, multi: Multiplicity.Value = Multiplicity.One) extends
ParserTreeNode(multi, "Regex.gif") {
  override def toString = "%s %s %s  ".format(multiplicity, regexName, annotation)
  def cloneTree() = {
    val clone = new RegexNode(regexName, multiplicity)
    clone.errorMessage = errorMessage
    clone
  }
  def isComplete = true
}

object RegexNode {
  def unapply(n: RegexNode) = Some(Pair(n.multiplicity, n.regexName))
}

class RootNode(val name: String) extends ParserTreeNode(Multiplicity.One, "Root.gif") {
  override def toString = "<html><font color=\"%s\">1 (root) %s  </font></html>".
      format(if (isComplete) "black" else "red", annotation)
  def cloneTree() = {
    val clone = new RootNode(name)
    clone.errorMessage = errorMessage
    val model = new DefaultTreeModel(clone)
    for (i <- 0 until getChildCount) {
      val child = getChild(i)
      val childClone = child.cloneTree
      model.insertNodeInto(childClone, clone, i)
    }
    model.nodeStructureChanged(clone)
    clone
  }
  def isComplete = getChildCount > 0
  override def nodeName = name
}

object RootNode {
  def unapply(n: RootNode) = Some(n.multiplicity)
}

class RepSepNode (multi: Multiplicity.Value = Multiplicity.ZeroOrMore) extends
ParserTreeNode(multi, "RepSep.gif") {
  override def toString = "<html><font color=\"%s\">%s (%s) %s  </font></html>".
      format(if (isComplete) "black" else "red" ,multiplicity, nodeName, annotation)
  def cloneTree() = {
    val clone = new RepSepNode(multiplicity)
    clone.errorMessage = errorMessage
    val model = new DefaultTreeModel(clone)
    for (i <- 0 until getChildCount) {
      val child = getChild(i)
      val childClone = child.cloneTree
      model.insertNodeInto(childClone, clone, i)
    }
    model.nodeStructureChanged(clone)
    //printf("Sequence Clone child-count: %d\n", clone.getChildCount)
    clone
  }
  def isComplete = getChildCount == 2
}

object RepSepNode {
  def unapply(n: RepSepNode) = Some(n.multiplicity)
}

class SequenceNode (multi: Multiplicity.Value = Multiplicity.One) extends
ParserTreeNode(multi, "Sequence.gif") {
  override def toString = "<html><font color=\"%s\">%s (%s) %s  </font></html>".
      format(if (isComplete) "black" else "red" ,multiplicity, nodeName, annotation)
  def cloneTree() = {
    val clone = new SequenceNode(multiplicity)
    clone.errorMessage = errorMessage
    val model = new DefaultTreeModel(clone)
    for (i <- 0 until getChildCount) {
      val child = getChild(i)
      val childClone = child.cloneTree
      model.insertNodeInto(childClone, clone, i)
    }
    model.nodeStructureChanged(clone)
    //printf("Sequence Clone child-count: %d\n", clone.getChildCount)
    clone
  }
  def isComplete = getChildCount >= 2
}

object SequenceNode {
  def unapply(n: SequenceNode) = Some(n.multiplicity)
}

class ChoiceNode (multi: Multiplicity.Value = Multiplicity.One) extends
ParserTreeNode(multi, "Choice.gif") {
  override def toString = "<html><font color=\"%s\">%s (%s) %s  </font></html>".
      format(if (isComplete) "black" else "red" ,multiplicity, nodeName, annotation)
  def cloneTree() = {
    val clone = new ChoiceNode(multiplicity)
    clone.errorMessage = errorMessage
    val model = new DefaultTreeModel(clone)
    for (i <- 0 until getChildCount) {
      val child = getChild(i)
      val childClone = child.cloneTree
      model.insertNodeInto(childClone, clone, i)
    }
    model.nodeStructureChanged(clone)
    //printf("Choice Clone child-count: %d\n", clone.getChildCount)
    clone
  }
  def isComplete = getChildCount >= 2
}

object ChoiceNode {
  def unapply(n: ChoiceNode) = Some(n.multiplicity)
}

class ReferenceNode(val parserName: String, multi: Multiplicity.Value = Multiplicity.One) extends
ParserTreeNode(multi, "Reference.gif") {
  override def toString = "%s %s %s  ".format(multiplicity, parserName, annotation)
  def cloneTree() = {
    val clone = new ReferenceNode(parserName, multiplicity)
    clone.errorMessage = errorMessage
    clone
  }
  def isComplete = true
}

object ReferenceNode {
  def unapply(n: ReferenceNode) = Some(Pair(n.multiplicity, n.parserName))
}

