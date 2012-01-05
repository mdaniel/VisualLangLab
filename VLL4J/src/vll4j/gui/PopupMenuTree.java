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

package vll4j.gui;

import vll4j.tree.NodeBase;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;

public class PopupMenuTree extends JPopupMenu {
    
    PopupMenuTree(PanelRuleTree treePanel) {
        this.treePanel = treePanel;
        createPopup();
    }
    
    private void createPopup() {
        ButtonGroup bg = new ButtonGroup();
        multiplicityOneItem = new JRadioButtonMenuItem(treePanel.multiplicityOneAction);
        bg.add(multiplicityOneItem);
        multiplicityZeroMoreItem = new JRadioButtonMenuItem(treePanel.multiplicityZeroMoreAction);
        bg.add(multiplicityZeroMoreItem);
        multiplicityOneMoreItem = new JRadioButtonMenuItem(treePanel.multiplicityOneMoreAction);
        bg.add(multiplicityOneMoreItem);
        multiplicityZeroOneItem = new JRadioButtonMenuItem(treePanel.multiplicityZeroOneAction);
        bg.add(multiplicityZeroOneItem);
        multiplicityNotItem = new JRadioButtonMenuItem(treePanel.multiplicityNotAction);
        bg.add(multiplicityNotItem);
        multiplicityGuardItem = new JRadioButtonMenuItem(treePanel.multiplicityGuardAction);
        bg.add(multiplicityGuardItem);
        goToItem = new JMenuItem(treePanel.goToAction);
        add(goToItem);
        add(new JSeparator());
        add(addMenu);
        addMenu.add(treePanel.addTokenAction);
        addMenu.add(treePanel.addReferenceAction);
        addMenu.add(new JSeparator());
        addMenu.add(treePanel.addChoiceAction);
        addMenu.add(treePanel.addSequenceAction);
        addMenu.add(treePanel.addRepSepAction);
        addMenu.add(new JSeparator());
        addMenu.add(treePanel.addSemPredAction);
        add(multiplicityMenu);
        multiplicityMenu.add(multiplicityOneItem);
        multiplicityMenu.add(multiplicityZeroMoreItem);
        multiplicityMenu.add(multiplicityOneMoreItem);
        multiplicityMenu.add(multiplicityZeroOneItem);
        multiplicityMenu.add(new JSeparator());
        multiplicityMenu.add(multiplicityNotItem);
        multiplicityMenu.add(multiplicityGuardItem);
        add(editMenu);
        cutMenuItem = new JMenuItem(treePanel.cutAction);
        editMenu.add(cutMenuItem);
        copyMenuItem = new JMenuItem(treePanel.copyAction);
        editMenu.add(copyMenuItem);
        pasteMenuItem = new JMenuItem(treePanel.pasteAction);
        editMenu.add(pasteMenuItem);
        deleteMenuItem = new JMenuItem(treePanel.deleteAction);
        editMenu.add(deleteMenuItem);
        add(new JSeparator());
        dropMenuItem = new JCheckBoxMenuItem(treePanel.dropAction);
        add(dropMenuItem);
        add(commitMenu);
        packratMenuItem = new JCheckBoxMenuItem(treePanel.packratAction);
        add(packratMenuItem);
        errorMessageItem = new JMenuItem(treePanel.errorMessageAction);
        add(errorMessageItem);
        descriptionMenuItem = new JMenuItem(treePanel.descriptionAction);
        add(descriptionMenuItem);
        add(new JSeparator());
        traceMenuItem = new JCheckBoxMenuItem(treePanel.traceAction);
        add(traceMenuItem);
    }
    
    void adjustMenu() {
        NodeBase selectedNode = treePanel.selectedNode;
        selectedNode.accept(visitorAdjustMenu);
    }
    
    PanelRuleTree treePanel;
    VisitorMenuAdjustment visitorAdjustMenu = new VisitorMenuAdjustment(this);
    JMenuItem goToItem;
    JMenu addMenu = new JMenu("Add");
    JMenu multiplicityMenu = new JMenu("Multiplicity");
    JRadioButtonMenuItem multiplicityOneItem = null;
    JRadioButtonMenuItem multiplicityZeroMoreItem = null;
    JRadioButtonMenuItem multiplicityOneMoreItem = null;
    JRadioButtonMenuItem multiplicityZeroOneItem = null;
    JRadioButtonMenuItem multiplicityNotItem = null;
    JRadioButtonMenuItem multiplicityGuardItem = null;
    JMenu editMenu = new JMenu("Edit");
    JMenuItem cutMenuItem;
    JMenuItem copyMenuItem;
    JMenuItem pasteMenuItem;
    JMenuItem deleteMenuItem;
    JMenuItem dropMenuItem;
    JMenuItem commitMenu = new JCheckBoxMenuItem("Commit");
    JMenuItem packratMenuItem;
    JMenuItem errorMessageItem;
    JMenuItem descriptionMenuItem;
    JMenuItem traceMenuItem;
    
}