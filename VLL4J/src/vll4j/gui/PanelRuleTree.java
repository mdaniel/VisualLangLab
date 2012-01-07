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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import vll4j.tree.*;

public class PanelRuleTree extends JPanel implements TreeSelectionListener {

    PanelRuleTree(Vll4jGui gui) {
        setLayout(new BorderLayout());
        this.gui = gui;
        selectedNode = rootNode = (NodeRoot)gui.theForest.ruleBank.get("Main");
        theModel = new DefaultTreeModel(rootNode);
        theTree = new JTree(theModel);
        ToolTipManager.sharedInstance().registerComponent(theTree); 
        theTree.setDragEnabled(true);
        theTree.setTransferHandler(new TransferHandlerTree());
        theTree.setSelectionRow(0);
        theTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        theTree.setDragEnabled(true);
        theTree.setDropMode(DropMode.INSERT);
        theTree.setTransferHandler(new TransferHandlerTree());
        theTree.addTreeSelectionListener(this);
        theTree.addMouseListener(treePopupListener);
        theTree.setShowsRootHandles(true);
        theTree.setCellRenderer(new RendererRuleNode());
        add(new JScrollPane(theTree), BorderLayout.CENTER);
        treePopupMenu = new PopupMenuTree(this);
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BorderLayout());
        btnPanel.add(statusLabel, BorderLayout.CENTER);
        btnPanel.add(helpButton, BorderLayout.EAST);
        add(btnPanel, BorderLayout.SOUTH);
    }
    
    void setRuleName(String ruleName) {
        selectedNode = gui.theForest.ruleBank.get(ruleName);
        rootNode = (NodeRoot)selectedNode;
        TreeNode p[] = selectedNode.getPath();
        TreePath tp[] = new TreePath[p.length];
        for (int i = 0; i < tp.length; ++i)
            tp[i] = new TreePath(p[i]);
        theTree.removeTreeSelectionListener(this);
        theModel.setRoot(rootNode);
        theTree.setSelectionPaths(tp);
        theTree.addTreeSelectionListener(this);
        for (int i = 0; i < theTree.getRowCount(); ++i) {
            theTree.expandRow(i);
        }
        gui.theAstPanel.resetView();
        gui.theActionCodePanel.resetView();
        statusLabel.setText(String.format(" %s %s", selectedNode.nodeType(), selectedNode.nodeName()));
    }

    void resetNodeDisplay(NodeBase node) {
        theModel.nodeChanged(selectedNode);
        theTree.expandPath(new TreePath(selectedNode.getPath()));
        theTree.scrollPathToVisible(new TreePath(node));
        gui.theAstPanel.resetView();
    }
    
    Action addChoiceAction = new AbstractAction("Choice", Resources.choice) {
        @Override
        public void actionPerformed(ActionEvent e) {
            NodeChoice newNode = new NodeChoice();
            theModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount());
            resetNodeDisplay(newNode);
        }
    };
    
    Action addSequenceAction = new AbstractAction("Sequence", Resources.sequence) {
        @Override
        public void actionPerformed(ActionEvent e) {
            NodeSequence newNode = new NodeSequence();
            theModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount());
            resetNodeDisplay(newNode);
        }
    };
    
    Action addRepSepAction = new AbstractAction("RepSep", Resources.repSep) {
        @Override
        public void actionPerformed(ActionEvent e) {
            NodeRepSep newNode = new NodeRepSep();
            theModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount());
            resetNodeDisplay(newNode);
        }
    };
    
    Action addSemPredAction = new AbstractAction("SemPred", Resources.predicate) {
        @Override
        public void actionPerformed(ActionEvent e) {
            NodeSemPred newNode = new NodeSemPred();
            theModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount());
            resetNodeDisplay(newNode);
        }
    };
    
    Action goToAction = new AbstractAction("Go to") {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedNode instanceof NodeReference) {
                String ruleName = ((NodeReference)selectedNode).refRuleName;
                gui.theRuleManager.theComboBox.setSelectedItem(ruleName);
            } else if (selectedNode instanceof NodeLiteral) {
                String ruleName = ((NodeLiteral)selectedNode).literalName;
                gui.theTokenManager.editToken(ruleName);
            } else if (selectedNode instanceof NodeRegex) {
                String ruleName = ((NodeRegex)selectedNode).regexName;
                gui.theTokenManager.editToken(ruleName);
            }
        }
    };
    
    Action addTokenAction = new AbstractAction("Token", Resources.token) {
        @Override
        public void actionPerformed(ActionEvent e) {
            Object names[] = gui.theForest.tokenBank.keySet().toArray();
            if (names.length == 0) {
                JOptionPane.showMessageDialog(treePopupMenu, "No tokens defined yet", "ERROR - Add token", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Object token = JOptionPane.showInputDialog(gui, "Select token", "Add token", JOptionPane.QUESTION_MESSAGE, 
                null, names, names[0]);
            if (token == null)
                return;
            String pattern = gui.theForest.tokenBank.get(token);
            boolean isRegex = pattern.charAt(0) == 'R';
            NodeBase newNode = isRegex ? new NodeRegex((String)token) : new NodeLiteral((String)token);
            theModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount());
            resetNodeDisplay(newNode);
        }
    };
    
    Action addReferenceAction = new AbstractAction("Reference", Resources.reference) {
        @Override
        public void actionPerformed(ActionEvent e) {
            Object names[] = gui.theForest.ruleBank.keySet().toArray();
            String rule = (String)JOptionPane.showInputDialog(gui, "Select rule", "Add reference", JOptionPane.QUESTION_MESSAGE, 
                null, names, names[0]);
            if (rule == null)
                return;
            NodeReference newNode = new NodeReference(rule);
            theModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount());
            resetNodeDisplay(newNode);
        }
    };

    Action multiplicityOneAction = new AbstractAction("1 (just one)") {
        @Override
        public void actionPerformed(ActionEvent e) {
            selectedNode.multiplicity = Multiplicity.One;
            resetNodeDisplay(selectedNode);
        }
    };
    
    Action multiplicityZeroOneAction = new AbstractAction("? (0 or 1)") {
        @Override
        public void actionPerformed(ActionEvent e) {
            selectedNode.multiplicity = Multiplicity.ZeroOrOne;
            resetNodeDisplay(selectedNode);
        }
    };
    
    Action multiplicityZeroMoreAction = new AbstractAction("* (0 or more)") {
        @Override
        public void actionPerformed(ActionEvent e) {
            selectedNode.multiplicity = Multiplicity.ZeroOrMore;
            resetNodeDisplay(selectedNode);
        }
    };
    
    Action multiplicityOneMoreAction = new AbstractAction("+ (1 or more)") {
        @Override
        public void actionPerformed(ActionEvent e) {
            selectedNode.multiplicity = Multiplicity.OneOrMore;
            resetNodeDisplay(selectedNode);
        }
    };
    
    Action multiplicityNotAction = new AbstractAction("0 (not)") {
        @Override
        public void actionPerformed(ActionEvent e) {
            selectedNode.multiplicity = Multiplicity.Not;
            resetNodeDisplay(selectedNode);
        }
    };
    
    Action multiplicityGuardAction = new AbstractAction("= (guard)") {
        @Override
        public void actionPerformed(ActionEvent e) {
            selectedNode.multiplicity = Multiplicity.Guard;
            resetNodeDisplay(selectedNode);
        }
    };
    
    Action cutAction = new AbstractAction("Cut", Resources.cut16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            NodeBase parent = (NodeBase)selectedNode.getParent();
            TreePath parentPath = theTree.getSelectionPath().getParentPath();
            theClipBoard = selectedNode;
            theClipBoard.isDropped = false;
            theModel.removeNodeFromParent(selectedNode);
            theTree.setSelectionPath(parentPath);
            resetNodeDisplay(parent);
        }
    };
    
    Action copyAction = new AbstractAction("Copy", Resources.copy16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            theClipBoard = (NodeBase)selectedNode.clone();
            theClipBoard.isDropped = false;
        }
    };
    
    Action pasteAction = new AbstractAction("Paste", Resources.paste16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            NodeBase newNode = (NodeBase)theClipBoard.clone();
            theModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount());
            resetNodeDisplay(selectedNode);
        }
    };
    
    Action deleteAction = new AbstractAction("Delete", Resources.delete16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            NodeBase parent = (NodeBase)selectedNode.getParent();
            TreePath parentPath = theTree.getSelectionPath().getParentPath();
            theModel.removeNodeFromParent(selectedNode);
            theTree.setSelectionPath(parentPath);
            resetNodeDisplay(parent);
        }
    };
    
    Action packratAction = new AbstractAction("Packrat") {
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(gui, "Packrat is NOT YET IMPLEMENTED", "WARNING - Packrat", JOptionPane.WARNING_MESSAGE);
            ((NodeRoot)selectedNode).isPackrat = ((JCheckBoxMenuItem)(e.getSource())).isSelected();
            resetNodeDisplay(selectedNode);
        }
    };
    
    Action traceAction = new AbstractAction("Trace") {
        @Override
        public void actionPerformed(ActionEvent e) {
            selectedNode.isTraced = ((JCheckBoxMenuItem)(e.getSource())).isSelected();
            resetNodeDisplay(selectedNode);
        }
    };
    
    Action dropAction = new AbstractAction("Drop") {
        @Override
        public void actionPerformed(ActionEvent e) {
            selectedNode.isDropped = ((JCheckBoxMenuItem)(e.getSource())).isSelected();
            resetNodeDisplay(selectedNode);
        }
    };
    
    Action descriptionAction = new AbstractAction("Description") {
        @Override
        public void actionPerformed(ActionEvent e) {
            String descr = (String)JOptionPane.showInputDialog(gui, "Enter description", "Description", 
                    JOptionPane.QUESTION_MESSAGE, null, null, selectedNode.description);
            if (descr == null || descr.equals(selectedNode.description))
                return;
            selectedNode.description = descr;
            resetNodeDisplay(selectedNode);
        }
    };
    
    Action errorMessageAction = new AbstractAction("Error message") {
        @Override
        public void actionPerformed(ActionEvent e) {
            String msg = (String)JOptionPane.showInputDialog(gui, "Enter error message", "Error message", 
                    JOptionPane.QUESTION_MESSAGE, null, null, selectedNode.errorMessage);
            if (msg == null || msg.equals(selectedNode.errorMessage))
                return;
            selectedNode.errorMessage = msg;
            resetNodeDisplay(selectedNode);
        }
    };
    
    @Override
    public void valueChanged(TreeSelectionEvent e) {
        selectedNode = (NodeBase) e.getPath().getLastPathComponent();
        gui.theActionCodePanel.resetView();
        gui.theAstPanel.resetView();
        statusLabel.setText(String.format(" %s %s", selectedNode.nodeType(), selectedNode.nodeName()));
    }
    
    PopupMenuTree treePopupMenu;
    NodeRoot rootNode;
    private DefaultTreeModel theModel;
    Vll4jGui gui;
    JTree theTree;
    NodeBase selectedNode = null;
    PopupListenerTree treePopupListener = new PopupListenerTree(this);
    NodeBase theClipBoard = null;
    private JButton helpButton = new JButton(Resources.help16);
    private JLabel statusLabel = new JLabel("  ");
}
