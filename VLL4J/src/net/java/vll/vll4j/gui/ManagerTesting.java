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
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.script.ScriptException;
import javax.swing.*;
import net.java.vll.vll4j.api.NodeBase;
import net.java.vll.vll4j.api.VisitorParserGeneration;
import net.java.vll.vll4j.combinator.Parsers.ParseResult;
import net.java.vll.vll4j.combinator.Parsers.Parser;
import net.java.vll.vll4j.combinator.Reader;
import net.java.vll.vll4j.combinator.Utils;

public class ManagerTesting {

    ManagerTesting(Vll4jGui gui) {
        this.gui = gui;
    }

    Action parseInputAction = new AbstractAction("Parse text", Resources.alignLeft16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            myThread = new Thread() {
                @Override
                public void run() {
                    parseStopAction.setEnabled(true);
                    setEnabled(false);
                    runner(false);
                    setEnabled(true);
                    parseStopAction.setEnabled(false);
                    System.out.flush();
                }
            };
            myThread.start();
        }
    };

    Action parseFileAction = new AbstractAction("Parse file", Resources.host16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            myThread = new Thread() {
                @Override
                public void run() {
                    stopRequested = false;
                    parseStopAction.setEnabled(true);
                    setEnabled(false);
                    if (fileChooser == null) {
                        fileChooser = new JFileChooser();
                        fileChooser.setDialogTitle("Open");
                        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
                    }
                    if (fileChooser.showOpenDialog(gui) == JFileChooser.APPROVE_OPTION) {
                        try {
                            runner(true);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                    setEnabled(true);
                    parseStopAction.setEnabled(false);
                    System.out.flush();
                }
            };
            myThread.start();
        }
    };

    Action parseStopAction = new AbstractAction("Stop parsing", Resources.stop16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            stopRequested = true;
        }
    };

    Action treeHandlerBasicAction = new AbstractAction("Basic") {
        @Override
        public void actionPerformed(ActionEvent e) {
            printStructured = false;
        }
    };

    Action treeHandlerStructuredAction = new AbstractAction("Structured") {
        @Override
        public void actionPerformed(ActionEvent e) {
            printStructured = true;
        }
    };

    Action treeHandlerCustomAction = new AbstractAction("Custom") {
        @Override
        public void actionPerformed(ActionEvent e) {
            printStructured = false;
        }
    };

    Action traceAllAction = new AbstractAction("Trace all") {
        @Override
        public void actionPerformed(ActionEvent e) {
            traceAll = !traceAll;
        }
    };

    Action logClearAction = new AbstractAction("Clear log", Resources.clear16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            gui.theTestingPanel.logClear();
        }
    };

    Action logCopyAction = new AbstractAction("Copy log", Resources.copy16) {
        @Override
        public void actionPerformed(ActionEvent e) {
            gui.theTestingPanel.logCopy();
        }
    };
    
    private void appendStatus(final String status, final boolean reset) {
        SwingUtilities.invokeLater(new Thread() {
            @Override
        public void run() {
            if (reset)
                gui.theTestingPanel.logStatus.setText(status);
            else 
                gui.theTestingPanel.logStatus.setText(
                        gui.theTestingPanel.logStatus.getText() + status);
        }});
    }
    
    private File[] dredgeFiles(File f) {
        if (f.isDirectory()) {
            List<File> lf = new ArrayList<File>();
            dredgeFiles(f, lf);
            return lf.toArray(new File[lf.size()]);
        } else 
            return new File[] {f};
    }
    
    private void dredgeFiles(File root, List<File>lf) {
        File files[] = root.listFiles();
        for (File f: files) {
            if (f.isDirectory()) {
                dredgeFiles(f, lf);
            } else {
                lf.add(f);
            }
        }
    }

    private void runner(boolean fromFile) {
        NodeBase apex = gui.theTreePanel.rootNode;
        long t0 = System.currentTimeMillis(), t1, t2;
        visitorParserGenerator = new VisitorParserGeneration(gui.theForest, gui.apiParsers, traceAll);
        gui.theForest.bindings.put("vllParserTestInput", gui.theTestingPanel.inputArea);
        gui.theForest.bindings.put("vllParserLog", gui.theTestingPanel.logArea);
        Parser<? extends Object> parser = (Parser<? extends Object>) apex.accept(visitorParserGenerator);
        if (!visitorParserGenerator.parserGeneratedOk) {
            JOptionPane.showMessageDialog(gui, "Can't generate parser\nReview rule definitions", 
                    fromFile ? "ERROR - Parse file" : "ERROR - Parse input", JOptionPane.ERROR_MESSAGE);
            appendStatus(" Can't generate parser - Review rule definitions", true);
            return;
        } else {
            t1 = System.currentTimeMillis();
            appendStatus(String.format(" Combinators: %d ms", t1 - t0), true);
        }
        File inFile = null;
        if (fromFile) {
            inFile = fileChooser.getSelectedFile();
        }
        if (fromFile && inFile.isDirectory()) {
            inFile = fileChooser.getSelectedFile();
            t0 = System.currentTimeMillis();
            int countOk = 0, countNotOk = 0;
            for (File f: dredgeFiles(inFile)) {
                t1 = System.currentTimeMillis();
                try {
                    ReaderFile readerFile = new ReaderFile(f);
                    gui.theForest.bindings.put("vllSource", readerFile.source());
                    ParseResult pr = gui.apiParsers.parseAll(parser, readerFile);
                    t2 = System.currentTimeMillis();
                    if (pr.successful()) {
                        ++countOk;
                        System.out.printf("%s (%d bytes %d ms): OK%n", 
                                f.getAbsolutePath(), f.length(), t2 - t1);
                    } else {
                        ++countNotOk;
                        System.err.printf("%s (%d bytes %d ms): ERROR (line=%d, col=%d)%n", 
                                f.getAbsolutePath(), f.length(), t2 - t1, 
                                pr.next().line(), pr.next().column());
                    }
                } catch (Throwable t) {
                    ++countNotOk;
                    if (t.getCause() instanceof ScriptException) {
                        t.printStackTrace();
                        JOptionPane.showMessageDialog(gui, "Error in user-provided action-code\nDetails in stack-trace", 
                                "Action-code error", JOptionPane.ERROR_MESSAGE);
                        break;
                    } else if (t.getCause() instanceof StackOverflowError) {
                        System.err.printf("%s: ERROR: %s%n", 
                                f.getAbsolutePath(), t.getMessage());
                    }
                }
                appendStatus(String.format(" %d Ok, %d NOk in %d ms", countOk, countNotOk, t1 - t0), true);
                if (stopRequested) {
                    System.err.println("User-Requested STOP");
                    break;
                }
                System.out.flush();
            }
        } else {
            t0 = System.currentTimeMillis();
            try {
                Reader reader = fromFile ? new ReaderFile(inFile) : 
                        new ReaderTextArea(gui.theTestingPanel.inputArea);
                gui.theForest.bindings.put("vllSource", reader.source());
                ParseResult pr = gui.apiParsers.parseAll(parser, reader);
                t1 = System.currentTimeMillis();
                appendStatus(String.format(", Parser: %d ms", t1 - t0), false);
                if (pr.successful()) {
                    t0 = System.currentTimeMillis();
                    String ast = Utils.dumpValue(pr.get(), printStructured);
                    t1 = System.currentTimeMillis();
                    if (ast.length() > 50000)
                        JOptionPane.showMessageDialog(gui, "Large ASTs are slow to appear, please wait", "Print Delay Warning", JOptionPane.WARNING_MESSAGE);
                    appendStatus(String.format(", AST->String: %d ms", t1 - t0), false);
                    t0 = System.currentTimeMillis();
                    System.out.println(ast);
                    System.out.println();
                    t1 = System.currentTimeMillis();
                    appendStatus(String.format(", Printing: %d ms", t1 - t0), false);
                } else {
                    System.err.printf("%s%n", gui.apiParsers.dumpResult(pr));
                }
            } catch (Throwable t) {
                if (t.getCause() instanceof ScriptException) {
                    JOptionPane.showMessageDialog(gui, "Error in user-provided action-code\nSee details in stack-trace", 
                          "Action-code error", JOptionPane.ERROR_MESSAGE);
                  }
                t.printStackTrace();
            }
        }
    }

    private Vll4jGui gui;
    private VisitorParserGeneration visitorParserGenerator;
    private Thread myThread = null;
    private JFileChooser fileChooser = null;
    private boolean traceAll = false;
    private boolean stopRequested = false;
    private boolean printStructured = false;
}
