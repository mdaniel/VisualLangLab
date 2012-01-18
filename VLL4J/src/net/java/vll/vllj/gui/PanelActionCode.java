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

import net.java.vll.vllj.tree.ActionFunction;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import net.java.vll.vllj.tree.NodeRoot;

public class PanelActionCode extends JPanel {
    
    PanelActionCode(Vll4jGui gui) {
        this.gui = gui;
        setLayout(new BorderLayout());
        add(new JLabel("Action Code", SwingConstants.CENTER), BorderLayout.NORTH);
        codeArea.addKeyListener(keyAdapter);
        add(new JScrollPane(codeArea), BorderLayout.CENTER);
        saveButton.setEnabled(false);
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BorderLayout());
        btnPanel.add(saveButton, BorderLayout.CENTER);
        helpButton = new JButton(gui.theHelpFunctionsManager.displayHelpActionCode) {
            @Override
            public Dimension getPreferredSize() {
                int h = super.getPreferredSize().height;
                return new Dimension(h, h);
            }
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };
        btnPanel.add(helpButton, BorderLayout.EAST);
        add(btnPanel, BorderLayout.SOUTH);
        normalTextColor = codeArea.getForeground();
    }
    
    void resetView() {
        if (gui.theTreePanel.selectedNode instanceof NodeRoot) {
            codeArea.setEnabled(false);
            codeArea.setText("");
        } else {
            codeArea.setEnabled(true);
            String at = gui.theTreePanel.selectedNode.actionText;
            ActionFunction af = gui.theTreePanel.selectedNode.actionFunction;
            codeArea.setText(at);
            codeArea.setForeground(at.trim().isEmpty() == (af == null) ? normalTextColor : Color.red);
        }
        saveButton.setEnabled(false);
    }
    
    private Action saveAction = new AbstractAction("Save") {
        @Override
        public void actionPerformed(ActionEvent o) {
            gui.theTreePanel.selectedNode.actionText = codeArea.getText();
            saveButton.setEnabled(false);
            String msg = gui.theForest.compileActionCode(gui.theTreePanel.selectedNode);
            if (msg == null) {
                codeArea.setForeground(normalTextColor);
            } else {
                JOptionPane.showMessageDialog(gui, msg, "ERROR - Save action", JOptionPane.ERROR_MESSAGE);
                codeArea.setForeground(Color.red);
            }
            gui.theTreePanel.resetNodeDisplay(gui.theTreePanel.selectedNode);
        }
    };
    
    KeyAdapter keyAdapter = new KeyAdapter() {
        @Override
        public void keyTyped(KeyEvent e) {
            saveButton.setEnabled(true);
        }
    };
    
    TextAreaCustom codeArea = new TextAreaCustom();
    private Color normalTextColor;
    private JButton saveButton = new JButton(saveAction);
    private JButton helpButton = null;
    Vll4jGui gui;
}
