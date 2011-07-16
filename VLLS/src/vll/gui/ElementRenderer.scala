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

import javax.swing.{JTree}
import javax.swing.tree.{DefaultTreeCellRenderer}
import java.awt.event.MouseEvent
import java.awt.{Component, Font, Color, Graphics}
import scala.swing.Swing
import vll.core.{ LiteralNode, SequenceNode, Multiplicity, RegexNode, VllParsers, RootNode }

class ElementRenderer(val gui: VllGui, val hub: VllParsers) extends DefaultTreeCellRenderer {
  override def getTreeCellRendererComponent(tree: JTree, value: Object, selected: Boolean, 
    expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component = {
    var renderer: Component = null
    try {
      guiNode = value.asInstanceOf[GuiNode]
      isRoot = guiNode.pNode.isInstanceOf[RootNode]
      parserValid = guiNode.isValid
      toolTipText = guiNode.pNode match {
        case lit: LiteralNode => hub.tokenBank(lit.literalName).left.get
        case reg: RegexNode => hub.tokenBank(reg.regexName).right.get
        case _ => guiNode.isValidMessage
      }
      displayName = guiNode.displayName(gui.viewFullNamesMenuItem.selected)
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
    if (!parserValid) {
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
    msg
  }
  override def getForeground() = color //if (parserValid) Color.black else Color.red
  var guiNode: GuiNode = null
  var dropped = false
  var theIcon: ElementIcon = null
  var toolTipText = ""
  var theFont: Option[Font] = None
  var displayName = ""
  var theAnnotations = ""
  var multiplicity = Multiplicity.One
  var parserValid = true
  var isCommitPoint = false
  var color = Color.black
  var isRoot = false
  val errorImage = Swing.Icon(getClass.getResource("images/ErrorMark.gif")).getImage
  val commitImage = Swing.Icon(getClass.getResource("images/CommitMark.gif")).getImage
}