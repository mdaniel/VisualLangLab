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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import net.java.vll.vll4j.api.NodeBase;
import net.java.vll.vll4j.combinator.Utils;

public class ManagerTokens {
    ManagerTokens(Vll4jGui theGui) {
        this.gui = theGui;
    }
    
    private String[] getTokenInfo(boolean isRegex) {
        String title = String.format("New %s", isRegex ? "regex" : "literal");
        Pattern inPattern = Pattern.compile(isRegex ? "([^\\s]+)|([a-zA-Z][a-zA-Z0-9$_]*(?::\\-?\\d+)?)\\s+(.+)" :
            "([^\\s]+)|([a-zA-Z][a-zA-Z0-9$_]*)\\s+(.+)");
        String input = JOptionPane.showInputDialog(gui, "Enter: name & spaces (optional), pattern",
                title, JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().length() == 0)
            return null;
        Matcher m = inPattern.matcher(input.trim());
        if (!m.matches()) {
            JOptionPane.showMessageDialog(gui, "Bad format\nExpected name, space(s), pattern", 
                    "WARNING - " + title, JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String tokenName = (m.group(1) == null) ? m.group(2) : String.format("\"%s\"", m.group(0));
        if (gui.theForest.tokenBank.containsKey(tokenName)) {
            JOptionPane.showMessageDialog(gui, "Name conflict\nA token with this name already exists", 
                    "WARNING - " + title, JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String tokenPattern = (m.group(1) == null) ? m.group(3) : m.group(0);
        try {
            String reg = Utils.unEscape(tokenPattern);
            if (isRegex)
                Pattern.compile(reg);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(gui, ex.toString(), "WARNING - New " +
                    (isRegex ? "regex" :"literal"), JOptionPane.WARNING_MESSAGE);
            return null;
        }
        boolean ok = true;
        for (Map.Entry<String,String> e: gui.theForest.tokenBank.entrySet()) {
            if (e.getValue().substring(1).equals(tokenPattern) &&
                    (e.getKey().endsWith("_") == tokenName.endsWith("_"))) {
                JOptionPane.showMessageDialog(gui, String.format("Pattern conflict\nToken '%s' uses the same pattern",
                        e.getKey()), "WARNING - " + title, JOptionPane.WARNING_MESSAGE);
                ok = false;
            }
        }
        if (isRegex && (!tokenPattern.equals("\\\\z") && "".matches(Utils.unEscape(tokenPattern)))) {
            JOptionPane.showMessageDialog(gui, "Bad pattern\nPattern matches empty string", "WARNING - " +
                    title, JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (isRegex && tokenPattern.equals("\\\\z") && !tokenName.endsWith("_")) {
            JOptionPane.showMessageDialog(gui, "Bad name\nEOF must be local token", "WARNING - " +
                    title, JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String pattern = (isRegex ? "R" : "L") + tokenPattern;
        return ok ? new String[] {tokenName, pattern} : null;
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
            gui.theForest.tokenBank.put(info[0], info[1]);
        }
    };
    
    Action newRegexAction = new AbstractAction("New regex", Resources.newRegex) {
        @Override
        public void actionPerformed(ActionEvent e) {
            String info[] = getTokenInfo(true);
            if (info == null)
                return;
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
            String names[] = gui.theForest.tokenBank.keySet().toArray(new String[gui.theForest.tokenBank.size()]);
            List<String> editableNames = new ArrayList<String>();
            for (String key: names) {
                String val = gui.theForest.tokenBank.get(key);
//System.out.printf("key: %s, val: %s%n", key, val);
                if (val.length() == key.length() - 1 &&
                        val.substring(1).equals(key.substring(1, key.length() - 1)))
                    continue;
                editableNames.add(key);
            }
            if (editableNames.size() == 0) {
                JOptionPane.showMessageDialog(gui, "No editable tokens defined yet", "WARNING - Edit token", JOptionPane.WARNING_MESSAGE);
                return;
            }
            names = editableNames.toArray(new String[editableNames.size()]);
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
        
    Vll4jGui gui;
}
