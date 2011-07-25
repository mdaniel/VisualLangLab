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

import java.awt.event.{MouseAdapter, MouseEvent}

class TreeNodePopupListener(val ruleTreePanel: RuleTreePanel) extends MouseAdapter {

  override def mousePressed(e: MouseEvent) {
try {
//  println("mousePressed()"); System.out.flush()
    maybeShowPopup(e);
} catch {
  case x => x.printStackTrace(); System.out.flush()
}
  }

  override def mouseReleased(e: MouseEvent) {
try {
//  println("mouseReleased()"); System.out.flush()
    maybeShowPopup(e);
} catch {
  case x => x.printStackTrace(); System.out.flush()
}
  }

  private def maybeShowPopup(e: MouseEvent) {
    if (e.isPopupTrigger()) {
      val path = ruleTreePanel.theTree.getPathForLocation(e.getX(), e.getY())
      if (path != null) {
        ruleTreePanel.theTree.setSelectionPath(path)
        ruleTreePanel.selectedNode = path.getLastPathComponent.asInstanceOf[GuiNode]
        ruleTreePanel.treeNodePopupMenu.adjustMenu()
        ruleTreePanel.treeNodePopupMenu.show(e.getComponent(), e.getX() + /* VllGui.top.treeWidth */
            ruleTreePanel.theTree.getWidth / 10, e.getY());
      }
    }
  }
}
