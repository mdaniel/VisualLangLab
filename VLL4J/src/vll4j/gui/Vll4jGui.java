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

import vll4j.tree.NodeRoot;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import vll4j.core.SimpleLexingRegexParsers;
import vll4j.tree.Forest;

public class Vll4jGui extends JFrame {

    private Vll4jGui() {
        super("VisualLangLab/J");
        titleString = getTitle();
        initializeAllData();
        theRuleManager.theComboBox.setMaximumSize(theRuleManager.theComboBox.getPreferredSize());
        createGui();
    }

    private void initializeAllData() {
        theTestManager = new ManagerTesting(this);
        theTokenManager = new ManagerTokens(this);
        theFileManager = new ManagerFileOps(this);
        theRuleManager = new ManagerRules(this);
        theRuleManager.theComboBox = new JComboBox<String>();
        theRuleManager.theComboBox.setToolTipText("Select rule");
        theMiscFunctionsManager = new ManagerMiscOps(this);
        theHelpFunctionsManager = new ManagerHelp(this);
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
        tokenMenu.add(theTokenManager.importTokenAction);
        tokenMenu.add(theTokenManager.exportTokenAction);
        menuBar.add(ruleMenu);
        ruleMenu.add(theRuleManager.ruleNewAction);
        ruleMenu.add(theRuleManager.ruleFindAction);
        ruleMenu.add(theRuleManager.ruleRenameAction);
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
        testTreeHandlerCustom = new JRadioButtonMenuItem(theTestManager.treeHandlerCustomAction);
        testTreeHandlerMenu.add(testTreeHandlerCustom);
        bg.add(testTreeHandlerBasic);
        bg.add(testTreeHandlerStructured);
        bg.add(testTreeHandlerCustom);
        testTraceAllItem = new JCheckBoxMenuItem(theTestManager.traceAllAction);
        testMenu.add(testTraceAllItem);
        menuBar.add(helpMenu);
        helpMenu.add(theHelpFunctionsManager.helpAction);
//        helpMenu.addSeparator();
        helpMenu.add(helpSamplesMenu);
        helpSamplesMenu.add(new JMenuItem(theHelpFunctionsManager.sampleTDARExpr));
        helpSamplesMenu.add(new JMenuItem(theHelpFunctionsManager.sampleTdarExprActions));
        helpSamplesMenu.add(new JMenuItem(theHelpFunctionsManager.sampleTdarSimpleTreeInterpreter));
        helpSamplesMenu.add(new JSeparator());
        helpSamplesMenu.add(new JMenuItem(theHelpFunctionsManager.sampleP2SEArithExpr));
        helpSamplesMenu.add(new JMenuItem(theHelpFunctionsManager.sampleP2SEArithExprAction));
//        helpMenu.addSeparator();
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
        toolBar.add(tip(theRuleManager.ruleRenameAction));
        toolBar.addSeparator();
        toolBar.add(tip(theTokenManager.newLiteralAction));
        toolBar.add(tip(theTokenManager.newRegexAction));
        toolBar.add(tip(theTokenManager.findTokenAction));
        toolBar.add(tip(theTokenManager.editTokenAction));
        toolBar.add(tip(theTokenManager.importTokenAction));
        toolBar.add(tip(theTokenManager.exportTokenAction));
        toolBar.addSeparator();
        toolBar.add(tip(theTestManager.parseInputAction));
        toolBar.add(tip(theTestManager.parseFileAction));
        toolBar.add(tip(theTestManager.parseStopAction));
        toolBar.addSeparator();
        toolBar.add(tip(theTestManager.logCopyAction));
        toolBar.add(tip(theTestManager.logClearAction));
        theTestManager.parseStopAction.setEnabled(false);
    }

    void setGrammarName(String grammarName) {
        setTitle(String.format("%s - %s", titleString, grammarName));
    }
    
    private void warnPreview() {
        String msg = "<html>This preview of Version-10.01 has the following limitations:<br/><br/>" +
                "1) Packrat parsing not yet implemented<br/>" +
                "2) No API for application programs<br/><br/>" +
                "It has all other capabilities planned for 10.01, specifically:<br/><br/>" +
                "a) Is written in Java, and uses no other APIs<br/>" +
                "b) Same GUI and other characteristics as previous versions<br/>" +
                "c) Much smaller download compared with previous versions<br/><br/>" +
                "Check back at http://vll.java.net/ for full release version" ;
        JOptionPane.showMessageDialog(rootPane, msg, "Version 10.01 Preview", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        long t0 = System.currentTimeMillis();
        if (System.getProperty("os.name").contains("Windows")) {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (Exception e) {}
        }
        Vll4jGui me = new Vll4jGui();
        System.setOut(me.theTestingPanel.getOutStream());
        System.setErr(me.theTestingPanel.getErrStream());
        while ((System.currentTimeMillis() - t0) <= 2000) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {}
        }
        me.setVisible(true);
        me.warnPreview();
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
    
    SimpleLexingRegexParsers regexParsers = new SimpleLexingRegexParsers();
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
    JMenuItem testTreeHandlerCustom = null;
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
    String version = "10.01";
}
