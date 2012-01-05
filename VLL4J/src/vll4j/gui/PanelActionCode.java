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
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import vll4j.core.Reader;
import vll4j.tree.NodeRoot;

public class PanelActionCode extends JPanel {
    
    PanelActionCode(Vll4jGui gui) {
        this.gui = gui;
        setLayout(new BorderLayout());
        add(new JLabel("Action Code", SwingConstants.CENTER), BorderLayout.NORTH);
        codeArea.addKeyListener(keyAdapter);
        add(new JScrollPane(codeArea), BorderLayout.CENTER);
        saveButton.setEnabled(false);
        add(saveButton, BorderLayout.SOUTH);
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
    
    private ActionFunction compile(String script) throws ScriptException {
        if (compilable == null) {
            ScriptEngine se = new ScriptEngineManager().getEngineByName("JavaScript");
            compilable = (Compilable)(se);
        }
        script = script.substring(script.indexOf('('));
        final CompiledScript cs = compilable.compile(String.format("(function %s)(vllARG)", script));
        return new ActionFunction() {
            public Object run(Object arg, Reader r) throws ScriptException {
                cs.getEngine().put("vllARG", arg);
                cs.getEngine().put("vllLine", r.line());
                cs.getEngine().put("vllCol", r.column());
                cs.getEngine().put("vllOffset", r.offset());
                cs.getEngine().put("vllInput", r.source().subSequence(r.offset(), r.source().length()));
                cs.getEngine().put("vllParserTestInput", gui.theTestingPanel.inputArea);
                cs.getEngine().put("vllParserLog", gui.theTestingPanel.logArea);
                return cs.eval();
            }
        };
    }
    
    String compileActionCode(NodeBase node) {
        String script = node.actionText;
        if (script.trim().isEmpty()) {
            node.actionFunction = null;
            return null;
        } else {
            if (!functionMatcher.reset(script).matches()) {
                node.actionFunction = null;
                return "Need JavaScript function with 1 argument";
            }
            try {
                node.actionFunction = compile(script);
                return null;
            } catch (Exception e) {
                node.actionFunction = null;
                String message = e.getMessage();
                message = message.contains(": ") ? message.substring(message.indexOf(": ") + 2) : message;
                return message;
            }
        }
    }
    
    private Action saveAction = new AbstractAction("Save") {
        public void actionPerformed(ActionEvent o) {
            gui.theTreePanel.selectedNode.actionText = codeArea.getText();
            saveButton.setEnabled(false);
            String msg = compileActionCode(gui.theTreePanel.selectedNode);
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
        public void keyTyped(KeyEvent e) {
            saveButton.setEnabled(true);
        }
    };
    
    TextAreaCustom codeArea = new TextAreaCustom();
    private Color normalTextColor;
    private JButton saveButton = new JButton(saveAction);
    private Compilable compilable = null;
    Vll4jGui gui;
    private Matcher functionMatcher = Pattern.compile(
        "\\s*f(?:u(?:n(?:c(?:t(?:i(?:on?)?)?)?)?)?)?\\s*\\(\\s*[a-zA-Z][a-zA-Z0-9]*\\s*\\)(?s:.*)").matcher("");
}
