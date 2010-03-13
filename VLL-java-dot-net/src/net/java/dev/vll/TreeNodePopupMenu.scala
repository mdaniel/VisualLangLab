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

import javax.swing.{JPopupMenu, JMenuItem, JMenu, JCheckBoxMenuItem}

object TreeNodePopupMenu extends JPopupMenu {

  def adjustMenu() {
    def selectedNode = ParserTreePanel.selectedNode
    def clipboard = ParserTreePanel.clipboard
    traceNode.setSelected(ParserTreePanel.selectedNode.trace)
    val parent = selectedNode.getParent
    val parentIndex = if (parent eq null) -1 else parent.getIndex(selectedNode)
    val parentChildCount = if (parent eq null) -1 else parent.getChildCount
    val childCount = selectedNode.getChildCount
    moveUp.setEnabled(parentIndex > 0)
    moveDown.setEnabled(parentIndex <= parentChildCount - 2)
    cutNode.setEnabled(parent != null)
    deleteNode.setEnabled(parent != null)
    multOne.setEnabled(true)
    multZeroOrOne.setEnabled(true)
    parent match {
      case x: RepSepNode =>
        multMenu.setEnabled(false)
      case null =>
        multMenu.setEnabled(false)
        errorMsg.setEnabled(false)
        traceNode.setEnabled(false)
      case _ =>
        multMenu.setEnabled(true)
        errorMsg.setEnabled(true)
        traceNode.setEnabled(true)
    }
    selectedNode match {
      case cho: ChoiceNode => addMenu.setEnabled(true)
        pasteNode.setEnabled(clipboard != null)
      case seq: SequenceNode => addMenu.setEnabled(true)
        pasteNode.setEnabled(clipboard != null)
      case lit: LiteralNode => addMenu.setEnabled(false)
        pasteNode.setEnabled(false)
      case reg: RegexNode => addMenu.setEnabled(false)
        pasteNode.setEnabled(false)
      case rs: RepSepNode =>
        multOne.setEnabled(false)
        multZeroOrOne.setEnabled(false)
        pasteNode.setEnabled(clipboard != null && childCount < 2)
        addMenu.setEnabled(childCount < 2)
      case ref: ReferenceNode => addMenu.setEnabled(false)
        pasteNode.setEnabled(false)
      case root: RootNode => addMenu.setEnabled(false)
        addMenu.setEnabled(childCount < 1)
        pasteNode.setEnabled(clipboard != null && childCount < 1)
      case _ => addMenu.setEnabled(false)
        pasteNode.setEnabled(false)
    }
  }

  val addMenu = new JMenu("Add")
  val newToken = new JMenuItem("Token")
  addMenu.add(newToken)
  newToken.addActionListener(ParserTreePanel)
  val newSequence = new JMenuItem("Sequence")
  addMenu.add(newSequence)
  newSequence.addActionListener(ParserTreePanel)
  val newRepSep = new JMenuItem("RepSep")
  addMenu.add(newRepSep)
  newRepSep.addActionListener(ParserTreePanel)
  val newChoice = new JMenuItem("Choice")
  addMenu.add(newChoice)
  newChoice.addActionListener(ParserTreePanel)
  val newReference = new JMenuItem("Parser")
  addMenu.add(newReference)
  newReference.addActionListener(ParserTreePanel)
  add(addMenu)
  //addSeparator()

  val multMenu = new JMenu("Multiplicity")
  val multOne = new JMenuItem("1 (just one)")
  val multZeroOrOne = new JMenuItem("? (0 or 1)")
  val multZeroOrMore = new JMenuItem("* (0 or more)")
  val multOneOrMore = new JMenuItem("+ (1 or more)")
  val multZero = new JMenuItem("0 (not there)")
  multMenu.add(multOne)
  multOne.addActionListener(ParserTreePanel)
  multZeroOrOne.addActionListener(ParserTreePanel)
  multZeroOrMore.addActionListener(ParserTreePanel)
  multOneOrMore.addActionListener(ParserTreePanel)
  multZero.addActionListener(ParserTreePanel)
  multMenu.add(multZeroOrOne)
  multMenu.add(multZeroOrMore)
  multMenu.add(multOneOrMore)
  multMenu.addSeparator()
  multMenu.add(multZero)
  add(multMenu)
  //addSeparator()

  val editMenu = new JMenu("Edit")
  add(editMenu)
  val moveUp = new JMenuItem("Move Up")
  editMenu.add(moveUp)
  moveUp.addActionListener(ParserTreePanel)
  val moveDown = new JMenuItem("Move Down")
  editMenu.add(moveDown)
  moveDown.addActionListener(ParserTreePanel)
  editMenu.addSeparator()

  val cutNode = new JMenuItem("Cut")
  editMenu.add(cutNode)
  cutNode.addActionListener(ParserTreePanel)
  val copyNode = new JMenuItem("Copy")
  editMenu.add(copyNode)
  copyNode.addActionListener(ParserTreePanel)
  val pasteNode = new JMenuItem("Paste")
  editMenu.add(pasteNode)
  pasteNode.addActionListener(ParserTreePanel)
  val deleteNode = new JMenuItem("Delete")
  editMenu.add(deleteNode)
  deleteNode.addActionListener(ParserTreePanel)

  val errorMsg = new JMenuItem("Error message")
  add(errorMsg)
  errorMsg.addActionListener(ParserTreePanel)
  addSeparator()

  val traceNode = new JCheckBoxMenuItem("Trace")
  add(traceNode)
  traceNode.addActionListener(ParserTreePanel)

  val typeNode = new JMenu("Type")
  add(typeNode)
  val type1Node = new JMenuItem("1-deep")
  typeNode.add(type1Node)
  type1Node.addActionListener(ParserTreePanel)
//  val type2Node = new JMenuItem("2-deep")
//  typeNode.add(type2Node)
//  type2Node.addActionListener(ParserTreePanel)
  val type3Node = new JMenuItem("3-deep")
  typeNode.add(type3Node)
  type3Node.addActionListener(ParserTreePanel)
  val typeNNode = new JMenuItem("n-deep")
  typeNode.add(typeNNode)
  typeNNode.addActionListener(ParserTreePanel)
}
