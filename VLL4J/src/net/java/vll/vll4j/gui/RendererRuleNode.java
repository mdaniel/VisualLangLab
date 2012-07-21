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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import net.java.vll.vll4j.api.*;

public class RendererRuleNode extends DefaultTreeCellRenderer {
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
        boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        theNode = (NodeBase)value;
        isDropped = theNode.isDropped;
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
            String litName = ((NodeLiteral)theNode).literalName;
            return litName.endsWith("_") ? Resources.literalLocal : Resources.literal;
        } else if (theNode instanceof NodeReference) {
            return Resources.reference;
        } else if (theNode instanceof NodeRegex) {
            String regName = ((NodeRegex)theNode).regexName;
            return regName.endsWith("_") ? Resources.regexLocal : Resources.regex;
        } else if (theNode instanceof NodeRepSep) {
            return Resources.repSep;
        } else if (theNode instanceof NodeRoot) {
            return Resources.root;
        } else if (theNode instanceof NodeSemPred) {
            return Resources.semPred;
        } else if (theNode instanceof NodeSequence) {
            return Resources.sequence;
        } else if (theNode instanceof NodeWildCard) {
            return Resources.wildCard;
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
        if (isDropped) {
            g.setColor(Color.black);
            g.drawLine(0, getHeight(), getHeight(), 0);
            g.setColor(Color.white);
            g.drawLine(0, getHeight() + 1, getHeight(), 1);
            g.drawLine(0, getHeight() - 1, getHeight(), -1);
        }
    }
    
    NodeBase theNode;
    private VisitorValidation visitorNodeValidation = new VisitorValidation();
    private String validationString;
    private boolean isDropped;
}
