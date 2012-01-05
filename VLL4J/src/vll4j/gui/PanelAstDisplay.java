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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

public class PanelAstDisplay extends JPanel {
    
    PanelAstDisplay(Vll4jGui gui) {
        this.gui = gui;
        visitorAstGeneration = new VisitorAstGeneration(gui);
        setLayout(new BorderLayout());
        add(new JLabel("Parse Tree (AST) Structure", SwingConstants.CENTER), BorderLayout.NORTH);
        astArea.setEditable(false);
        astArea.setFont(new Font(Font.MONOSPACED, astArea.getFont().getStyle(), astArea.getFont().getSize()));
        add(new JScrollPane(astArea), BorderLayout.CENTER);
        JPanel sp = new JPanel();
        sp.setLayout(new GridLayout(1, 4));
        sp.add(new JLabel(" Depth: "));
        ButtonGroup bg = new ButtonGroup();
        sp.add(btnOne);
        bg.add(btnOne);
        sp.add(btnThree);
        bg.add(btnThree);
        sp.add(btnRule);
        bg.add(btnRule);
        btnRule.setSelected(true);
        sp.add(btnFull);
        bg.add(btnFull);
        add(sp, BorderLayout.SOUTH);
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
                    visitorAstGeneration.theDepth = 1;
                } else if (btnThree.isSelected()) {
                    visitorAstGeneration.theDepth = 3;
                } else  if (btnRule.isSelected()) {
                    visitorAstGeneration.theDepth = Integer.MAX_VALUE - 1;
                } else {
                    visitorAstGeneration.theDepth = Integer.MAX_VALUE;
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
    private VisitorAstGeneration visitorAstGeneration;
    private Vll4jGui gui;
    private Thread myThread = null;
}
