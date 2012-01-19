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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import net.java.vll.vll4j.api.NodeBase;

public class PanelAstDisplay extends JPanel {
    
    PanelAstDisplay(Vll4jGui gui) {
        this.gui = gui;
        visitorAstGeneration = new VisitorAstDescription(gui);
        setLayout(new BorderLayout());
        add(new JLabel("Parse Tree (AST) Structure", SwingConstants.CENTER), BorderLayout.NORTH);
        astArea.setEditable(false);
        astArea.setFont(new Font(Font.MONOSPACED, astArea.getFont().getStyle(), astArea.getFont().getSize()));
        add(new JScrollPane(astArea), BorderLayout.CENTER);
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new GridLayout(1, 6));
        ButtonGroup bg = new ButtonGroup();
        btnPanel.add(btnOne);
        bg.add(btnOne);
        btnPanel.add(btnThree);
        bg.add(btnThree);
        btnPanel.add(btnRule);
        bg.add(btnRule);
        btnRule.setSelected(true);
        btnPanel.add(btnFull);
        bg.add(btnFull);
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BorderLayout());
        helpButton = new JButton(gui.theHelpFunctionsManager.displayHelpAST) {
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
        southPanel.add(helpButton, BorderLayout.EAST);
        southPanel.add(btnPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
        setButtonsActive(true);
    }
    
    private void drawAst() {
        if (myThread != null)
            return;
        visitorAstGeneration.level = 0;
        myThread = new Thread() {   
            public void run() {
                NodeBase node = gui.theTreePanel.selectedNode;
                if (btnOne.isSelected()) {
                    visitorAstGeneration.maxDepth = 1;
                } else if (btnThree.isSelected()) {
                    visitorAstGeneration.maxDepth = 3;
                } else  if (btnRule.isSelected()) {
                    visitorAstGeneration.maxDepth = Integer.MAX_VALUE - 1;
                } else {
                    visitorAstGeneration.maxDepth = Integer.MAX_VALUE;
                }
                try {
                    String ast = (String)node.accept(visitorAstGeneration);
                    astArea.setText(ast);
                } catch (Throwable ex) {
                    JOptionPane.showMessageDialog(gui, ex, "ERROR - AST generation", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
                myThread = null;
            }
        };
        myThread.start();
    }
    
    void resetView() {
        setButtonsActive(false);
        btnRule.setSelected(true);
        setButtonsActive(true);
        drawAst();
    }
    
    private void setButtonsActive(boolean active) {
        if (active) {
            btnOne.addActionListener(buttonListener);
            btnThree.addActionListener(buttonListener);
            btnRule.addActionListener(buttonListener);
            btnFull.addActionListener(buttonListener);
        } else {
            btnOne.removeActionListener(buttonListener);
            btnThree.removeActionListener(buttonListener);
            btnRule.removeActionListener(buttonListener);
            btnFull.removeActionListener(buttonListener);
        }
    }
    
    private ActionListener buttonListener = new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
            Object src = evt.getSource();
            if (src == btnFull) {
                if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(gui, 
                        "Need full-depth AST?\n(may cause memory errors)", 
                        "CONFIRM - AST depth", JOptionPane.OK_CANCEL_OPTION)) {
                    setButtonsActive(false);
                    btnRule.setSelected(true);
                    setButtonsActive(true);
                    return;
                }
            }
            drawAst();
        }
    };
    
    private JTextArea astArea = new JTextArea();
    private JRadioButton btnOne = new JRadioButton("1");
    private JRadioButton btnThree = new JRadioButton("3");
    private JRadioButton btnRule = new JRadioButton("Rule");
    private JRadioButton btnFull = new JRadioButton("Full");
    private JButton helpButton = null;
    private VisitorAstDescription visitorAstGeneration;
    private Vll4jGui gui;
    private Thread myThread = null;
}
