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
        logArea.setFont(new Font(Font.MONOSPACED, logArea.getFont().getStyle(), 
                logArea.getFont().getSize()));
        logArea.setEditable(false);
        StyleConstants.setFontFamily(blackFont, "monospaced");
        StyleConstants.setFontSize(blackFont, 12);
        StyleConstants.setForeground(blackFont, Color.black);
        StyleConstants.setFontFamily(redFont, "monospaced");
        StyleConstants.setFontSize(redFont, 12);
        StyleConstants.setForeground(redFont, Color.red);
        JPanel logBtnPanel = new JPanel();
        logBtnPanel.setLayout(new BorderLayout());
        logBtnPanel.add(logStatus, BorderLayout.CENTER);
        helpButton2 = new JButton(theGui.theHelpFunctionsManager.displayHelpTestLog) {
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
        helpButton1 = new JButton(theGui.theHelpFunctionsManager.displayHelpTestInput) {
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
    
    void logClear() {
        logArea.setText("");
    }
    
    void logCopy() {
        logArea.copy();
    }
    
    PrintStream getOutStream() {
        OutputStream os = new OutputStream() {
            StringBuilder sb = new StringBuilder();
            @Override
            public void write(int b) {
                sb.append((char)b);
                if (b == '\n' || sb.length() >= 1024) {
                    logArea.setSelectionStart(logArea.getDocument().getLength());
                    logArea.setSelectionEnd(logArea.getDocument().getLength());
                    try {
                        logArea.getDocument().insertString(logArea.getDocument().getLength(), sb.toString(), blackFont);
                    } catch (BadLocationException ex) {}
//                    logArea.setSelectionStart(logArea.getDocument().getLength());
//                    logArea.setSelectionEnd(logArea.getDocument().getLength());
                    sb.setLength(0);
                }
            }
            @Override
            public void write(byte b[], int off, int len) throws IOException {
                if (b == null) {
                    throw new NullPointerException();
                } else if ((off < 0) || (off > b.length) || (len < 0) ||
                        ((off + len) > b.length) || ((off + len) < 0)) {
                    throw new IndexOutOfBoundsException();
                } else if (len == 0) {
                    return;
                }
                String s = new String(b, off, len);
                logArea.setSelectionStart(logArea.getDocument().getLength());
                logArea.setSelectionEnd(logArea.getDocument().getLength());
                try {
                    logArea.getDocument().insertString(logArea.getDocument().getLength(), s, blackFont);
                } catch (BadLocationException ex) {}
            }
        };
        return new PrintStream(os, true);
    }
    
    PrintStream getErrStream() {
        OutputStream os = new OutputStream() {
            StringBuilder sb = new StringBuilder();
            public void write(int b) {
                sb.append((char)b);
                if (b == '\n' || sb.length() >= 1024) {
//                    logArea.setSelectionStart(logArea.getDocument().getLength());
//                    logArea.setSelectionEnd(logArea.getDocument().getLength());
                    try {
                        logArea.getDocument().insertString(logArea.getDocument().getLength(), sb.toString(), redFont);
                    } catch (BadLocationException ex) {}
//                    logArea.setSelectionStart(logArea.getDocument().getLength());
//                    logArea.setSelectionEnd(logArea.getDocument().getLength());
                    sb.setLength(0);
                }
            }
        };
        return new PrintStream(os, true);
    }
    
    Vll4jGui theGui;
    
    JTextArea inputArea = new TextAreaCustom();
    JTextPane logArea = new JTextPane();
    private SimpleAttributeSet blackFont = new SimpleAttributeSet();
    private SimpleAttributeSet redFont = new SimpleAttributeSet();
    private JButton helpButton1 = null;
    private JButton helpButton2 = null;
    private JLabel inputStatus = new JLabel();
    JLabel logStatus = new JLabel();
}
