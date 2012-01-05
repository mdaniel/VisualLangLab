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

package vll4j.core;

import javax.swing.JTextArea;

public class ReaderTextArea implements Reader {
    public ReaderTextArea(JTextArea textArea, int offset) {
        this.textArea = textArea;
        this.offset = offset;
    }
    public ReaderTextArea(JTextArea textArea) {
        this(textArea, 0);
    }
    public CharSequence source() {return textArea.getText();}
    public int offset() {return offset;}
    public boolean atEnd() {return offset >= textArea.getText().length();}
    public char first() {return textArea.getText().charAt(offset);}
    public ReaderTextArea rest() {
        ReaderTextArea csr = new ReaderTextArea(textArea, offset + 1);
        csr.line = line; csr.column = column;
        return csr;
    }
    public int line() {return line;}
    public int column() {return column;}
    public ReaderTextArea drop(int n) {
        if (offset + n > textArea.getText().length())
            throw new IllegalArgumentException();
        for (int i = 0; i < n; ++i) {
            String src = textArea.getText();
            if (src.charAt(offset + i) == '\n') {
                ++line;
                column = 1;
            } else {
                ++column;
            }
        }
        ReaderTextArea csr = new ReaderTextArea(textArea, offset + n);
        csr.line = line; csr.column = column;
        return csr;
    }
    private JTextArea textArea;
    private int offset;
    private int line = 1, column = 1;
}
