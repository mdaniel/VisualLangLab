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
import java.io.InputStream;
import java.net.URL;
import javax.swing.*;

public class ManagerHelp {

    ManagerHelp(Vll4jGui gui) {
        this.gui = gui;
        title = String.format("VisualLangLab %s", gui.version);
    }
    
    Action aboutAction = new AbstractAction("About VisualLangLab", Resources.information16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTabbedPane tp = new JTabbedPane();
            tp.add("VisualLangLab", new JLabel(Resources.splashImage));
            tp.add("Copyright", new JLabel(copyright));
            tp.add("Licenses", new JLabel(licenses));
            JOptionPane.showMessageDialog(gui, tp, title, JOptionPane.PLAIN_MESSAGE);
        }
        String copyright = "<html>" +
            "<b>VisualLangLab (http://vll.java.net/)</b><br/>" +
            "Copyright 2004, 2010, 2012, Sanjay Dasgupta <br/>(sanjay.dasgupta@gmail.com)<br/><br/>" +
            "VisualLangLab is free software: you can redistribute it and/or <br/>" +
            "modify it under the terms of the GNU General Public License as <br/>" +
            "published by the Free Software Foundation, either version 3 of <br/>" +
            "the License, or (at your option) any later version.<br/><br/>" +
            "VisualLangLab is distributed in the hope that it will be useful,<br/>" +
            "but WITHOUT ANY WARRANTY; without even the implied warranty<br/>" +
            "of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.<br/>" +
            "See the GNU General Public License for more details.<br/><br/>" +
            "You should have received a copy of the GNU General Public License<br/>" +
            "along with VisualLangLab. If not, see http://www.gnu.org/licenses/</html>";
        String licenses = "<html>" +
            "VisualLangLab uses the following public resources:<br/><br/>" +
            "1) Icon images from the Java look and feel Graphics Repository<br/>" +
            "(http://java.sun.com/developer/techDocs/hi/repository/)<br/><br/>" +
            "2) Some elements, rewritten in Java, of the <br/>" +
            "<i>scala.util.parsing.combinator</i> package of the Scala API. <br/>" +
            "(http://www.scala-lang.org/api/current/index.html#package)<br/><br/>" +
            "Copyright notices for the use and/or redistribution of these <br/>" +
            "items are included in the <i>licenses</i> directory of the ZIP and JAR <br/>" +
            "files of the distribution.<br/>" +
            "</html>";
    };
            
    Action versionCheck = new AbstractAction("Version check", Resources.tipOfTheDay16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(gui, "New version check coming soon!", 
                    "Version check", JOptionPane.PLAIN_MESSAGE);
        }
    };
    
    Action sampleTdarExpr = new AbstractAction("TDAR-Expr") {
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(gui, msg, "TDAR-Expr", JOptionPane.INFORMATION_MESSAGE);
            InputStream is = ClassLoader.getSystemClassLoader().
                    getResourceAsStream("vll4j/gui/resources/TDAR-Expr.vll");
            gui.reset(false);
            gui.theFileManager.openInputStream(is, false);
            gui.setGrammarName("TDAR-Expr");
            gui.theRuleManager.reset();
        }
        String msg = "<html>" + 
            "This example is based on the parser described in <br/>" + 
            "section 3.1, <i>Recognizing Language Syntax</i>, <br/>" + 
            "of the book <i>The Definitive ANTLR Reference</i> <br>" + 
            "(http://pragprog.com/book/tpantlr/the-definitive-antlr-reference)<br/><br/>" +
            "NOTE: Each 'statement' in the test input <br/>" +
            "(including the last) must end with a NEWLINE<br/><br/>" +
            "Read more about this parser at the following url:<br/>" + 
            "&nbsp;&nbsp;&nbsp;&nbsp;http://vll.java.net/examples/a-quick-tour.html";
    };

    Action sampleTdarExprActions = new AbstractAction("TDAR-Expr-Action") {
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(gui, msg, "TDAR-Expr-Action", JOptionPane.INFORMATION_MESSAGE);
            InputStream is = ClassLoader.getSystemClassLoader().
                    getResourceAsStream("vll4j/gui/resources/TDAR-Expr-Action.vll");
            gui.reset(false);
            gui.theFileManager.openInputStream(is, false);
            gui.setGrammarName("TDAR-Expr-Action");
            gui.theRuleManager.reset();
        }
        String msg = "<html>" + 
            "This example is based on the parser described in <br/>" + 
            "section 3.2, <i>Using Syntax to Drive Action Execution</i>, <br/>" + 
            "of the book <i>The Definitive ANTLR Reference</i> <br>" + 
            "(http://pragprog.com/book/tpantlr/the-definitive-antlr-reference)<br/><br/>" +
            "Actions (JavaScript functions) are associated with some <br/> " +
            "rule-tree nodes identified by the <i>action</i> attribute<br/><br/>" +
            "NOTE: Each 'statement' in the test input <br/>" +
            "(including the last) must end with a NEWLINE<br/><br/>" +
            "Read more about this parser at the following url:<br/>" + 
            "&nbsp;&nbsp;&nbsp;&nbsp;http://vll.java.net/examples/a-quick-tour.html";
    };

    Action sampleTdarExprAst = new AbstractAction("TDAR-Expr-AST") {
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(gui, msg, "TDAR-Expr-AST", JOptionPane.INFORMATION_MESSAGE);
            InputStream is = ClassLoader.getSystemClassLoader().
                    getResourceAsStream("vll4j/gui/resources/TDAR-Expr-AST.vll");
            gui.reset(false);
            gui.theFileManager.openInputStream(is, false);
            gui.setGrammarName("TDAR-Expr-AST");
            gui.theRuleManager.reset();
        }
        String msg = "<html>" + 
            "This grammar supports discussion of the techinque in section 3.3, <br/>" + 
            "<i>Evaluating Expressions Using an AST Intermediate Form</i>, <br/>" + 
            "of the book <i>The Definitive ANTLR Reference</i> <br>" + 
            "(http://pragprog.com/book/tpantlr/the-definitive-antlr-reference)<br/><br/>" +
            "Further discussion of the use of this grammar can be <br/>" + 
            "found at the following url:<br/>" + 
            "&nbsp;&nbsp;&nbsp;&nbsp;http://vll.java.net/examples/a-quick-tour.html";
    };

    Action sampleTdarSimpleTreeInterpreter = new AbstractAction("TDAR-Simple-Tree-Based-Interpreter") {
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(gui, msg, "TDAR-Simple-Tree-Based-Interpreter", JOptionPane.INFORMATION_MESSAGE);
            InputStream is = ClassLoader.getSystemClassLoader().
                    getResourceAsStream("vll4j/gui/resources/TDAR-Simple-Tree-Based-Interpreter.vll");
            gui.reset(false);
            gui.theFileManager.openInputStream(is, false);
            gui.setGrammarName("TDAR-Simple-Tree-Based-Interpreter");
            gui.theRuleManager.reset();
        }
        String msg = "<html>" + 
            "This example is based on the parser described at the very end of <br/>" + 
            "section 3.3, <i>Evaluating Expressions Using an AST Intermediate Form</i>, <br/>" + 
            "of the book <i>The Definitive ANTLR Reference</i> <br/>" + 
            "(http://pragprog.com/book/tpantlr/the-definitive-antlr-reference)<br/><br/>" +
            "Complete details can be found here: <br/> " +
            "http://www.antlr.org/wiki/display/ANTLR3/Simple+tree-based+interpeter<br/><br/>" +
            "You can find sample test input at the web-page above. <br/><br/>" +
            "NOTE: Each 'statement' in the test input <br/>" +
            "(including the last) must end with a NEWLINE<br/><br/>" +
            "Read more about this parser at the following url:<br/>" + 
            "&nbsp;&nbsp;&nbsp;&nbsp;http://vll.java.net/examples/a-quick-tour.html";
   };

    Action samplePs2eArithExpr = new AbstractAction("PS2E-ArithExpr") {
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(gui, msg, "P2SE-ArithExpr", JOptionPane.INFORMATION_MESSAGE);
            InputStream is = ClassLoader.getSystemClassLoader().
                    getResourceAsStream("vll4j/gui/resources/PS2E-ArithExpr.vll");
            gui.reset(false);
            gui.theFileManager.openInputStream(is, false);
            gui.setGrammarName("P2SE-ArithExpr");
            gui.theRuleManager.reset();
        }
        String msg = "<html>" + 
            "This parser is based on the code at page 760 of \"Programming in Scala\"<br/>" + 
            "(http://www.artima.com/shop/programming_in_scala_2ed)<br/><br/>" + 
            "Another version of this parser with action-code that evaluages the<br>" + 
            "AST to a number is also available (P2SE-ArithExpr-Action).<br/><br/>" +
            "IMPORTANT: Select the top-level parser (Expr) when running it.<br/>" +
            "Sample input (remove quotes): \"(2 + 3) * (7 - 3)\"</html>";
    };

    Action samplePs2eArithExprAction = new AbstractAction("PS2E-ArithExpr-Action") {
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(gui, msg, "PS2E-ArithExpr-Action", JOptionPane.INFORMATION_MESSAGE);
            InputStream is = ClassLoader.getSystemClassLoader().
                    getResourceAsStream("vll4j/gui/resources/PS2E-ArithExpr-Action.vll");
            gui.reset(false);
            gui.theFileManager.openInputStream(is, false);
            gui.setGrammarName("PS2E-ArithExpr-Action");
            gui.theRuleManager.reset();
        }
        String msg = "<html>" + 
            "This parser is also based on the code at page 760 of \"Programming in Scala\"<br/>" + 
            "(http://www.artima.com/shop/programming_in_scala_2ed),<br/>" + 
            "but it also has actions that evaluate the AST to a number.<br><br/>" + 
            "IMPORTANT: Select the top-level parser (Expr) when running it.<br/>" +
            "Sample input (remove quotes): \"(2 + 3) * (7 - 3)\"</html>";
    };
    
    Action samplePswpPayrollParserCombinators = new AbstractAction("PSWP-Payroll-Parser-Combinators") {
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(gui, msg, "PSWP-Payroll-Parser-Combinators", JOptionPane.INFORMATION_MESSAGE);
            InputStream is = ClassLoader.getSystemClassLoader().
                    getResourceAsStream("vll4j/gui/resources/PSWP-Payroll-Parser-Combinators.vll");
            gui.reset(false);
            gui.theFileManager.openInputStream(is, false);
            gui.setGrammarName("PSWP-Payroll-Parser-Combinators");
            gui.theRuleManager.reset();
        }
        String msg = "<html>" + 
            "This example is based on the parser described around page-240 <br/>" + 
            "of the book <i>Programming Scala</i> (http://programmingscala.com/)<br/><br/>" +
            "An online version of the parser code can be found here: <br/> " +
            "http://ofps.oreilly.com/titles/9780596155957/DomainSpecificLanguages.html<br/>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;#_generating_paychecks_with_the_external_dsl<br/><br/>" +
            "This parser also includes a special rule that serves as a <i>test-harness</i> <br/>" +
            "by using an action-function to wrap the main rule in a <br/>" +
            "test-data setup part and a output-checker part. <br/>" +
            "The rule called <i>PaycheckTester</i> is the test-harness. <br/><br/>" +
            "Read more about this parser at the following url:<br/>" + 
            "&nbsp;&nbsp;&nbsp;&nbsp;http://vll.java.net/RapidPrototypingForScala.html";
   };

    Action displayHelpMain = new AbstractAction("Help", Resources.help16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (helpPane == null)
                createHelpPane();
            helpPane.setSelectedIndex(5);
            JOptionPane.showMessageDialog(gui, helpPane, title + " Help", JOptionPane.PLAIN_MESSAGE);
        }
    };

    Action displayHelpRuleTree = new AbstractAction("", Resources.information16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (helpPane == null)
                createHelpPane();
            helpPane.setSelectedIndex(2);
            JLabel lbl = (JLabel)helpPane.getSelectedComponent();
            if (lbl.getText().isEmpty())
                lbl.setText(getHtml("PanelRuleTree.html"));
            JOptionPane.showMessageDialog(gui, helpPane, title + " Help", JOptionPane.PLAIN_MESSAGE);
        }
    };

    Action displayHelpAST = new AbstractAction("", Resources.information16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (helpPane == null)
                createHelpPane();
            helpPane.setSelectedIndex(0);
            JLabel lbl = (JLabel)helpPane.getSelectedComponent();
            if (lbl.getText().isEmpty())
                lbl.setText(getHtml("PanelAST.html"));
            JOptionPane.showMessageDialog(gui, helpPane, title + " Help", JOptionPane.PLAIN_MESSAGE);
        }
    };

    Action displayHelpActionCode = new AbstractAction("", Resources.information16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (helpPane == null)
                createHelpPane();
            helpPane.setSelectedIndex(1);
            JLabel lbl = (JLabel)helpPane.getSelectedComponent();
            if (lbl.getText().isEmpty())
                lbl.setText(getHtml("PanelActionCode.html"));
            JOptionPane.showMessageDialog(gui, helpPane, title + " Help", JOptionPane.PLAIN_MESSAGE);
        }
    };

    Action displayHelpTestInput = new AbstractAction("", Resources.information16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (helpPane == null)
                createHelpPane();
            helpPane.setSelectedIndex(3);
            JLabel lbl = (JLabel)helpPane.getSelectedComponent();
            if (lbl.getText().isEmpty())
                lbl.setText(getHtml("PanelTestInput.html"));
            JOptionPane.showMessageDialog(gui, helpPane, title + " Help", JOptionPane.PLAIN_MESSAGE);
        }
    };

    Action displayHelpTestLog = new AbstractAction("", Resources.information16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (helpPane == null)
                createHelpPane();
            helpPane.setSelectedIndex(4);
            JLabel lbl = (JLabel)helpPane.getSelectedComponent();
            if (lbl.getText().isEmpty())
                lbl.setText(getHtml("PanelTestLog.html"));
            JOptionPane.showMessageDialog(gui, helpPane, title + " Help", JOptionPane.PLAIN_MESSAGE);
        }
    };

    private void createHelpPane() {
        helpPane = new JTabbedPane();
        helpPane.add("AST", new JLabel());
        helpPane.add("Action-Code", new JLabel());
        helpPane.add("Rule-Tree", new JLabel());
        helpPane.add("Test-Input", new JLabel());
        helpPane.add("Test-Log", new JLabel());
        helpPane.add("VisualLangLab", new JLabel());
    }
    
    private String getHtml(String name) {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        URL res = cl.getResource("vll4j/gui/resources/" + name);
        try {
            InputStream hs = res.openStream();
            byte buf[] = new byte[hs.available()];
            hs.read(buf);
            return new String(buf);
        } catch (Exception e) {
            return "";
        }
    }

    private Vll4jGui gui;
    String title;
    private JTabbedPane helpPane = null;
}
