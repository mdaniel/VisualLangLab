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

import java.awt.event.ActionEvent;
import java.util.Stack;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import vll4j.tree.NodeBase;
import vll4j.tree.NodeRoot;

public class ManagerRules {
    
    ManagerRules(Vll4jGui gui) {
        this.gui = gui;
    }
    
    void reset() {
        ruleStack.clear();
        ruleBackAction.setEnabled(false);
        lastRulePopped = "";
    }
    
    Action ruleBackAction = new AbstractAction("Back", Resources.back16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            lastRulePopped = ruleStack.pop();
            theComboBox.setSelectedItem(lastRulePopped);
            setEnabled(!ruleStack.isEmpty());
        }
    };
    
    void addRuleToComboBox(String ruleName) {
        theComboBox.setAction(null);
        int oldLength = theComboBox.getItemCount();
        for (int i = theComboBox.getItemCount() - 1; i >= 0; --i) {
            String item = theComboBox.getItemAt(i);
            if (ruleName.compareTo(item) > 0) {
                theComboBox.insertItemAt(ruleName, i + 1);
                break;
            } else if (ruleName.compareTo(item) == 0) {
                JOptionPane.showMessageDialog(gui, "A rule with this name already exists", 
                        "WARNING - New rule", JOptionPane.WARNING_MESSAGE);
                break;
            } else if (i == 0) {
                theComboBox.insertItemAt(ruleName, 0);
            }
        }
        if (oldLength != theComboBox.getItemCount()) {
            theComboBox.setMaximumSize(theComboBox.getPreferredSize());
            theComboBox.setSelectedItem(ruleName);
            gui.theTreePanel.setRuleName(ruleName);
        }
        theComboBox.setAction(comboBoxAction);
    }
    
    void removeRuleFromComboBox(String ruleName) {
//for (int i = 0; i < theComboBox.getItemCount(); ++i) 
//    System.out.printf("%d %s%n", i, theComboBox.getItemAt(i));
        int idx = theComboBox.getSelectedIndex();
//System.out.printf("getSelectedIndex() %d%n", idx);
//System.out.printf("getItemAt(idx) %s%n", theComboBox.getItemAt(idx));
//        String nextItem = (idx == theComboBox.getItemCount() - 1) ?
//                 theComboBox.getItemAt(idx - 1) : theComboBox.getItemAt(idx + 1);
//System.out.println(idx == theComboBox.getItemCount() - 1);
//System.out.printf("nextItem %s%n", nextItem);
        theComboBox.setAction(null);
        theComboBox.removeItemAt(idx);
        theComboBox.setAction(comboBoxAction);
//        gui.theTreePanel.setRuleName(nextItem);
    }
    
    Action ruleNewAction = new AbstractAction("New rule", Resources.newReference) {
        @Override
        public void actionPerformed(ActionEvent e) {
            String ruleName = JOptionPane.showInputDialog(gui, "Enter rule name", "New rule", JOptionPane.QUESTION_MESSAGE);
            if (ruleName == null)
                return;
            ruleName = ruleName.trim();
            if (gui.theForest.ruleBank.containsKey(ruleName)) {
                JOptionPane.showMessageDialog(gui, "Rule name already used", "WARNING - New rule", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!ruleName.matches("[a-zA-Z$_][a-zA-Z$_0-9-]*")) {
                JOptionPane.showMessageDialog(gui, "Illegal rule name", "WARNING - New rule", JOptionPane.WARNING_MESSAGE);
                return;
            }
            ruleStack.add((String)theComboBox.getSelectedItem());
            ruleBackAction.setEnabled(!ruleStack.isEmpty());
            gui.theForest.ruleBank.put(ruleName, new NodeRoot(ruleName));
            addRuleToComboBox(ruleName);
        }
    };
    
    private String[] findRuleInRules(String rule) {
            VisitorRuleSearch v = new VisitorRuleSearch(rule);
            for (NodeBase n: gui.theForest.ruleBank.values()) {
                n.accept(v);
            }
            return v.ruleSet.toArray(new String[v.ruleSet.size()]);
    }
    
    Action ruleFindAction = new AbstractAction("Find rule", Resources.search16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            String ruleName = (String)theComboBox.getSelectedItem();
            String rules[] = findRuleInRules(ruleName);
            if (rules.length == 0) {
                JOptionPane.showMessageDialog(gui, String.format("Rule '%s' isn't referred in any other rule", ruleName), "WARNING - Find rule", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String selectedRule = (String)JOptionPane.showInputDialog(gui, 
                    String.format("Rule '%s' is referred in rules listed below\nClick OK to display selected rule", ruleName), "Find rule", JOptionPane.QUESTION_MESSAGE, 
                null, rules, rules[0]);
            if (selectedRule != null) {
                theComboBox.setSelectedItem(selectedRule);
            }
        }
    };
    
    Action ruleRenameAction = new AbstractAction("Rename rule", Resources.refresh16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            String currentName = (String)theComboBox.getSelectedItem();
            String newName = (String)JOptionPane.showInputDialog(gui, "Enter new name", "Rename rule", 
                    JOptionPane.QUESTION_MESSAGE, null, null, currentName);
            if (newName == null)
                return;
            newName = newName.trim();
            if (!newName.matches("[a-zA-Z$_][a-zA-Z$_0-9-]*")) {
                JOptionPane.showMessageDialog(gui, "Illegal rule name", "WARNING - Rename rule", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (gui.theForest.ruleBank.containsKey(newName)) {
                JOptionPane.showMessageDialog(gui, "Rule name already used", "WARNING - Rename rule", JOptionPane.WARNING_MESSAGE);
                return;
            }
            VisitorRuleRenaming v = new VisitorRuleRenaming(currentName, newName);
            for (NodeBase n: gui.theForest.ruleBank.values()) {
                n.accept(v);
            }
//System.out.println("Visitor done");
            NodeBase n = gui.theForest.ruleBank.get(currentName);
            gui.theForest.ruleBank.put(newName, n);
            gui.theForest.ruleBank.remove(currentName);
            if (theComboBox.getItemCount() == 1) {
                theComboBox.setAction(null);
                theComboBox.removeAllItems();
                theComboBox.addItem(newName);
                theComboBox.setAction(comboBoxAction);
                theComboBox.setMaximumSize(theComboBox.getPreferredSize());
            } else {
                removeRuleFromComboBox(currentName);
                addRuleToComboBox(newName);
                for (int i = 0; i < ruleStack.size(); ++i) {
                    if (ruleStack.elementAt(i).equals(currentName))
                        ruleStack.setElementAt(newName, i);
                }
            }
        }
    };
    
    Action ruleDeleteAction = new AbstractAction("Delete rule", Resources.delete16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (theComboBox.getItemCount() == 1) {
                JOptionPane.showMessageDialog(gui, "Cant delete only rule - rename instead", "WARNING - Delete rule", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String ruleToDelete = (String)theComboBox.getSelectedItem();
            if (ruleToDelete == null)
                return;
            String refdRules[] = findRuleInRules(ruleToDelete);
            if (refdRules.length != 0) {
                String selectedRule = (String)JOptionPane.showInputDialog(gui, 
                        String.format("Cant delete rule '%s'\nused in rules listed below\nClick OK to display a rule", 
                        ruleToDelete), "WARNING - Delete rule", 
                        JOptionPane.WARNING_MESSAGE, null, refdRules, refdRules[0]);
                if (selectedRule != null)
                    theComboBox.setSelectedItem(selectedRule);
                return;
            }
            if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(gui, String.format("Delete rule '%s'?", ruleToDelete), "Delete rule", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                return;
            };
            gui.theForest.ruleBank.remove(ruleToDelete);
            removeRuleFromComboBox(ruleToDelete);
            ruleStack.remove(ruleToDelete);
            ruleBackAction.setEnabled(!ruleStack.isEmpty());
        }
    };
    
    Action comboBoxAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            String ruleName = (String)theComboBox.getSelectedItem();
            if (!ruleName.equals(lastRulePopped)) {
                ruleStack.add(gui.theTreePanel.rootNode.ruleName);
                ruleBackAction.setEnabled(!ruleStack.isEmpty());
            }
            gui.theTreePanel.setRuleName(ruleName);
            gui.theActionCodePanel.resetView();
        }
    };
    
    Vll4jGui gui;
    JComboBox<String> theComboBox;
    private Stack<String> ruleStack = new Stack<String>();
    private String lastRulePopped = "";
}
