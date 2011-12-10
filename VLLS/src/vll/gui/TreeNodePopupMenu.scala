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

package vll.gui

import javax.swing.{JPopupMenu, JMenuItem, JMenu, JRadioButtonMenuItem, JCheckBoxMenuItem, ButtonGroup}
import vll.core.ChoiceNode
import vll.core.LiteralNode
import vll.core.ReferenceNode
import vll.core.RegexNode
import vll.core.RepSepNode
import vll.core.RootNode
import vll.core.SequenceNode
import vll.core.PredicateNode
import vll.core.WildCardNode

class TreeNodePopupMenu(val ruleTreePanel: RuleTreePanel) extends JPopupMenu {

  def adjustMenu() {
    val selectedNode = ruleTreePanel.selectedNode
    val parentNode = selectedNode.getParent
    val isRootNode = parentNode eq null
    val clipboard = ruleTreePanel.clipboard
    val parentIndex = if (isRootNode) -1 else parentNode.getIndex(selectedNode)
    val parentChildCount = if (isRootNode) -1 else parentNode.getChildCount
    val childCount = selectedNode.getChildCount

    traceNode.setEnabled(parentNode ne null)
    traceNode.setSelected(selectedNode.trace)
    packratCheck.setEnabled(isRootNode)
    packratCheck.setSelected(selectedNode.isPackrat)
    cutNode.setEnabled(!isRootNode)
    copyNode.setEnabled(!isRootNode)
    deleteNode.setEnabled(!isRootNode)
    multMenu.setEnabled(!isRootNode && !parentNode.pNode.isInstanceOf[RepSepNode])

    errorMsg.setEnabled(!isRootNode)
    errorMsg.setSelected(errorMsg.isEnabled && selectedNode.errorMessage == "")

    val mv = Array(multOne, multZeroOrOne, multZeroOrMore, multOneOrMore, multNot, multGuard)
    mv(selectedNode.multiplicity.id).setSelected(true)
    multOne.setEnabled(!selectedNode.pNode.isInstanceOf[RepSepNode])
    multZeroOrOne.setEnabled(!selectedNode.pNode.isInstanceOf[RepSepNode])
    multNot.setEnabled((parentNode ne null) && parentNode.pNode.isInstanceOf[SequenceNode])
    multGuard.setEnabled((parentNode ne null) && parentNode.pNode.isInstanceOf[SequenceNode])

    dropCheck.setEnabled(!isRootNode && parentNode.pNode.isInstanceOf[SequenceNode])
    dropCheck.setSelected(dropCheck.isEnabled && selectedNode.drop)

    gotoMenuItem.setEnabled(selectedNode.pNode.isInstanceOf[ReferenceNode] ||
        selectedNode.pNode.isInstanceOf[RegexNode] ||
        selectedNode.pNode.isInstanceOf[LiteralNode])
    selectedNode.pNode match {
      case cho: ChoiceNode => addMenu.setEnabled(true)
        pasteNode.setEnabled(clipboard ne null)
      case seq: SequenceNode => addMenu.setEnabled(true)
        pasteNode.setEnabled(clipboard ne null)
      case lit: LiteralNode => addMenu.setEnabled(false)
        pasteNode.setEnabled(false)
      case reg: RegexNode => addMenu.setEnabled(false)
        pasteNode.setEnabled(false)
      case rs: RepSepNode =>
        pasteNode.setEnabled((clipboard ne null) && childCount < 2)
        addMenu.setEnabled(childCount < 2)
      case ref: ReferenceNode => addMenu.setEnabled(false)
        pasteNode.setEnabled(false)
      case root: RootNode => addMenu.setEnabled(root.isEmpty)
        pasteNode.setEnabled((clipboard ne null) && root.isEmpty)
      case root: PredicateNode => addMenu.setEnabled(false)
        pasteNode.setEnabled(false)
      case w: WildCardNode => addMenu.setEnabled(false)
        pasteNode.setEnabled(false)
    }
    commitPoint.setEnabled(!isRootNode && parentNode.pNode.isInstanceOf[SequenceNode])
    commitPoint.setSelected(commitPoint.isEnabled &&
        parentNode.pNode.asInstanceOf[SequenceNode].commitPoint == parentIndex)
    newPredicate.setEnabled(selectedNode.pNode.isInstanceOf[SequenceNode])
  }

  val gotoMenuItem = new JMenuItem("Go to")
  add(gotoMenuItem)
  gotoMenuItem.addActionListener(ruleTreePanel)
  addSeparator()

