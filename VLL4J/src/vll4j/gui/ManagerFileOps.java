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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import vll4j.core.Utils;
import vll4j.tree.NodeBase;

public class ManagerFileOps {

    ManagerFileOps(Vll4jGui gui) {
        this.gui = gui;
    }
    
    Action fileNewAction = new AbstractAction("New", Resources.new16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            grammarFile = null;
            gui.reset(true);
            gui.theRuleManager.reset();
        }
    };

    void openInputStream(InputStream is, boolean tokensOnly) {
        gui.reset(false);
        try {
            gui.theForest.openInputStream(is, tokensOnly);
            if (!tokensOnly) {
                gui.regexParsers.whiteSpaceRegex = gui.theForest.whiteSpace;
                gui.regexParsers.commentSpecRegex = gui.theForest.comment;
                gui.regexParsers.resetWhitespace();
                gui.theRuleManager.theComboBox.setAction(null);
                gui.theRuleManager.theComboBox.removeAllItems();
                for (String ruleName : gui.theForest.ruleBank.keySet()) {
                    gui.theRuleManager.theComboBox.addItem(ruleName);
                }
                gui.theRuleManager.theComboBox.setMaximumSize(gui.theRuleManager.theComboBox.getPreferredSize());
                gui.theRuleManager.theComboBox.setAction(gui.theRuleManager.comboBoxAction);
                gui.theTreePanel.setRuleName(gui.theRuleManager.theComboBox.getItemAt(0));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(gui, String.format("Error processing file: %s", ex),
                    "ERROR - File open", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    Action fileOpenAction = new AbstractAction("Open", Resources.open16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = getFileChooser("Open");
            if (fc.showOpenDialog(gui) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            grammarFile = fc.getSelectedFile();
            try {
                InputStream is = new FileInputStream(grammarFile);
                openInputStream(is, false);
                gui.setGrammarName(grammarFile.getName());
                gui.theRuleManager.reset();
                is.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(gui, String.format("%s", ex),
                        "ERROR - File open", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
            }
        }
    };

    Action importTokenAction = new AbstractAction("Import tokens", Resources.import16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = getFileChooser("Import tokens");
            if (fc.showOpenDialog(gui) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            grammarFile = fc.getSelectedFile();
            try {
                InputStream is = new FileInputStream(grammarFile);
                openInputStream(is, true);
                is.close();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(gui, String.format("%s", ex),
                        "ERROR - Import tokens", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
            }
        }
    };
        
    private void writeFile(boolean tokensOnly) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(grammarFile);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(gui, ex, "ERROR - Save file", JOptionPane.ERROR_MESSAGE);
            return;
        }
        pw.println("<?xml version=\"1.0\"?>\n");
        pw.println("<!-- *****************  Do not edit  ***************** -->");
        pw.println("<!-- Generated by VisualLangLab (http://vll.java.net/) -->");
        pw.println("<!-- *****************  Do not edit  ***************** -->");
        pw.println("<VLL-Grammar>");
        pw.println("  <Tokens>");
        for (Map.Entry<String, String> me : gui.theForest.tokenBank.entrySet()) {
            String value = me.getValue();
            if (value.startsWith("L")) {
                pw.printf("    <Literal Name=\"%s\" Pattern=\"%s\" />%n", me.getKey(),
                        Utils.encode4xml(value.substring(1)));
            } else {
                pw.printf("    <Regex Name=\"%s\" Pattern=\"%s\" />%n", me.getKey(),
                        Utils.encode4xml(value).substring(1));
            }
        }
        pw.println("  </Tokens>");
        if (!tokensOnly) {
            pw.printf("  <Whitespace>%s</Whitespace>%n", 
                    Utils.encode4xml(gui.regexParsers.whiteSpaceRegex));
            pw.printf("  <Comments>%s</Comments>%n", 
                    Utils.encode4xml(gui.regexParsers.commentSpecRegex));
            pw.println("  <Parsers>");
            VisitorXmlGeneration xmlWriter = new VisitorXmlGeneration(pw);
            for (Map.Entry<String, NodeBase> me : gui.theForest.ruleBank.entrySet()) {
                me.getValue().accept(xmlWriter);
            }
            pw.println("  </Parsers>");
        }
        pw.println("</VLL-Grammar>");
        pw.close();
        gui.setGrammarName(grammarFile.getName());
    }
    
    Action fileSaveAction = new AbstractAction("Save", Resources.save16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (grammarFile == null) {
                JFileChooser fc = getFileChooser("Save");
                if (fc.showOpenDialog(gui) != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                grammarFile = fc.getSelectedFile();
                if (grammarFile.exists()) {
                    if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(gui, "Overwrite existing file?", "Save As", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                        return;
                    }
                }
            }
            writeFile(false);
        }
    };
    
    Action fileSaveAsAction = new AbstractAction("Save As", Resources.saveAs16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = getFileChooser("Save As");
            if (fc.showOpenDialog(gui) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            grammarFile = fc.getSelectedFile();
            if (grammarFile.exists()) {
                if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(gui, "Overwrite existing file?", "Save As", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                    return;
                }
            }
            writeFile(false);
        }
    };
    
    Action exportTokenAction = new AbstractAction("Export tokens", Resources.export16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = getFileChooser("Export tokens");
            if (fc.showOpenDialog(gui) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            grammarFile = fc.getSelectedFile();
            if (grammarFile.exists()) {
                if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(gui, "Overwrite existing file?", "Save As", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                    return;
                }
            }
            writeFile(true);
        }
    };
        
    Action fileExitAction = new AbstractAction("Exit") {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    };

    private JFileChooser getFileChooser(String title) {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        }
        fileChooser.setDialogTitle(title);
        fileChooser.setApproveButtonText(title.charAt(0) == 'S' ? "Save" : "Open");
        return fileChooser;
    }
    
    Vll4jGui gui;
    private JFileChooser fileChooser;
    private File grammarFile = null;
}
