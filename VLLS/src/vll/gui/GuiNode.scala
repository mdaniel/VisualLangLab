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

package vll.gui

import java.awt.Color
import javax.swing.tree.MutableTreeNode
import javax.swing.tree.TreeNode
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import vll.core.ChoiceNode
import vll.core.LiteralNode
import vll.core.Multiplicity
import vll.core.ParserTreeNode
import vll.core.ReferenceNode
import vll.core.RegexNode
import vll.core.RepSepNode
import vll.core.RootNode
import vll.core.SequenceNode
import vll.core.PredicateNode
import scala.collection.JavaConversions._

class GuiNode private (val pNode: ParserTreeNode) extends MutableTreeNode
    with Traversable[GuiNode] {
  def foreach[Unit](f: GuiNode => Unit) {
    pNode.foreach(n => f(GuiNode(n)))
  }
  override def equals(other: Any) = other match {
    case gn: GuiNode => pNode eq gn.pNode
    case _ => false
  }
  def nodeName = pNode.nodeName
  def errorMessage = pNode.errorMessage
  def errorMessage_=(msg: String) {pNode.errorMessage = msg}
  def drop = pNode.drop
  def drop_=(d: Boolean) {pNode.drop = d}
  def multiplicity = pNode.multiplicity
  def multiplicity_=(m: Multiplicity.Value) {pNode.multiplicity = m}
  def isPackrat = pNode.isInstanceOf[RootNode] && pNode.asInstanceOf[RootNode].isPackrat == true
  def isPackrat_=(p: Boolean) {pNode match {
      case rn: RootNode => rn.isPackrat = p
      case _ => throw new IllegalArgumentException("Not a RootNode")
    }}
  def trace = pNode.trace
  def trace_=(d: Boolean) {pNode.trace = d}
  override def size = pNode.size
  def getPath: Array[GuiNode] = {
    var path = ArrayBuffer[GuiNode]()
    var ptn = pNode
    while (ptn != null) {
      GuiNode(ptn) +=: path
      ptn = ptn.parent
    }
    path.toArray
  }
  def cloneTree = GuiNode(pNode.cloneTree)
  def isValid = pNode.isValid
  def isValidMessage = pNode.isValidMessage
  def epsilonOk = pNode.epsilonOk
  def displayName(full: Boolean) = {pNode match {
        case ref: ReferenceNode => if (full) (ref.parserName + "@" + ref.nodeName) else ref.parserName
        case lit: LiteralNode => if (full) (lit.literalName + "@" + lit.nodeName) else lit.literalName
        case reg: RegexNode => if (full) (reg.regexName + "@" + reg.nodeName) else reg.regexName
        case seq: SequenceNode => if (full) seq.nodeName else "(Sequence)"
        case rn: RootNode => if (rn.isPackrat) rn.name + " (Packrat)" else rn.name
        case choice: ChoiceNode => if (full) choice.nodeName else "(Choice)"
        case rs: RepSepNode => if (full) rs.nodeName else "(RepSep)"
        case pn: PredicateNode => if (full) pn.nodeName else "(Predicate)"
      }}
  def nodeAnnotations(showEpsOk: Boolean): String = {
    val commit = (pNode.parent ne null) && pNode.parent.isInstanceOf[SequenceNode] &&
    (pNode.parent.asInstanceOf[SequenceNode].commitPoint == pNode.parent.indexOf(pNode))
    if (pNode.drop || !pNode.actionText.isEmpty || !pNode.errorMessage.isEmpty || pNode.trace || epsilonOk || commit) {
      val sb = new StringBuilder()
      if (!pNode.actionText.isEmpty) sb.append("action")
      if (commit) sb.append(if (sb.isEmpty) "commit" else ", commit")
      if (pNode.drop) sb.append(if (sb.isEmpty) "drop" else ", drop")
      if (!pNode.errorMessage.isEmpty) sb.append(if (sb.isEmpty) "message" else ", message")
      if (pNode.trace) sb.append(if (sb.isEmpty) "trace" else ", trace")
      if (showEpsOk && epsilonOk) sb.append(if (sb.isEmpty) "\u03b5-ok" else ", \u03b5-ok")
      sb.toString
    } else
      ""
  }
  //
  // MutableTreeNode methods ...
  //
  private var userObject: Object = null
  def insert(child: MutableTreeNode, index: Int) {child match {
      case gn: GuiNode => pNode.insert(index, gn.pNode)
        gn.pNode.parent = pNode
      case _ => throw new IllegalArgumentException("Not a GuiNode")
    }}
  def remove(index: Int) {val rn = pNode.remove(index); rn.parent = null}
  def remove(node: MutableTreeNode) {remove(getIndex(node))}
  //def removeFromParent() {pNode.parent.remove(getIndex(this)); pNode.parent = null}
  def removeFromParent() {getParent.remove(this); pNode.parent = null}
  def setParent(newParent: MutableTreeNode) {
    newParent match {
      case gn: GuiNode => pNode.parent = gn.pNode
      case _ => throw new IllegalArgumentException("Not a GuiNode")
    }
  }
  def setUserObject(obj: Object) {userObject = obj}
  def getUserObject = userObject
  //
  // TreeNode methods ...
  //
  def children = pNode.iterator.map(x => GuiNode(x))
  def getAllowsChildren: Boolean = pNode match {
    case lit: LiteralNode => false
    case reg: RegexNode => false
    case ref: ReferenceNode => false
    case _ => true
  }
  def getChildAt(index: Int) = GuiNode(pNode(index))
  def getChildCount = pNode.size
  def getIndex(n: TreeNode) = n match {
    case gn: GuiNode => pNode.indexOf(gn.pNode)
    case _ => throw new IllegalArgumentException("Not a GuiNode")
  }
  def getParent: GuiNode = if (pNode.parent == null) null else GuiNode(pNode.parent)
  def isLeaf = pNode.isEmpty
  //
  // Others ...
  //
  override def toString = pNode.toString
  var color = Color.black
}

object GuiNode {
  def apply(pn: ParserTreeNode): GuiNode = {
    if (cache.contains(pn.seqNbr))
      cache(pn.seqNbr)
    else {
      val guiNode = new GuiNode(pn)
      cache(pn.seqNbr) = guiNode
      guiNode
    }
  }
  def clearCache() {
    cache.clear
  }
  private val cache = HashMap[Int, GuiNode]()
}
