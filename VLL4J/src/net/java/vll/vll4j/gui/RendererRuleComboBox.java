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
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import net.java.vll.vll4j.api.NodeRoot;

public class RendererRuleComboBox extends BasicComboBoxRenderer {
    
    public RendererRuleComboBox(Vll4jGui gui) {
        this.gui = gui;
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList list,
        Object value, int index, boolean isSelected, boolean cellHasFocus) {
        ruleName = value.toString();
        rootNode = (NodeRoot)gui.theForest.ruleBank.get(ruleName);
        setText(ruleName);
        setIcon(myIcon);
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        visitorRuleInfo.visitRoot(rootNode);
        if (visitorRuleInfo.hasErrors) {
            g.setColor(Color.red);
            g.drawLine(1, 1, 5, 1);
            g.drawLine(1, 1, 1, 6);
            g.drawLine(1, 6, 5, 6);
            g.drawLine(1, 3, 3, 3);
        }
        if (visitorRuleInfo.hasActions) {
            g.setColor(Color.black);
            g.drawLine(9, 6, 12, 1);
            g.drawLine(12, 1, 14, 6);
            g.drawLine(11, 4, 14, 4);
        }
    }
    
    private Vll4jGui gui;
    private NodeRoot rootNode;
    private Icon myIcon = Resources.rule;
    String ruleName = "";
    private VisitorRuleInfo visitorRuleInfo = new VisitorRuleInfo();
 }

