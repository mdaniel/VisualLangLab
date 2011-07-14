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

import scala.collection._

abstract sealed class ParserTreeNode (
  var multiplicity: Multiplicity.Value
) extends mutable.ArrayBuffer[ParserTreeNode] {
  val seqNbr = ParserTreeNode.getSeqNbr
  var errorMessage = ""
  var drop = false
  var trace = false
  var parent: ParserTreeNode = null
  var actionText: String = ""
  def cloneTree: ParserTreeNode
  def epsilonOk: Boolean
  protected var isValidChecks = List[Pair[Function0[Boolean], String]](
    Pair(()=>{if (actionText.isEmpty) {true} else {
          if (Utils.isJavascriptCode(actionText))
            try {JsEngine.compile(actionText); true} catch {case _ => false}
          else if (Utils.isScalaCode(actionText))
            try {ScalaEngine.compile(actionText); true} catch {case _ => false}
          else
            false
        }}, "Syntax error in JS action text")
  )
  def isValid: Boolean = {
    isValidMessage = null
    isValidChecks.foldLeft(true)((t, c) => {val ok = c._1(); if (!ok) if (isValidMessage eq null) isValidMessage = c._2 else isValidMessage += (", " + c._2); (ok && t)})
  }
  var isValidMessage: String = null
  def nodeName: String = if (parent eq null) "*" else parent.nodeName + "_" + parent.indexOf(this)
  def allNodes: mutable.ArrayBuffer[ParserTreeNode] = flatMap(n => (n: @unchecked) match {
      case LiteralNode(_, _) => mutable.ArrayBuffer(n)
      case RegexNode(_, _) => mutable.ArrayBuffer(n)
      case ReferenceNode(_, _) => mutable.ArrayBuffer(n)
      case RepSepNode(_) => n.allNodes += n
      case ChoiceNode(_) => n.allNodes += n
      case SequenceNode(_) => n.allNodes += n
      case PredicateNode() => mutable.ArrayBuffer(n)
    })
  protected def copyFieldsFrom(n: ParserTreeNode) {
    drop = n.drop
    errorMessage = n.errorMessage
    actionText = n.actionText
  }
}

object ParserTreeNode {
  private var seqNbr = 0
  private def getSeqNbr = {seqNbr += 1; seqNbr}
  val refStack = new mutable.Stack[String]
}

case class PredicateNode extends ParserTreeNode(Multiplicity.One) {
  override def equals(other: Any) = other match {case r: AnyRef => this eq r; case _ => false}
  def epsilonOk = multiplicity == Multiplicity.ZeroOrMore || multiplicity == Multiplicity.ZeroOrOne
  def cloneTree() = {
    val clone = PredicateNode()
    clone.copyFieldsFrom(this)
    clone
  }
  isValidChecks ::= Pair(()=>{!actionText.isEmpty}, "Predicate code is empty")
}

case class LiteralNode(multi: Multiplicity.Value, literalName: String) extends ParserTreeNode(multi) {
  override def equals(other: Any) = other match {case r: AnyRef => this eq r; case _ => false}
  def epsilonOk = multiplicity == Multiplicity.ZeroOrMore || multiplicity == Multiplicity.ZeroOrOne
  def cloneTree() = {
    val clone = LiteralNode(multiplicity, literalName)
    clone.copyFieldsFrom(this)
    clone
  }
}

case class RegexNode(multi: Multiplicity.Value, regexName: String) extends ParserTreeNode(multi) {
  override def equals(other: Any) = other match {case r: AnyRef => this eq r; case _ => false}
  def epsilonOk = multiplicity == Multiplicity.ZeroOrMore || multiplicity == Multiplicity.ZeroOrOne
  def cloneTree() = {
    val clone = RegexNode(multiplicity, regexName)
    clone.copyFieldsFrom(this)
    clone
  }
}

case class RootNode(var name: String, var isPackrat: Boolean = false) extends ParserTreeNode(Multiplicity.One) {
  override def equals(other: Any) = other match {case r: AnyRef => this eq r; case _ => false}
  def epsilonOk = {
    if (isEmpty) false 
    else if (ParserTreeNode.refStack.contains(name)) false
    else {ParserTreeNode.refStack.push(name); val res = head.epsilonOk; ParserTreeNode.refStack.pop(); res}
  }
  def cloneTree() = {
    val clone = RootNode(name)
    clone.copyFieldsFrom(this)
    clone.append(head.cloneTree)
    clone.head.parent = clone
    clone
  }
  isValidChecks ::= Pair(()=>{!isEmpty}, "RootNode needs 1 child node")
  isValidChecks ::= Pair(()=>{!epsilonOk}, "Matches the empty string")
  override def nodeName = name
  override def allNodes: mutable.ArrayBuffer[ParserTreeNode] = super.allNodes += this
}

case class RepSepNode (multi: Multiplicity.Value = Multiplicity.ZeroOrMore) extends ParserTreeNode(multi) {
  override def equals(other: Any) = other match {case r: AnyRef => this eq r; case _ => false}
  def epsilonOk = if (isEmpty) false else multiplicity == Multiplicity.ZeroOrMore 
  def cloneTree() = {
    val clone = RepSepNode(multiplicity)
    clone.copyFieldsFrom(this)
    foreach(ch => clone.append(ch.cloneTree))
    clone.foreach(_.parent = clone)
    clone
  }
  isValidChecks ::= Pair(()=>{size >= 2}, "RepSep needs 2 child nodes")
}

case class SequenceNode (multi: Multiplicity.Value = Multiplicity.One) extends ParserTreeNode(multi) {
  var commitPoint = -1
  override def equals(other: Any) = other match {case r: AnyRef => this eq r; case _ => false}
  def epsilonOk = if (isEmpty) false else multiplicity == Multiplicity.ZeroOrMore || multiplicity == Multiplicity.ZeroOrOne ||
      forall(_.epsilonOk)
  def cloneTree() = {
    val clone = SequenceNode(multiplicity)
    clone.copyFieldsFrom(this)
    foreach(ch => clone.append(ch.cloneTree))
    clone.foreach(_.parent = clone)
    clone
  }
  isValidChecks ::= Pair(()=>{size >= 2}, "Sequence needs >= 2 child nodes")
  isValidChecks ::= Pair(()=>{isEmpty || exists(n => !n.drop)}, "Can't drop all child nodes")
}

case class ChoiceNode (multi: Multiplicity.Value = Multiplicity.One) extends ParserTreeNode(multi) {
  override def equals(other: Any) = other match {case r: AnyRef => this eq r; case _ => false}
  def epsilonOk = if (isEmpty) false else multiplicity == Multiplicity.ZeroOrMore || multiplicity == Multiplicity.ZeroOrOne ||
      exists(_.epsilonOk)
  def cloneTree() = {
    val clone = ChoiceNode(multiplicity)
    clone.copyFieldsFrom(this)
    foreach(ch => clone.append(ch.cloneTree))
    clone.foreach(n => n.parent = clone)
    clone
  }
  isValidChecks ::= Pair(()=>{size >= 2}, "Choice needs >= 2 child nodes")
}

case class ReferenceNode(multi: Multiplicity.Value, var parserName: String) extends ParserTreeNode(multi) {
  override def equals(other: Any) = other match {case r: AnyRef => this eq r; case _ => false}
  def epsilonOk = multiplicity == Multiplicity.ZeroOrMore || multiplicity == Multiplicity.ZeroOrOne 
  def cloneTree() = {
    val clone = ReferenceNode(multiplicity, parserName)
    clone.copyFieldsFrom(this)
    clone
  }
}
