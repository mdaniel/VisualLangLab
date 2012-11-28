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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class PanelTesting extends JPanel {
    PanelTesting(Vll4jGui theGui) {
        this.theGui = theGui;
        setLayout(new BorderLayout());
        JPanel eastPanel = new JPanel();
        eastPanel.setLayout(new BorderLayout());
        eastPanel.add(new JLabel("Parser Log", SwingConstants.CENTER), BorderLayout.NORTH);
        logArea.setOpaque(true);
        logArea.setFont(new Font(Font.MONOSPACED, logArea.getFont().getStyle(),
                logArea.getFont().getSize()));
        logArea.setEditable(false);
        JPanel logBtnPanel = new JPanel();
        logBtnPanel.setLayout(new BorderLayout());
        logBtnPanel.add(logStatus, BorderLayout.CENTER);
        JButton helpButton2 = new JButton(theGui.theHelpFunctionsManager.displayHelpTestLog) {
            @Override
            public Dimension getPreferredSize() {
                int h = super.getPreferredSize().height;
                return new Dimension(h, h);
            }
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };
        logBtnPanel.add(helpButton2, BorderLayout.EAST);
        eastPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        eastPanel.add(logBtnPanel, BorderLayout.SOUTH);
        JPanel westPanel = new JPanel();
        westPanel.setLayout(new BorderLayout());
        westPanel.add(new JLabel("Parser Test Input", SwingConstants.CENTER), BorderLayout.NORTH);
        inputArea.setLineWrap(true);
        inputArea.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                int dot = e.getDot();
                int line = 1, col = 1;
                for (char ch: inputArea.getText().substring(0, dot).toCharArray()) {
                    if (ch == '\n') {
                        ++line; col = 1;
                    } else {
                        ++col;
                    }
                }
                inputStatus.setText(String.format(" Line %d, Column %d", line, col));
            }
        });
        JPanel inputBtnPanel = new JPanel();
        inputBtnPanel.setLayout(new BorderLayout());
        inputBtnPanel.add(inputStatus, BorderLayout.CENTER);
        JButton helpButton1 = new JButton(theGui.theHelpFunctionsManager.displayHelpTestInput) {
            @Override
            public Dimension getPreferredSize() {
                int h = super.getPreferredSize().height;
                return new Dimension(h, h);
            }
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };
        inputBtnPanel.add(helpButton1, BorderLayout.EAST);
        westPanel.add(new JScrollPane(inputArea), BorderLayout.CENTER);
        westPanel.add(inputBtnPanel, BorderLayout.SOUTH);
        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                westPanel, eastPanel);
        sp.setDividerLocation(theGui.frameWidth / 5);
        add(sp, BorderLayout.CENTER);
    }

    void setMultiFileLog(boolean mf) {
        if (mf) {
            logArea.setLineWrap(true);
        } else {
            logArea.setLineWrap(false);
        }
    }
    
    void logClear() {
        logArea.errLines.clear();
        logArea.setText("");
    }
    
    void logCopy() {
        int selectionStart = logArea.getSelectionStart();
        int selectionEnd = logArea.getSelectionEnd();
        logArea.setSelectionStart(0);
        logArea.setSelectionEnd(logArea.getDocument().getLength());
        logArea.copy();
        logArea.setSelectionStart(selectionStart);
        logArea.setSelectionEnd(selectionEnd);
    }

    PrintStream getOutStream() {
        OutputStream os = new OutputStream() {
            StringBuilder sb = new StringBuilder();
            public void write(int b) {
                System.out.flush();
                sb.append((char)b);
                if (b == '\n') {
                    final String line = sb.toString();
                    sb.setLength(0);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            logArea.append(line);
                            int len = logArea.getText().length();
                            logArea.select(len, len);
                        }
                    });
                }
            }
        };
        return new PrintStream(os, true);
    }

    PrintStream getErrStream() {
        OutputStream os = new OutputStream() {
            StringBuilder sb = new StringBuilder();
            public void write(int b) {
                System.out.flush();
                sb.append((char)b);
                if (b == '\n') {
                    final String line = sb.toString().replace("\t", "        ");
                    sb.setLength(0);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            logArea.errLines.add(new Integer[]{logArea.getText().length(), line.length()});
                            logArea.append(line);
                            int len = logArea.getText().length();
                            logArea.select(len, len);
                        }
                    });
                }
            }
        };
        return new PrintStream(os, true);
    }
    
    Vll4jGui theGui;

    JTextArea inputArea = new TextAreaCustom();
    LogTextArea logArea = new LogTextArea();
    private JLabel inputStatus = new JLabel();
    JLabel logStatus = new JLabel();
}
