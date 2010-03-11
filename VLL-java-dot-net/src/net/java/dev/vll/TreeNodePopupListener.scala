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

import java.awt.event.{MouseAdapter, MouseEvent}
import javax.swing.JPopupMenu

class TreeNodePopupListener(val popup: JPopupMenu) extends MouseAdapter {

  override def mousePressed(e: MouseEvent) {
    maybeShowPopup(e);
  }

  override def mouseReleased(e: MouseEvent) {
    maybeShowPopup(e);
  }

  private def maybeShowPopup(e: MouseEvent) {
    if (e.isPopupTrigger()) {
      val path = ParserTreePanel.theTree.getPathForLocation(e.getX(), e.getY())
      if (path != null) {
        //RuleTreePanel.theTree.addSelectionPath(path)
        ParserTreePanel.theTree.setSelectionPath(path)
        //RuleTreePanel.theTree.scrollPathToVisible(path)
        //val node = path.getLastPathComponent.asInstanceOf[RuleTreeNode]
        ParserTreePanel.selectedNode = path.getLastPathComponent.asInstanceOf[ParserTreeNode]
        TreeNodePopupMenu.adjustMenu()
        TreeNodePopupMenu.show(e.getComponent(), e.getX() + VisualLangLab.treeWidth / 10, e.getY());
      }
    }
  }
}
