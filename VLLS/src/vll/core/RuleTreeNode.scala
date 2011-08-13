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

abstract sealed class RuleTreeNode (
  var multiplicity: Multiplicity.Value
) extends mutable.ArrayBuffer[RuleTreeNode] {
  val seqNbr = RuleTreeNode.getSeqNbr
  var errorMessage = ""
  var description = ""
  var drop = false
  var trace = false
  var parent: RuleTreeNode = null
  private var actionHasSyntaxError = false
  private var actionCode: String = ""
  var actionFunction: VllParsers.ActionType = null
  def actionText = actionCode
  def actionText_=(newCode: String) {
    actionCode = newCode
    actionHasSyntaxError = false
    if (!actionCode.trim.isEmpty) {
      if (Utils.isJavascriptCode(actionCode))
        try {actionFunction = JsEngine.compile(actionCode)} catch {case x => actionHasSyntaxError = true; actionFunction = null; throw x}
      else if (Utils.isScalaCode(actionCode))
        try {actionFunction = ScalaEngine.compile(actionCode)} catch {case x => actionHasSyntaxError = true; actionFunction = null; throw x}
      else {
        actionHasSyntaxError = true
        actionFunction = null
        throw new Exception("Not a Javascript or Scala function")
      }
    }
  }
  def cloneTree: RuleTreeNode
  def epsilonOk: Boolean
  protected var isValidChecks = List[Pair[Function0[Boolean], String]](
    Pair(() => !actionHasSyntaxError, "Syntax error in action code")
  )
  def isValid: Boolean = {
    isValidMessage = null
    isValidChecks.foldLeft(true)((t, c) => {val ok = c._1(); if (!ok) if (isValidMessage eq null) isValidMessage = c._2 else isValidMessage += (", " + c._2); (ok && t)})
  }
  var isValidMessage: String = null
  def nodeName: String = if (parent eq null) "*" else parent.nodeName + "_" + parent.indexOf(this)
  def allNodes: mutable.ArrayBuffer[RuleTreeNode] = flatMap(n => (n: @unchecked) match {
      case LiteralNode(_, _) => mutable.ArrayBuffer(n)
      case RegexNode(_, _) => mutable.ArrayBuffer(n)
      case ReferenceNode(_, _) => mutable.ArrayBuffer(n)
      case RepSepNode(_) => n.allNodes += n
      case ChoiceNode(_) => n.allNodes += n
      case SequenceNode(_) => n.allNodes += n
      case PredicateNode() => mutable.ArrayBuffer(n)
    })
  protected def copyFieldsFrom(n: RuleTreeNode) {
    drop = n.drop
    errorMessage = n.errorMessage
    try {actionText = n.actionText} catch {case _ =>}
    actionFunction = n.actionFunction
    actionHasSyntaxError = n.actionHasSyntaxError
  }
}

object RuleTreeNode {
  private var seqNbr = 0
  private def getSeqNbr = {seqNbr += 1; seqNbr}
  val refStack = new mutable.Stack[String]
}

case class PredicateNode extends RuleTreeNode(Multiplicity.One) {
  override def equals(other: Any) = other match {case r: AnyRef => this eq r; case _ => false}
  def epsilonOk = multiplicity == Multiplicity.ZeroOrMore || multiplicity == Multiplicity.ZeroOrOne
  def cloneTree() = {
    val clone = PredicateNode()
    clone.copyFieldsFrom(this)
    clone
  }
  isValidChecks ::= Pair(()=>{!actionText.isEmpty}, "Predicate code is empty")
}

case class LiteralNode(multi: Multiplicity.Value, literalName: String) extends RuleTreeNode(multi) {
  override def equals(other: Any) = other match {case r: AnyRef => this eq r; case _ => false}
  def epsilonOk = multiplicity == Multiplicity.ZeroOrMore || multiplicity == Multiplicity.ZeroOrOne
  def cloneTree() = {
    val clone = LiteralNode(multiplicity, literalName)
    clone.copyFieldsFrom(this)
    clone
  }
}

case class RegexNode(multi: Multiplicity.Value, regexName: String) extends RuleTreeNode(multi) {
  override def equals(other: Any) = other match {case r: AnyRef => this eq r; case _ => false}
  def epsilonOk = multiplicity == Multiplicity.ZeroOrMore || multiplicity == Multiplicity.ZeroOrOne
  def cloneTree() = {
    val clone = RegexNode(multiplicity, regexName)
    clone.copyFieldsFrom(this)
    clone
  }
}

case class RootNode(var name: String, var isPackrat: Boolean = false) extends RuleTreeNode(Multiplicity.One) {
  override def equals(other: Any) = other match {case r: AnyRef => this eq r; case _ => false}
  def epsilonOk = {
    if (isEmpty) false 
    else if (RuleTreeNode.refStack.contains(name)) false
    else {RuleTreeNode.refStack.push(name); val res = head.epsilonOk; RuleTreeNode.refStack.pop(); res}
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
  override def allNodes: mutable.ArrayBuffer[RuleTreeNode] = super.allNodes += this
}

case class RepSepNode (multi: Multiplicity.Value = Multiplicity.ZeroOrMore) extends RuleTreeNode(multi) {
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

case class SequenceNode (multi: Multiplicity.Value = Multiplicity.One) extends RuleTreeNode(multi) {
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

case class ChoiceNode (multi: Multiplicity.Value = Multiplicity.One) extends RuleTreeNode(multi) {
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

case class ReferenceNode(multi: Multiplicity.Value, var ruleName: String) extends RuleTreeNode(multi) {
  override def equals(other: Any) = other match {case r: AnyRef => this eq r; case _ => false}
  def epsilonOk = multiplicity == Multiplicity.ZeroOrMore || multiplicity == Multiplicity.ZeroOrOne 
  def cloneTree() = {
    val clone = ReferenceNode(multiplicity, ruleName)
    clone.copyFieldsFrom(this)
    clone
  }
}
