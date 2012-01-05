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

import vll4j.tree.Multiplicity;
import vll4j.tree.NodeBase;
import vll4j.tree.NodeSequence;
import vll4j.tree.NodeChoice;
import vll4j.tree.NodeRoot;
import vll4j.tree.NodeRepSep;
import vll4j.tree.NodeReference;
import vll4j.tree.NodeLiteral;
import vll4j.tree.NodeRegex;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import vll4j.core.Utils;

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

    private void populateNode(Node xn, NodeBase pn) {
        NamedNodeMap attr = xn.getAttributes();
        if (attr.getNamedItem("Drop") != null) {
            pn.isDropped = true;
        }
        if (attr.getNamedItem("ActionText") != null) {
            pn.actionText = attr.getNamedItem("ActionText").getTextContent();
            String status = gui.theActionCodePanel.compileActionCode(pn);
            if (status != null) {
                JOptionPane.showMessageDialog(gui, String.format("Action-Code error at '%s': %s%n", pn.nodeName(), status),
                        "ERROR - Open file", JOptionPane.ERROR_MESSAGE);
            }
        }
        if (attr.getNamedItem("Description") != null) {
            pn.description = attr.getNamedItem("Description").getTextContent();
        }
        if (attr.getNamedItem("ErrMsg") != null) {
            pn.errorMessage = attr.getNamedItem("ErrMsg").getTextContent();
        }
        if (attr.getNamedItem("Mult") != null) {
            String m = attr.getNamedItem("Mult").getTextContent();
            if (m.equals("*")) {
                pn.multiplicity = Multiplicity.ZeroOrMore;
            } else if (m.equals("+")) {
                pn.multiplicity = Multiplicity.OneOrMore;
            } else if (m.equals("?")) {
                pn.multiplicity = Multiplicity.ZeroOrOne;
            } else if (m.equals("0")) {
                pn.multiplicity = Multiplicity.Not;
            } else if (m.equals("=")) {
                pn.multiplicity = Multiplicity.Guard;
            }
        }
        if (pn instanceof NodeChoice) {
        } else if (pn instanceof NodeLiteral) {
        } else if (pn instanceof NodeReference) {
        } else if (pn instanceof NodeRegex) {
        } else if (pn instanceof NodeRepSep) {
        } else if (pn instanceof NodeRoot) {
            if (attr.getNamedItem("Packrat") != null) {
                ((NodeRoot) pn).isPackrat = true;
            }
        } else if (pn instanceof NodeSequence) {
        }
        NodeList clist = xn.getChildNodes();
        for (int i = 0; i < clist.getLength(); ++i) {
            Node cn = clist.item(i);
            String elmtName = cn.getNodeName();
            if (elmtName.equals("Choice")) {
                NodeBase c = new NodeChoice();
                pn.add(c);
                populateNode(cn, c);
            } else if (elmtName.equals("Token")) {
                String tokenName = cn.getAttributes().getNamedItem("Ref").getTextContent();
                String tokenValue = gui.theForest.tokenBank.get(tokenName);
                NodeBase c = tokenValue.charAt(0) == 'L' ? new NodeLiteral(tokenName)
                        : new NodeRegex(tokenName);
                pn.add(c);
                populateNode(cn, c);
            } else if (elmtName.equals("Reference")) {
                String ruleName = cn.getAttributes().getNamedItem("Ref").getTextContent();
                NodeBase c = new NodeReference(ruleName);
                pn.add(c);
                populateNode(cn, c);
            } else if (elmtName.equals("RepSep")) {
                NodeBase c = new NodeRepSep();
                pn.add(c);
                populateNode(cn, c);
            } else if (elmtName.equals("Root")) {
                // Never found
            } else if (elmtName.equals("Sequence")) {
                NodeBase c = new NodeSequence();
                pn.add(c);
                populateNode(cn, c);
            }
        }
    }
    
    void openInputStream(InputStream is) {
        Element docElmt = null;
        gui.reset(false);
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document d = db.parse(is);
            docElmt = d.getDocumentElement();
            String whiteSpace = docElmt.getElementsByTagName("Whitespace").item(0).getTextContent();
            String comment = docElmt.getElementsByTagName("Comments").item(0).getTextContent();
            gui.theMiscFunctionsManager.whiteSpace = whiteSpace;
            gui.theMiscFunctionsManager.commentSpec = comment;
            gui.theMiscFunctionsManager.setWhitespace();
            NodeList regs = docElmt.getElementsByTagName("Regex");
            for (int i = 0; i < regs.getLength(); ++i) {
                Node r = regs.item(i);
                String regName = r.getAttributes().getNamedItem("Name").getTextContent();
                String regPat = r.getAttributes().getNamedItem("Pattern").getTextContent();
                gui.theForest.tokenBank.put(regName, "R" + regPat);
            }
            NodeList lits = docElmt.getElementsByTagName("Literal");
            for (int i = 0; i < lits.getLength(); ++i) {
                Node r = lits.item(i);
                String litName = r.getAttributes().getNamedItem("Name").getTextContent();
                String litPat = r.getAttributes().getNamedItem("Pattern").getTextContent();
                gui.theForest.tokenBank.put(litName, "L" + litPat);
            }
            NodeList parsers = docElmt.getElementsByTagName("Parser");
            for (int i = 0; i < parsers.getLength(); ++i) {
                Node xNode = parsers.item(i);
                String ruleName = xNode.getAttributes().getNamedItem("Name").getTextContent();
                NodeRoot root = new NodeRoot(ruleName);
                populateNode(xNode, root);
                gui.theForest.ruleBank.put(ruleName, root);
            }
            gui.theRuleManager.theComboBox.setAction(null);
            gui.theRuleManager.theComboBox.removeAllItems();
            for (String ruleName : gui.theForest.ruleBank.keySet()) {
                gui.theRuleManager.theComboBox.addItem(ruleName);
            }
            gui.theRuleManager.theComboBox.setMaximumSize(gui.theRuleManager.theComboBox.getPreferredSize());
            gui.theRuleManager.theComboBox.setAction(gui.theRuleManager.comboBoxAction);
            gui.theTreePanel.setRuleName(gui.theRuleManager.theComboBox.getItemAt(0));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(gui, String.format("Error processing file: %s", ex),
                    "ERROR - File open", JOptionPane.ERROR_MESSAGE);
                return;
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
                openInputStream(is);
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

    private void writeFile() {
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
        pw.printf("  <Whitespace>%s</Whitespace>%n", 
                Utils.encode4xml(gui.theMiscFunctionsManager.whiteSpace));
        pw.printf("  <Comments>%s</Comments>%n", 
                Utils.encode4xml(gui.theMiscFunctionsManager.commentSpec));
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
        pw.println("  <Parsers>");
        VisitorXmlGeneration xmlWriter = new VisitorXmlGeneration(pw);
        for (Map.Entry<String, NodeBase> me : gui.theForest.ruleBank.entrySet()) {
            me.getValue().accept(xmlWriter);
        }
        pw.println("  </Parsers>");
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
            writeFile();
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
            writeFile();
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
