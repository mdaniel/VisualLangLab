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

import javax.swing.{JTree}
import javax.swing.tree.{DefaultTreeCellRenderer}
import java.awt.event.MouseEvent
import java.awt.{Component}

object ElementRenderer extends DefaultTreeCellRenderer {
  override def getTreeCellRendererComponent(tree: JTree, value: Object, selected: Boolean, 
      expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean): Component = {
    theIcon = new ElementIcon( value.asInstanceOf[ParserTreeNode], selected )
    value match {
      case l: LiteralNode => toolTipText = TokenBank(l.literalName).left.get
      case r: RegexNode => toolTipText = TokenBank(r.regexName).right.get
      case s: SequenceNode =>
        toolTipText = if(s.getChildCount < 2) "Must have 2 or more child nodes" else ""
      case rn: RootNode =>
        toolTipText = if(rn.getChildCount < 1) "Must have 1 child node" else ""
      case c: ChoiceNode =>
        toolTipText = if(c.getChildCount < 2) "Must have 2 or more child nodes" else ""
      case rs: RepSepNode =>
        toolTipText = if(rs.getChildCount < 2) "Must have 2 child nodes" else ""
      case _ => toolTipText = "???"
    }
    super.getTreeCellRendererComponent( tree, value, selected, expanded, leaf, row, hasFocus )
  }
  override def getToolTipText(me: MouseEvent): String = toolTipText
  override def getLeafIcon = theIcon
  override def getOpenIcon = theIcon
  override def getClosedIcon = theIcon
  var theIcon: ElementIcon = null
  var toolTipText = ""
}
