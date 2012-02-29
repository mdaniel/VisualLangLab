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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.*;
import net.java.vll.vll4j.api.ApiParsers;
import net.java.vll.vll4j.api.Forest;
import net.java.vll.vll4j.api.NodeRoot;

public class Vll4jGui extends JFrame {

    private Vll4jGui() {
        super("VisualLangLab/J");
        titleString = getTitle();
        initializeAllData();
        theRuleManager.theComboBox.setMaximumSize(theRuleManager.theComboBox.getPreferredSize());
        createGui();
  }

    private void initializeAllData() {
        theHelpFunctionsManager = new ManagerHelp(this);
        theTestManager = new ManagerTesting(this);
        theTokenManager = new ManagerTokens(this);
        theFileManager = new ManagerFileOps(this);
        theRuleManager = new ManagerRules(this);
        theRuleManager.theComboBox = new JComboBox<String>();
        theRuleManager.theComboBox.setToolTipText("Select rule");
        theRuleManager.theComboBox.setRenderer(theCellRenderer);
        theMiscFunctionsManager = new ManagerMiscOps(this);
        reset(true);
    }
    
    void reset(boolean withMain) {
        theForest.tokenBank.clear();
        theForest.ruleBank.clear();
        if (withMain)
            theForest.ruleBank.put("Main", new NodeRoot("Main"));
        theRuleManager.theComboBox.setAction(null);
        theRuleManager.theComboBox.removeAllItems();
        if (withMain)
            theRuleManager.theComboBox.addItem("Main");
        theRuleManager.theComboBox.setAction(theRuleManager.comboBoxAction);
        if (withMain && theTreePanel != null)
            theTreePanel.setRuleName("Main");
        setTitle(titleString);
    }

    private void createGui() {
        setSize(frameWidth, frameHeight);
        setLocation((screenSize.width - frameWidth) / 2, (screenSize.height - frameHeight) / 2);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setIconImage(Resources.icon.getImage());
        addMenuBar();
        addToolBar();
        theTreePanel = new PanelRuleTree(this);
        theTestingPanel = new PanelTesting(this);
        theAstPanel = new PanelAstDisplay(this);
        theActionCodePanel = new PanelActionCode(this);
        JSplitPane sp1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, theAstPanel, theActionCodePanel);
        JSplitPane sp2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, theTreePanel, sp1);
        sp2.setDividerLocation(screenSize.width / 4);
        JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sp2, theTestingPanel);
        add(sp, BorderLayout.CENTER);
    }

    private void addMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        menuBar.add(fileMenu);
        fileMenu.add(theFileManager.fileNewAction);
        fileMenu.add(new JSeparator());
        fileMenu.add(theFileManager.fileOpenAction);
        fileMenu.add(theFileManager.fileSaveAction);
        fileMenu.add(theFileManager.fileSaveAsAction);
        fileMenu.add(new JSeparator());
        fileMenu.add(theFileManager.fileExitAction);
        menuBar.add(viewMenu);
        menuBar.add(tokenMenu);
        tokenMenu.add(theTokenManager.newLiteralAction);
        tokenMenu.add(theTokenManager.newRegexAction);
        tokenMenu.add(theTokenManager.editTokenAction);
        tokenMenu.add(theTokenManager.findTokenAction);
        tokenMenu.add(new JSeparator());
        tokenMenu.add(theTokenManager.deleteTokenAction);
        tokenMenu.add(new JSeparator());
        tokenMenu.add(theFileManager.importTokenAction);
        tokenMenu.add(theFileManager.exportTokenAction);
        menuBar.add(ruleMenu);
        ruleMenu.add(theRuleManager.ruleNewAction);
        ruleMenu.add(theRuleManager.ruleFindAction);
        ruleMenu.add(new JSeparator());
        ruleMenu.add(theRuleManager.ruleRenameAction);
        ruleMenu.add(theRuleManager.ruleOptimizeAction);
        ruleMenu.add(new JSeparator());
        ruleMenu.add(theRuleManager.ruleDeleteAction);
        menuBar.add(globalsMenu);
        globalsMenu.add(theMiscFunctionsManager.globalsWhitespaceAction);
        globalsMenu.add(theMiscFunctionsManager.globalsCommentAction);
        menuBar.add(logMenu);
        logMenu.add(theTestManager.logCopyAction);
        logMenu.add(theTestManager.logClearAction);
        menuBar.add(testMenu);
        testMenu.add(theTestManager.parseInputAction);
        testMenu.add(theTestManager.parseFileAction);
        testMenu.add(new JSeparator());
        testMenu.add(testTreeHandlerMenu);
        ButtonGroup bg = new ButtonGroup();
        testTreeHandlerBasic = new JRadioButtonMenuItem(theTestManager.treeHandlerBasicAction);
        testTreeHandlerBasic.setSelected(true);
        testTreeHandlerMenu.add(testTreeHandlerBasic);
        testTreeHandlerStructured = new JRadioButtonMenuItem(theTestManager.treeHandlerStructuredAction);
        testTreeHandlerMenu.add(testTreeHandlerStructured);
        bg.add(testTreeHandlerBasic);
        bg.add(testTreeHandlerStructured);
        testTraceAllItem = new JCheckBoxMenuItem(theTestManager.traceAllAction);
        testMenu.add(testTraceAllItem);
        menuBar.add(helpMenu);
        helpMenu.add(theHelpFunctionsManager.displayHelpMain);
