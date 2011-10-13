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

import javax.swing.{JTree}
import javax.swing.tree.{DefaultTreeCellRenderer}
import java.awt.event.MouseEvent
import java.awt.{Component, Font, Color, Graphics}
import scala.swing.Swing
import vll.core.{ LiteralNode, SequenceNode, Multiplicity, RegexNode, VllParsers, RootNode , ChoiceNode, RepSepNode, PredicateNode, ReferenceNode}

class ElementRenderer(val gui: VllGui, val hub: VllParsers) extends DefaultTreeCellRenderer {
  override def getTreeCellRendererComponent(tree: JTree, value: Object, selected: Boolean, 
    expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component = {
    var renderer: Component = null
    try {
      guiNode = value.asInstanceOf[GuiNode]
      isRoot = guiNode.pNode.isInstanceOf[RootNode]
      ruleValid = guiNode.isValid
      toolTipText = guiNode.pNode match {
        case LiteralNode(_, name) => if (guiNode.isValidMessage eq null)
          "Literal('%s')".format(hub.tokenBank(name).left.get) else guiNode.isValidMessage
        case RegexNode(_, name) => if (guiNode.isValidMessage eq null)
          "Regex('%s')".format(hub.tokenBank(name).right.get) else guiNode.isValidMessage
        case SequenceNode(_) => if (guiNode.isValidMessage eq null)
          "Sequence" else guiNode.isValidMessage
        case ChoiceNode(_) => if (guiNode.isValidMessage eq null)
          "Choice" else guiNode.isValidMessage
         case RepSepNode(m) => if (guiNode.isValidMessage eq null)
          (if (m == Multiplicity.ZeroOrMore) "RepSep" else "Rep1Sep") else guiNode.isValidMessage
        case PredicateNode() => if (guiNode.isValidMessage eq null)
          "Predicate" else guiNode.isValidMessage
        case ReferenceNode(_, name) => if (guiNode.isValidMessage eq null)
          "Reference(%s)".format(name) else guiNode.isValidMessage
        case RootNode(name, _) => if (guiNode.isValidMessage eq null)
          "Root(%s)".format(name) else guiNode.isValidMessage
        case _ => guiNode.isValidMessage
      }
      displayName = guiNode.displayName(gui.viewFullNamesMenuItem.selected)
      description = guiNode.description
      theAnnotations = guiNode.nodeAnnotations(gui.viewShowEpsOkMenuItem.selected)
      multiplicity = guiNode.multiplicity
      theIcon = new ElementIcon(guiNode.pNode, selected)
      dropped = guiNode.drop
      color = guiNode.color
      isCommitPoint = (guiNode.pNode.parent ne null) && guiNode.pNode.parent.isInstanceOf[SequenceNode] &&
      (guiNode.pNode.parent.asInstanceOf[SequenceNode].commitPoint == guiNode.pNode.parent.indexOf(guiNode.pNode))
      renderer = super.getTreeCellRendererComponent(tree, guiNode, selected, expanded, leaf, row, hasFocus)
      if (theFont == None) {
        val f = renderer.getFont
        theFont = Some(f)
        val newFont = new Font(Font.MONOSPACED, f.getStyle, f.getSize)
        //r.setFont(newFont)
      }
    } catch {
      case x => x.printStackTrace()
        System.out.flush()
    }
    renderer
  }
  override def paintComponent(g: Graphics) {
    def drawLeftBars(c: Color) {
      val h = getHeight; val w = getWidth
      g.setColor(c)
      g.drawLine(h, 2, h, h - 2)
      g.drawLine(h + 1, 2, h + 1, h - 2)
    }
    super.paintComponent(g)
    if (!ruleValid) {
      g.drawImage(errorImage, 0, 0, null)
    }
    if (dropped) {
      val h = getHeight
      g.setColor(Color.white)
      //g.drawLine(0, h - 3, h - 3 , 0)
      g.drawLine(0, h - 2, h - 2 , 0)
      g.drawLine(1, h - 1, h - 1 , 1)
      //g.drawLine(2, h - 1, h - 1 , 2)
      g.setColor(Color.black)
      g.drawLine(0, h - 1, h - 1 , 0)
    }
    if (multiplicity == Multiplicity.Not) {
      drawLeftBars(Color.red)
    } else if (multiplicity == Multiplicity.Guard) {
      drawLeftBars(Color.green.darker)
    }
    if (isCommitPoint) {
      g.drawImage(commitImage, 0, 0, null)
    }
  }
  override def getToolTipText(me: MouseEvent) = toolTipText
  override def getLeafIcon = theIcon
  override def getOpenIcon = theIcon
  override def getClosedIcon = theIcon
  override def getText(): String = {
    val msg = (displayName, theAnnotations) match {
      case ("", "") => if (isRoot)
        "" else "%s".format(multiplicity)
      case (d, "") => if (isRoot)
        "%s".format(d) else "%s %s".format(multiplicity, d)
      case ("", a) => if (isRoot)
        "[%s]".format(a) else "%s [%s]".format(multiplicity, a)
      case (d, a) => if (isRoot)
        "%s [%s]".format(d, a) else "%s %s [%s]".format(multiplicity, d, a)
    }
    if (guiNode eq null) "" else if(description.isEmpty) msg else 
      msg + " (%s)".format(if (description.length < 51) description else description.substring(0, 51))
  }
//  override def getForeground() = color //if (ruleValid) Color.black else Color.red
  var guiNode: GuiNode = null
  var dropped = false
  var theIcon: ElementIcon = null
  var toolTipText = ""
  var theFont: Option[Font] = None
  var displayName = ""
  var description = ""
  var theAnnotations = ""
  var multiplicity = Multiplicity.One
  var ruleValid = true
  var isCommitPoint = false
  var color = Color.black
  var isRoot = false
  val errorImage = Swing.Icon(getClass.getResource("images/ErrorMark.gif")).getImage
  val commitImage = Swing.Icon(getClass.getResource("images/CommitMark.gif")).getImage
}
