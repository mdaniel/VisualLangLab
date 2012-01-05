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

import vll4j.tree.VisitorValidation;
import vll4j.tree.NodeBase;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import vll4j.tree.NodeChoice;
import vll4j.tree.NodeLiteral;
import vll4j.tree.NodeReference;
import vll4j.tree.NodeRegex;
import vll4j.tree.NodeRepSep;
import vll4j.tree.NodeRoot;
import vll4j.tree.NodeSemPred;
import vll4j.tree.NodeSequence;

public class RendererRuleNode extends DefaultTreeCellRenderer {
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
        boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        theNode = (NodeBase)value;
        JLabel c =  (JLabel)super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        c.setText(theNode.getName());
        validationString = (String)theNode.accept(visitorNodeValidation);
        c.setToolTipText(validationString);
        return c;
    }
    @Override
    public Icon getLeafIcon() {
        return getNodeIcon();
    }
    @Override
    public Icon getOpenIcon() {
        return getNodeIcon();
    }
    @Override
    public Icon getClosedIcon() {
        return getNodeIcon();
    }
    private Icon getNodeIcon() {
        if (theNode instanceof NodeChoice) {
            return Resources.choice;
        } else if (theNode instanceof NodeLiteral) {
            return Resources.literal;
        } else if (theNode instanceof NodeReference) {
            return Resources.reference;
        } else if (theNode instanceof NodeRegex) {
            return Resources.regex;
        } else if (theNode instanceof NodeRepSep) {
            return Resources.repSep;
        } else if (theNode instanceof NodeRoot) {
            return Resources.root;
        } else if (theNode instanceof NodeSemPred) {
            return Resources.semPred;
        } else if (theNode instanceof NodeSequence) {
            return Resources.sequence;
        } else 
            return null;
    }
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        validationString = (String)theNode.accept(visitorNodeValidation);
        setToolTipText(validationString);
        if (validationString != null) {
            g.drawImage(Resources.errorMark.getImage(), 0, 0, null);
        }
    }
    
    NodeBase theNode;
    private VisitorValidation visitorNodeValidation = new VisitorValidation();
    private String validationString;
}