  val addMenu = new JMenu("Add")
  val newToken = new JMenuItem("Token", Images.token)
  addMenu.add(newToken)
  newToken.addActionListener(ruleTreePanel)
  val newReference = new JMenuItem("Reference", Images.reference)
  addMenu.add(newReference)
  newReference.addActionListener(ruleTreePanel)
  val newSequence = new JMenuItem("Sequence", Images.sequence)
  addMenu.add(newSequence)
  newSequence.addActionListener(ruleTreePanel)
  val newRepSep = new JMenuItem("RepSep", Images.repSep)
  addMenu.add(newRepSep)
  newRepSep.addActionListener(ruleTreePanel)
  val newChoice = new JMenuItem("Choice", Images.choice)
  addMenu.add(newChoice)
  newChoice.addActionListener(ruleTreePanel)
  val newPredicate = new JMenuItem("Predicate", Images.predicate)
  addMenu.add(newPredicate)
  newPredicate.addActionListener(ruleTreePanel)
  val newWildCard = new JMenuItem("WildCard", Images.wildCard)
  addMenu.add(newWildCard)
  newWildCard.addActionListener(ruleTreePanel)
  add(addMenu)
  //addSeparator()

  val multMenu = new JMenu("Multiplicity")
  val multOne = new JRadioButtonMenuItem("1 (just one)", true)
  val multZeroOrOne = new JRadioButtonMenuItem("? (0 or 1)")
  val multZeroOrMore = new JRadioButtonMenuItem("* (0 or more)")
  val multOneOrMore = new JRadioButtonMenuItem("+ (1 or more)")
  val multNot = new JRadioButtonMenuItem("0 (not)")
  val multGuard = new JRadioButtonMenuItem("= (guard)")
  val bg = new ButtonGroup
  bg.add(multOne)
  bg.add(multZeroOrOne)
  bg.add(multZeroOrMore)
  bg.add(multOneOrMore)
  bg.add(multNot)
  bg.add(multGuard)
  multMenu.add(multOne)
  multOne.addActionListener(ruleTreePanel)
  multZeroOrOne.addActionListener(ruleTreePanel)
  multZeroOrMore.addActionListener(ruleTreePanel)
  multOneOrMore.addActionListener(ruleTreePanel)
  multNot.addActionListener(ruleTreePanel)
  multGuard.addActionListener(ruleTreePanel)
  multMenu.add(multZeroOrOne)
  multMenu.add(multZeroOrMore)
  multMenu.add(multOneOrMore)
  multMenu.addSeparator()
  multMenu.add(multNot)
  multMenu.add(multGuard)
  add(multMenu)
  //addSeparator()

  val editMenu = new JMenu("Edit")
  add(editMenu)
  val cutNode = new JMenuItem("Cut", Images.cut16)
  editMenu.add(cutNode)
  cutNode.addActionListener(ruleTreePanel)
  val copyNode = new JMenuItem("Copy", Images.copy16)
  editMenu.add(copyNode)
  copyNode.addActionListener(ruleTreePanel)
  val pasteNode = new JMenuItem("Paste", Images.paste16)
  editMenu.add(pasteNode)
  pasteNode.addActionListener(ruleTreePanel)
  val deleteNode = new JMenuItem("Delete", Images.delete16)
  editMenu.add(deleteNode)
  deleteNode.addActionListener(ruleTreePanel)
  addSeparator()

  val dropCheck = new JCheckBoxMenuItem("Drop")
  add(dropCheck)
  dropCheck.addActionListener(ruleTreePanel)

  val commitPoint = new JCheckBoxMenuItem("Commit")
  add(commitPoint)
  commitPoint.addActionListener(ruleTreePanel)

  val packratCheck = new JCheckBoxMenuItem("Packrat")
  add(packratCheck)
  packratCheck.addActionListener(ruleTreePanel)

  val errorMsg = new JMenuItem("Error message")
  add(errorMsg)
  errorMsg.addActionListener(ruleTreePanel)

  val description = new JMenuItem("Description")
  add(description)
  description.addActionListener(ruleTreePanel)
  addSeparator()

  val traceNode = new JCheckBoxMenuItem("Trace")
  add(traceNode)
  traceNode.addActionListener(ruleTreePanel)

/*   val colorMenu = new JMenu("Color")
  add(colorMenu)
  val redNode = new JMenuItem("<html><font \"color=red\">Red</font></html>")
  colorMenu.add(redNode)
  redNode.addActionListener(parserTreePanel)
  val greenNode = new JMenuItem("<html><font \"color=green\">Green</font></html>")
  colorMenu.add(greenNode)
  greenNode.addActionListener(parserTreePanel)
  val blueNode = new JMenuItem("<html><font \"color=blue\">Blue</font></html>")
  colorMenu.add(blueNode)
  blueNode.addActionListener(parserTreePanel)
  val cyanNode = new JMenuItem("<html><font \"color=#00ffff\">Cyan</font></html>")
  colorMenu.add(cyanNode)
  cyanNode.addActionListener(parserTreePanel)
  val magentaNode = new JMenuItem("<html><font \"color=#ff00ff\">Magenta</font></html>")
  colorMenu.add(magentaNode)
  magentaNode.addActionListener(parserTreePanel)
  val orangeNode = new JMenuItem("<html><font \"color=#ffc800\">Orange</font></html>")
  colorMenu.add(orangeNode)
  orangeNode.addActionListener(parserTreePanel)
  val blackNode = new JMenuItem("<html><font \"color=black\">Black</font></html>")
  colorMenu.add(blackNode)
  blackNode.addActionListener(parserTreePanel)
 */
}
