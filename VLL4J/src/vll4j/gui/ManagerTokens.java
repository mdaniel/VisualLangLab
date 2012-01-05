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
import vll4j.core.Utils;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

public class ManagerTokens {
    ManagerTokens(Vll4jGui theGui) {
        this.gui = theGui;
    }
    
    private String[] getTokenInfo(boolean isRegex) {
        String title = String.format("New %s", isRegex ? "regex" : "literal");
        Pattern inPattern = Pattern.compile(isRegex ? "([a-zA-Z][a-zA-Z0-9$_]*(?::\\-?\\d+)?)\\s+(.+)" : 
            "([a-zA-Z][a-zA-Z0-9$_]*)\\s+(.+)");
        String input = JOptionPane.showInputDialog(gui, "Enter name, space(s), pattern", title, JOptionPane.QUESTION_MESSAGE);
        if (input == null)
            return null;
        Matcher m = inPattern.matcher(input);
        if (!m.matches()) {
            JOptionPane.showMessageDialog(gui, "Bad format\nExpected name, space(s), pattern", 
                    "WARNING - " + title, JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String name = m.group(1);
        if (gui.theForest.tokenBank.containsKey(name)) {
            JOptionPane.showMessageDialog(gui, "Name conflict\nA token with this name already exists", 
                    "WARNING - " + title, JOptionPane.WARNING_MESSAGE);
            return null;
        }
        // ToDo: need to check if regular expression is valid and matches empry string
        // ToDo: where should un-escaping happen?
        boolean ok = true;
        for (Map.Entry<String,String> e: gui.theForest.tokenBank.entrySet()) {
            if (e.getValue().substring(1).equals(m.group(2))) {
                JOptionPane.showMessageDialog(gui, String.format("Pattern conflict\nToken '%s' uses the same pattern", e.getKey()), "WARNING - " + title, JOptionPane.WARNING_MESSAGE);
                ok = false;
            }
        }
        String pattern = (isRegex ? "R" : "L") + m.group(2);
        return ok ? new String[] {name, pattern} : null;
    }
    
    private String[] findTokenInRules(String token) {
            VisitorTokenSearch v = new VisitorTokenSearch(token);
            for (NodeBase n: gui.theForest.ruleBank.values()) {
                n.accept(v);
            }
            return v.ruleSet.toArray(new String[v.ruleSet.size()]);
    }
    
    Action newLiteralAction = new AbstractAction("New literal", Resources.newLiteral) {
        @Override
        public void actionPerformed(ActionEvent e) {
            String info[] = getTokenInfo(false);
            if (info == null)
                return;
            try {
                Utils.unEscape(info[1]);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(gui, ex.toString(), "WARNING - New literal", JOptionPane.WARNING_MESSAGE);
                return;
            }
            gui.theForest.tokenBank.put(info[0], info[1]);
        }
    };
    
    Action newRegexAction = new AbstractAction("New regex", Resources.newRegex) {
        @Override
        public void actionPerformed(ActionEvent e) {
            String info[] = getTokenInfo(true);
            if (info == null)
                return;
            try {
                String reg = Utils.unEscape(info[1]);
                Pattern.compile(reg);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(gui, ex.toString(), "WARNING - New regex", JOptionPane.WARNING_MESSAGE);
                return;
            }
            gui.theForest.tokenBank.put(info[0], info[1]);
        }
    };
    
    void editToken(String token) {
        String value = gui.theForest.tokenBank.get(token);
        boolean isRegex = value.charAt(0) == 'R';
        value = value.substring(1);
        String title = String.format(isRegex ? "Edit regex '%s'" : "Edit literal '%s'", token);
        String newValue = (String) JOptionPane.showInputDialog(gui, "Edit pattern", title, 
                JOptionPane.QUESTION_MESSAGE, null, null, value);
        if (newValue == null || newValue.equals(value)) {
            return;
        }
        if (isRegex) {
            try {
                Pattern.compile(Utils.unEscape(newValue));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(gui, ex.getMessage(), title, JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        String pattern = (isRegex ? "R" : "L") + newValue;
        for (Map.Entry<String, String> me : gui.theForest.tokenBank.entrySet()) {
            if (me.getValue().equals(pattern)) {
                JOptionPane.showMessageDialog(gui, String.format("Token %s has the same pattern", me.getKey()), title, JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        gui.theForest.tokenBank.put(token, pattern);
    }
    
    Action editTokenAction = new AbstractAction("Edit token", Resources.edit16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            Object names[] = gui.theForest.tokenBank.keySet().toArray();
            if (names.length == 0) {
                JOptionPane.showMessageDialog(gui, "No tokens defined yet", "WARNING - Edit token", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String token = (String)JOptionPane.showInputDialog(gui, "Select token to edit", "Edit token", JOptionPane.QUESTION_MESSAGE, 
                null, names, names[0]);
            if (token == null) 
                return;
            editToken(token);
        }
    };
    
    Action findTokenAction = new AbstractAction("Find token", Resources.replace16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            Object names[] = gui.theForest.tokenBank.keySet().toArray();
            if (names.length == 0) {
                JOptionPane.showMessageDialog(gui, "No tokens defined yet", "WARNING - Find token", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String tokenName = (String)JOptionPane.showInputDialog(gui, "Select token to find", "Find token", JOptionPane.QUESTION_MESSAGE, 
                null, names, names[0]);
            if (tokenName == null)
                return;
            String rules[] = findTokenInRules(tokenName);
            if (rules.length == 0) {
                JOptionPane.showMessageDialog(gui, String.format("Token '%s' is not referred in any rule", tokenName), "WARNING - Find token", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String rule = (String)JOptionPane.showInputDialog(gui, 
                    String.format("Token '%s' is referred in rules listed below\nClick OK to display selected rule", tokenName), "Find token", JOptionPane.QUESTION_MESSAGE, 
                null, rules, rules[0]);
            if (rule != null)
                gui.theRuleManager.theComboBox.setSelectedItem(rule);
        }
    };
    
    Action deleteTokenAction = new AbstractAction("Delete token", Resources.delete16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            Object names[] = gui.theForest.tokenBank.keySet().toArray();
            if (names.length == 0) {
                JOptionPane.showMessageDialog(gui, "No tokens defined yet", "WARNING - Delete token", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String tokenToDelete = (String)JOptionPane.showInputDialog(gui, "Select token to delete", "Delete token", JOptionPane.QUESTION_MESSAGE, 
                null, names, names[0]);
            if (tokenToDelete == null)
                return;
            String rules[] = findTokenInRules(tokenToDelete);
            if (rules.length != 0) {
                String rule = (String)JOptionPane.showInputDialog(gui, 
                        String.format("Can't delete token '%s'\nUsed in rules listed below\nClick OK to display a rule", 
                        tokenToDelete), "WARNING - Delete token", JOptionPane.WARNING_MESSAGE, 
                    null, rules, rules[0]);
                if (rule != null)
                    gui.theRuleManager.theComboBox.setSelectedItem(rule);
                return;
            }
            int opt = JOptionPane.showConfirmDialog(gui, String.format("Delete '%s' ?", tokenToDelete), "Delete token", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                gui.theForest.tokenBank.remove(tokenToDelete);
            }
        }
    };
        
    Action importTokenAction = new AbstractAction("Import tokens", Resources.import16) {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    };
        
    Action exportTokenAction = new AbstractAction("Export tokens", Resources.export16) {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    };
        
    Vll4jGui gui;
}
