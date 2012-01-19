/*
 Copyright 2012, Sanjay Dasgupta
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

package net.java.vll.vll4j.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.tree.TreePath;
import net.java.vll.vll4j.api.NodeBase;

public class PopupListenerTree extends MouseAdapter {

    PopupListenerTree(PanelRuleTree treePanel) {
        this.treePanel = treePanel;
    }

    @Override
    public void mousePressed(MouseEvent me) {
        popup(me);
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        popup(me);
    }

    void popup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            TreePath path = treePanel.theTree.getPathForLocation(e.getX(), e.getY());
            if (path != null) {
                treePanel.theTree.setSelectionPath(path);
                treePanel.selectedNode = (NodeBase)path.getLastPathComponent();
                treePanel.treePopupMenu.adjustMenu();
                treePanel.treePopupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
    
    PanelRuleTree treePanel;
}
