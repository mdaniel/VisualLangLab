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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import javax.swing.*;
import vll4j.core.Parsers.ParseResult;
import vll4j.core.Parsers.Parser;
import vll4j.tree.NodeBase;
import vll4j.tree.VisitorParserGeneration;

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
                    try {
                        runner(false);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    setEnabled(true);
                    parseStopAction.setEnabled(false);
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
                    parseStopAction.setEnabled(true);
                    setEnabled(false);
                    if (fileChooser == null) {
                        fileChooser = new JFileChooser();
                        fileChooser.setDialogTitle("Open");
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
                }
            };
            myThread.start();
        }
    };

    Action parseStopAction = new AbstractAction("Stop parsing", Resources.stop16) {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    };

    Action treeHandlerBasicAction = new AbstractAction("Basic") {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    };

    Action treeHandlerStructuredAction = new AbstractAction("Structured") {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    };

    Action treeHandlerCustomAction = new AbstractAction("Custom") {
        @Override
        public void actionPerformed(ActionEvent e) {
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
        public void run() {
            if (reset)
                gui.theTestingPanel.logStatus.setText(status);
            else 
                gui.theTestingPanel.logStatus.setText(
                        gui.theTestingPanel.logStatus.getText() + status);
        }});
    }

    private void runner(boolean fromFile) {
        NodeBase apex = gui.theTreePanel.rootNode;
        long t0 = System.currentTimeMillis(), t1;
        visitorParserGenerator = new VisitorParserGeneration(gui.theForest, gui.regexParsers, traceAll);
        Parser<? extends Object> parser = (Parser<? extends Object>) apex.accept(visitorParserGenerator);
        if (!visitorParserGenerator.parserGeneratedOk) {
            JOptionPane.showMessageDialog(gui, "Can't generate parser\nReview rule definitions", 
                    fromFile ? "ERROR - Parse file" : "ERROR - Parse input", JOptionPane.ERROR_MESSAGE);
        appendStatus("Can't generate parser - Review rule definitions", true);
            return;
        } else {
            t1 = System.currentTimeMillis();
            appendStatus(String.format(" Combinators: %d ms", t1 - t0), true);
        }
        ParseResult pr;
        if (fromFile) {
            String input = "";
            File inFile = fileChooser.getSelectedFile();
            try {
                BufferedReader r = new BufferedReader(new FileReader(inFile));
                StringBuilder sb = new StringBuilder();
                for (String line = r.readLine(); line != null; line = r.readLine()) {
                    sb.append(line).append('\n');
                }
                input = sb.toString();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(gui, ex.getMessage(), 
                        "ERROR - Parse file", JOptionPane.ERROR_MESSAGE);
                return;
            }
            t0 = System.currentTimeMillis();
            pr = gui.regexParsers.parseAll(parser, input);
            t1 = System.currentTimeMillis();
        } else {
            t0 = System.currentTimeMillis();
            pr = gui.regexParsers.parseAll(parser, new ReaderTextArea(gui.theTestingPanel.inputArea));
            t1 = System.currentTimeMillis();
        }
        appendStatus(String.format(", Parser: %d ms", t1 - t0), false);
        if (pr.successful()) {
//            System.out.printf("Combinators: %d ms", t1 - t0);
//            System.out.printf(", Parser: %d ms", t3 - t2);
            t0 = System.currentTimeMillis();
            String ast = gui.regexParsers.dumpValue(pr.get());
            t1 = System.currentTimeMillis();
            appendStatus(String.format(", AST.toString: %d ms", t1 - t0), false);
//            System.out.printf(", AST.toString: %d ms%n", t5 - t4);
            t0 = System.currentTimeMillis();
            System.out.println(ast);
            System.out.println();
            t1 = System.currentTimeMillis();
            appendStatus(String.format(", Printing: %d ms", t1 - t0), false);
        } else {
            System.err.printf("%s%n", gui.regexParsers.dumpResult(pr));
        }
    }

    private Vll4jGui gui;
    private VisitorParserGeneration visitorParserGenerator;
    private Thread myThread = null;
    private JFileChooser fileChooser = null;
    private boolean traceAll = false;
}
