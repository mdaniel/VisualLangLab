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
import java.io.InputStream;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

public class ManagerHelp {

    ManagerHelp(Vll4jGui gui) {
        this.gui = gui;
        title = String.format("VisualLangLab %s", gui.version);
    }
    
    Action helpAction = new AbstractAction("Help") {
        @Override
        public void actionPerformed(ActionEvent e) {
            String msg = String.format("Get help at http://vll.java.net/", gui.version);
            JOptionPane.showMessageDialog(gui, msg, title, JOptionPane.INFORMATION_MESSAGE);
        }
    };

    Action aboutAction = new AbstractAction("About VisualLangLab") {
        @Override
        public void actionPerformed(ActionEvent e) {
            JTabbedPane tp = new JTabbedPane();
            tp.add("VisualLangLab", new JLabel(Resources.splashImage));
            tp.add("Copyright", new JLabel(copyright));
            tp.add("Licenses", new JLabel(licenses));
            JOptionPane.showMessageDialog(gui, tp, "VisualLangLab 10.01", JOptionPane.INFORMATION_MESSAGE, Resources.icon);
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
            
    Action sampleTDARExpr = new AbstractAction("TDAR-Expr") {
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(gui, msg, "TDAR-Expr", JOptionPane.INFORMATION_MESSAGE);
            InputStream is = ClassLoader.getSystemClassLoader().
                    getResourceAsStream("vll4j/gui/resources/TDAR-Expr.vll");
            gui.theFileManager.openInputStream(is);
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
            gui.theFileManager.openInputStream(is);
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

    Action sampleTdarSimpleTreeInterpreter = new AbstractAction("TDAR-Expr-Simple-Tree-Based-Interpreter") {
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(gui, msg, "TDAR-Expr-Simple-Tree-Based-Interpreter", JOptionPane.INFORMATION_MESSAGE);
            gui.theRuleManager.reset();
        }
        String msg = "";
    };

    Action sampleP2SEArithExpr = new AbstractAction("PS2E-ArithExpr") {
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(gui, msg, "P2SE-ArithExpr", JOptionPane.INFORMATION_MESSAGE);
            InputStream is = ClassLoader.getSystemClassLoader().
                    getResourceAsStream("vll4j/gui/resources/PS2E-ArithExpr.vll");
            gui.theFileManager.openInputStream(is);
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

    Action sampleP2SEArithExprAction = new AbstractAction("PS2E-ArithExpr-Action") {
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(gui, msg, "PS2E-ArithExpr-Action", JOptionPane.INFORMATION_MESSAGE);
            InputStream is = ClassLoader.getSystemClassLoader().
                    getResourceAsStream("vll4j/gui/resources/PS2E-ArithExpr-Action.vll");
            gui.theFileManager.openInputStream(is);
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

    private Vll4jGui gui;
    String title;
}
