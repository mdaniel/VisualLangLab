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

package net.java.vll.vllj.gui;

import net.java.vll.vllj.api.*;

public class VisitorMenuAdjustment extends VisitorBase {
    
    public VisitorMenuAdjustment(PopupMenuTree theMenu) {
        this.theMenu = theMenu;
    }
    
    void commonSetting(NodeBase n) {
        NodeBase parent = (NodeBase)n.getParent();
        boolean setting;
        if (parent == null) {
            setting = false;
        } else {
            if (parent instanceof NodeSequence)
                setting = true;
            else
                setting = false;
        }
        theMenu.dropMenuItem.setEnabled(setting);
        theMenu.commitMenu.setEnabled(setting);
        theMenu.packratMenuItem.setEnabled(false);
        theMenu.multiplicityOneItem.setEnabled(true); 
        theMenu.multiplicityZeroOneItem.setEnabled(parent == null || !(parent instanceof NodeChoice)); 
        theMenu.multiplicityZeroMoreItem.setEnabled(parent == null || !(parent instanceof NodeChoice)); 
        theMenu.multiplicityOneMoreItem.setEnabled(true); 
        theMenu.multiplicityNotItem.setEnabled(parent == null || !(parent instanceof NodeChoice)); 
        theMenu.multiplicityGuardItem.setEnabled(parent == null || !(parent instanceof NodeChoice)); 
        theMenu.multiplicityMenu.setEnabled(true);
        theMenu.traceMenuItem.setSelected(n.isTraced);
        theMenu.dropMenuItem.setSelected(n.isDropped);
        theMenu.packratMenuItem.setSelected(false);
        theMenu.commitMenu.setSelected(false);
        if (n.multiplicity == Multiplicity.One) {
            theMenu.multiplicityOneItem.setSelected(true); 
        } else if (n.multiplicity == Multiplicity.ZeroOrOne) {
            theMenu.multiplicityZeroOneItem.setSelected(true); 
        } else if (n.multiplicity == Multiplicity.ZeroOrMore) {
            theMenu.multiplicityZeroMoreItem.setSelected(true); 
        } else if (n.multiplicity == Multiplicity.OneOrMore) {
            theMenu.multiplicityOneMoreItem.setSelected(true); 
        } else if (n.multiplicity == Multiplicity.Not) {
            theMenu.multiplicityNotItem.setSelected(true); 
        } else if (n.multiplicity == Multiplicity.Guard) {
            theMenu.multiplicityGuardItem.setSelected(true); 
        } 
        theMenu.cutMenuItem.setEnabled(true);
        theMenu.copyMenuItem.setEnabled(true);
        theMenu.deleteMenuItem.setEnabled(true);
        theMenu.treePanel.addSemPredAction.setEnabled(false);
    }
    
    @Override
    public Object visitChoice(NodeChoice n) {
        commonSetting(n);
        theMenu.addMenu.setEnabled(true);
        theMenu.pasteMenuItem.setEnabled(theMenu.treePanel.theClipBoard != null);
        theMenu.goToItem.setEnabled(false);
        return null;
    }
    
    @Override
    public Object visitLiteral(NodeLiteral n) {
        commonSetting(n);
        theMenu.addMenu.setEnabled(false);
        theMenu.pasteMenuItem.setEnabled(false);
        theMenu.goToItem.setEnabled(true);
        return null;
    }
    
    @Override
    public Object visitReference(NodeReference n) {
        commonSetting(n);
        theMenu.addMenu.setEnabled(false);
        theMenu.pasteMenuItem.setEnabled(false);
        theMenu.goToItem.setEnabled(true);
        return null;
    }
    
    @Override
    public Object visitRegex(NodeRegex n) {
        commonSetting(n);
        theMenu.addMenu.setEnabled(false);
        theMenu.pasteMenuItem.setEnabled(false);
        theMenu.goToItem.setEnabled(true);
        return null;
    }
    
    @Override
    public Object visitRepSep(NodeRepSep n) {
        commonSetting(n);
        theMenu.addMenu.setEnabled(n.getChildCount() < 2);
        theMenu.pasteMenuItem.setEnabled(n.getChildCount() < 2 && theMenu.treePanel.theClipBoard != null);
        theMenu.multiplicityOneItem.setEnabled(false); 
        theMenu.multiplicityZeroOneItem.setEnabled(false); 
        theMenu.multiplicityNotItem.setEnabled(false); 
        theMenu.multiplicityGuardItem.setEnabled(false); 
        theMenu.goToItem.setEnabled(false);
        return null;
    }
    
    @Override
    public Object visitRoot(NodeRoot n) {
        commonSetting(n);
        theMenu.packratMenuItem.setEnabled(true);
        theMenu.packratMenuItem.setSelected(n.isPackrat);
        theMenu.addMenu.setEnabled(n.getChildCount() == 0);
        theMenu.multiplicityMenu.setEnabled(false);
        theMenu.goToItem.setEnabled(false);
        theMenu.pasteMenuItem.setEnabled(n.getChildCount() == 0 && theMenu.treePanel.theClipBoard != null);
        theMenu.cutMenuItem.setEnabled(false);
        theMenu.copyMenuItem.setEnabled(false);
        theMenu.deleteMenuItem.setEnabled(false);
        return null;
    }
    
    @Override
    public Object visitSemPred(NodeSemPred n) {
        commonSetting(n);
        theMenu.addMenu.setEnabled(false);
        theMenu.pasteMenuItem.setEnabled(false);
        theMenu.goToItem.setEnabled(true);
        theMenu.multiplicityMenu.setEnabled(false);
        return null;
    }
    
    @Override
    public Object visitSequence(NodeSequence n) {
        commonSetting(n);
        theMenu.addMenu.setEnabled(true);
        theMenu.goToItem.setEnabled(false);
        theMenu.pasteMenuItem.setEnabled(theMenu.treePanel.theClipBoard != null);
        theMenu.treePanel.addSemPredAction.setEnabled(true);
        return null;
    }
    
    @Override
    public Object visitWildCard(NodeWildCard n) {
        commonSetting(n);
        theMenu.addMenu.setEnabled(false);
        theMenu.goToItem.setEnabled(false);
        theMenu.pasteMenuItem.setEnabled(false);
        theMenu.treePanel.addSemPredAction.setEnabled(false);
        return null;
    }
    
    PopupMenuTree theMenu;
}