//        helpMenu.addSeparator();
        helpMenu.add(helpSamplesMenu);
        helpSamplesMenu.add(new JMenuItem(theHelpFunctionsManager.sampleTdarExpr));
        helpSamplesMenu.add(new JMenuItem(theHelpFunctionsManager.sampleTdarExprActions));
        helpSamplesMenu.add(new JMenuItem(theHelpFunctionsManager.sampleTdarExprAst));
        helpSamplesMenu.add(new JMenuItem(theHelpFunctionsManager.sampleTdarSimpleTreeInterpreter));
        helpSamplesMenu.add(new JSeparator());
        helpSamplesMenu.add(new JMenuItem(theHelpFunctionsManager.samplePs2eArithExpr));
        helpSamplesMenu.add(new JMenuItem(theHelpFunctionsManager.samplePs2eSimpleJson));
        helpSamplesMenu.add(new JMenuItem(theHelpFunctionsManager.samplePs2eArithExprAction));
        helpSamplesMenu.add(new JSeparator());
        helpSamplesMenu.add(new JMenuItem(theHelpFunctionsManager.samplePswpPayrollParserCombinators));
        helpMenu.add(theHelpFunctionsManager.versionCheck);
        helpMenu.add(theHelpFunctionsManager.aboutAction);
    }
    
    private Action tip(Action aa) {
        aa.putValue(Action.SHORT_DESCRIPTION, aa.getValue(Action.NAME));
        return aa;
    }
    
    void addToolBar() {
        JToolBar toolBar = new JToolBar();
        getContentPane().add(toolBar, BorderLayout.NORTH);
        toolBar.add(tip(theFileManager.fileNewAction));
        toolBar.add(tip(theFileManager.fileOpenAction));
        toolBar.add(tip(theFileManager.fileSaveAction));
        toolBar.add(tip(theFileManager.fileSaveAsAction));
        toolBar.addSeparator();
        theRuleManager.ruleBackAction.setEnabled(false);
        toolBar.add(tip(theRuleManager.ruleBackAction));
        toolBar.add(theRuleManager.theComboBox);
        toolBar.add(tip(theRuleManager.ruleNewAction));
        toolBar.add(tip(theRuleManager.ruleFindAction));
        toolBar.add(tip(theRuleManager.ruleOptimizeAction));
        toolBar.add(tip(theRuleManager.ruleRenameAction));
        toolBar.addSeparator();
        toolBar.add(tip(theTokenManager.newLiteralAction));
        toolBar.add(tip(theTokenManager.newRegexAction));
        toolBar.add(tip(theTokenManager.findTokenAction));
        toolBar.add(tip(theTokenManager.editTokenAction));
        toolBar.add(tip(theFileManager.importTokenAction));
        toolBar.add(tip(theFileManager.exportTokenAction));
        toolBar.addSeparator();
        toolBar.add(tip(theTestManager.parseInputAction));
        toolBar.add(tip(theTestManager.parseFileAction));
        toolBar.add(tip(theTestManager.parseStopAction));
        toolBar.addSeparator();
        toolBar.add(tip(theTestManager.logCopyAction));
        toolBar.add(tip(theTestManager.logClearAction));
        theTestManager.parseStopAction.setEnabled(false);
//        toolBar.addSeparator();
//        toolBar.add(tip(theHelpFunctionsManager.displayHelpMain));
    }

    void setGrammarName(String grammarName) {
        setTitle(String.format("%s - %s", titleString, grammarName));
    }
    
    private void warnPreview() {
        String msg = "<html>This preview of the pure Java version has the following limitations:<br/><br/>" +
                "&nbsp;&nbsp;1) Packrat parsing not yet fully tested<br/>" +
                "&nbsp;&nbsp;3) Some GUI controls are not yet implemented<br/>" +
                "&nbsp;&nbsp;3) Documentation is still being updated<br/><br/>" +
                "It has all other planned capabilities, specifically:<br/><br/>" +
                "&nbsp;&nbsp;a) Written in Java, with no other dependencies<br/>" +
                "&nbsp;&nbsp;b) Action-code supported in JavaScript only<br/>" +
                "&nbsp;&nbsp;c) Same GUI, grammar-file format, as previous version<br/>" +
                "&nbsp;&nbsp;d) Much smaller download than previous executable JAR<br/>" +
                "&nbsp;&nbsp;e) AST structure and API mostly backward-compatible<br/><br/>" +
                "Check back at http://vll.java.net/ for full release version" ;
        JOptionPane.showMessageDialog(rootPane, msg, "Version 10 (Pure Java) Preview", JOptionPane.PLAIN_MESSAGE);
    }

    public static void main(String[] args) {
        if (System.getProperty("os.name").contains("Windows")) {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (Exception e) {}
        }
        Vll4jGui me = new Vll4jGui();
        System.setOut(me.theTestingPanel.getOutStream());
        System.setErr(me.theTestingPanel.getErrStream());
        me.setVisible(true);
//        me.warnPreview();
    }
    
    PanelRuleTree theTreePanel = null;
    PanelTesting theTestingPanel = null;
    PanelActionCode theActionCodePanel = null;
    PanelAstDisplay theAstPanel = null;
    ManagerTesting theTestManager = null;
    ManagerTokens theTokenManager = null;
    ManagerFileOps theFileManager = null;
    ManagerRules theRuleManager = null;
    ManagerMiscOps theMiscFunctionsManager = null;
    ManagerHelp theHelpFunctionsManager = null;
    
    ApiParsers apiParsers = new ApiParsers();
    Forest theForest = new Forest();
    
    JMenu fileMenu = new JMenu("File");
    JMenu viewMenu = new JMenu("View");
    JMenu tokenMenu = new JMenu("Tokens");
    JMenu ruleMenu = new JMenu("Rules");
    JMenu globalsMenu = new JMenu("Globals");
    JMenu logMenu = new JMenu("Log");
    JMenu testMenu = new JMenu("Test");
    JMenu testTreeHandlerMenu = new JMenu("Tree handler");
    JMenuItem testTreeHandlerBasic = null;
    JMenuItem testTreeHandlerStructured = null;
    JMenuItem testTraceAllItem = null;
    JMenu helpMenu = new JMenu("Help");
    JMenuItem helpHelp = null;
    JMenuItem helpAbout = null;
    JMenu helpSamplesMenu = new JMenu("Sample grammars");
        
    Toolkit tk = Toolkit.getDefaultToolkit();
    Dimension screenSize = tk.getScreenSize();
    int frameWidth = (int) (screenSize.width * 0.75);
    int frameHeight = (int) (screenSize.height * 0.75);
    private String titleString;
    String version = "10.24";
    private ListCellRenderer theCellRenderer = new RendererRuleComboBox(this);
}
